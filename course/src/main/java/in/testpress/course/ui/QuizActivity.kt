package `in`.testpress.course.ui

import `in`.testpress.core.TestpressException
import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.R
import `in`.testpress.course.domain.getEndAttemptUrl
import `in`.testpress.course.domain.getGreenDaoAttempt
import `in`.testpress.enums.Status
import `in`.testpress.course.fragments.ExamEndHanlder
import `in`.testpress.course.fragments.LoadingQuestionsFragment
import `in`.testpress.course.fragments.QuestionNumberHandler
import `in`.testpress.course.fragments.QuizSlideFragment
import `in`.testpress.course.fragments.ShowQuizHandler
import `in`.testpress.course.repository.QuizExamRepository
import `in`.testpress.course.util.ProgressDialog
import `in`.testpress.course.viewmodels.QuizExamViewModel
import `in`.testpress.exam.ui.ReviewStatsActivity
import `in`.testpress.ui.BaseToolBarActivity
import `in`.testpress.util.InternetConnectivityChecker
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class QuizActivity : BaseToolBarActivity(), ShowQuizHandler, ExamEndHanlder, QuestionNumberHandler {
    lateinit var viewModel: QuizExamViewModel
    private var examEndUrl: String? = null
    private var contentAttemptId: Long = -1
    private var attemptId: Long = -1
    private var examId: Long = -1
    private var alertDialog: AlertDialog? = null
    private lateinit var dialog: Dialog
    private lateinit var questionNumberView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.quiz_container_layout)
        examId = intent.getLongExtra("EXAM_ID", -1)
        dialog = ProgressDialog.progressDialog(this, "Ending Exam")
        bindViews()
        initializeViewModel()
        initializeListeners()
        loadQuestions()
        customiseToolbar()
    }

    private fun bindViews() {
        questionNumberView = findViewById(R.id.question_number)
        questionNumberView.typeface = TestpressSdk.getRubikMediumFont(this)
    }

    private fun initializeViewModel() {
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return QuizExamViewModel(
                    QuizExamRepository(this@QuizActivity)
                ) as T
            }
        }).get(QuizExamViewModel::class.java)
    }

    private fun initializeListeners() {
        toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp)
        toolbar.setNavigationOnClickListener {
            showEndExamAlert()
        }

        viewModel.endAttemptState.observe(this) {
            dialog.hide()
            when(it.status) {
                Status.SUCCESS -> {
                    val intent = ReviewStatsActivity.createIntent(this, it.data?.getGreenDaoAttempt(this))
                    finish()
                    startActivity(intent)
                }
                Status.ERROR -> {
                    handleExamEndError(it.exception!!)
                }
            }
        }

        viewModel.endContentAttemptState.observe(this, Observer {
            dialog.hide()
            when(it.status) {
                Status.SUCCESS -> {
                    if (InternetConnectivityChecker.isConnected(this)) {
                        val intent = ReviewStatsActivity.createIntent(this, examId, contentAttemptId)
                        finish()
                        startActivity(intent)
                    }
                    finish()
                }
                Status.ERROR -> {
                    handleExamEndError(it.exception!!)
                }
            }
        })
    }

    private fun loadQuestions() {
        val fragment = LoadingQuestionsFragment().apply {
            arguments = intent.extras
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment).commitAllowingStateLoss()
    }

    private fun customiseToolbar() {
        toolbar.setBackgroundColor(Color.WHITE)
        toolbar.setTitleTextColor(resources.getColor(R.color.testpress_color_primary))
        supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        showLogoInToolbar()
        val closeButton = findViewById<ImageButton>(R.id.close)
        closeButton.visibility = View.VISIBLE
        closeButton.setOnClickListener {
            showEndExamAlert()
        }
    }

    private fun showEndExamAlert() {
        alertDialog?.dismiss()
        val alertDialogBuilder = AlertDialog.Builder(this, R.style.TestpressAppCompatAlertDialogStyle)
        alertDialogBuilder.setTitle(R.string.testpress_end_title)
        alertDialogBuilder.setMessage("Are you sure that you want to end the Quiz ?")
        alertDialogBuilder.setPositiveButton(R.string.testpress_end) { _, _ ->
            endExam()
        }

        alertDialogBuilder.setNegativeButton(R.string.testpress_cancel, null)
        alertDialog = alertDialogBuilder.show()
    }

    private fun handleExamEndError(exception: TestpressException) {
        alertDialog?.dismiss()
        val alertDialogBuilder = AlertDialog.Builder(this, R.style.TestpressAppCompatAlertDialogStyle)
        if (exception.isNetworkError) {
            alertDialogBuilder.setTitle(R.string.testpress_no_internet_connection)
            alertDialogBuilder.setMessage("Please check your internet connection and try again.")
        } else {
            alertDialogBuilder.setTitle("Error Occurred")
            alertDialogBuilder.setMessage("Error occurred while ending exam. Please try again.")
        }
        alertDialogBuilder.setPositiveButton(R.string.testpress_try_again) { _, _ ->
            endExam()
        }

        alertDialogBuilder.setNegativeButton(R.string.testpress_cancel, null)
        alertDialog = alertDialogBuilder.show()
    }

    override fun showQuiz(
        ContentAttemptId: Long?,
        attemptId: Long?,
        totalNoOfQuestions: Int,
        index: Int
    ) {
        if (examId == -1L) {
            viewModel.loadAttempt(attemptId!!).observe(this, Observer {
                contentAttemptId = -1
                this.attemptId = it.data!!.id
                examEndUrl = it?.data?.endUrl
                val examId = intent.getLongExtra("EXAM_ID", -1)
                val bundle = Bundle().apply {
                    putLong("EXAM_ID", examId)
                    putLong("ATTEMPT_ID", it.data!!.id)
                    putInt("NO_OF_QUESTIONS", totalNoOfQuestions)
                    putInt("START_INDEX", index)
                }
                val quizSlideFragment = QuizSlideFragment().apply { arguments=bundle }
                quizSlideFragment.endHanlder = this
                quizSlideFragment.questionNumberHandler = this
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, quizSlideFragment).commitAllowingStateLoss()
            })
        } else {
            viewModel.loadContentAttempt(ContentAttemptId!!).observe(this, Observer {
                contentAttemptId = it?.data!!.id
                this.attemptId = it.data!!.assessment?.id!!
                examEndUrl = it?.data?.getEndAttemptUrl(this)
                val examId = intent.getLongExtra("EXAM_ID", -1)
                val bundle = Bundle().apply {
                    putLong("EXAM_ID", examId)
                    putLong("ATTEMPT_ID", it.data!!.assessment!!.id)
                    putInt("NO_OF_QUESTIONS", totalNoOfQuestions)
                    putInt("START_INDEX", index)
                }
                val quizSlideFragment = QuizSlideFragment().apply { arguments=bundle }
                quizSlideFragment.endHanlder = this
                quizSlideFragment.questionNumberHandler = this
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, quizSlideFragment).commitAllowingStateLoss()
            })
        }
    }

    override fun endExam() {
        dialog.show()
        if (examEndUrl == null) {
            dialog.hide()
            finish()
        } else {
            viewModel.endExam(examId, examEndUrl!!, attemptId)
        }
    }

    override fun changeQuestionNumber(number: Int, total: Int) {
        questionNumberView.visibility = View.VISIBLE
        questionNumberView.text = "$number of $total"
    }

    override fun onBackPressed() {
        showEndExamAlert()
    }
}