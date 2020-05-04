package `in`.testpress.course.ui

import `in`.testpress.core.TestpressException
import `in`.testpress.course.R
import `in`.testpress.course.enums.Status
import `in`.testpress.course.fragments.ExamEndHanlder
import `in`.testpress.course.fragments.LoadingQuestionsFragment
import `in`.testpress.course.fragments.QuizSlideFragment
import `in`.testpress.course.fragments.ShowQuizHandler
import `in`.testpress.course.repository.QuizExamRepository
import `in`.testpress.course.util.ProgressDialog
import `in`.testpress.course.viewmodels.QuizExamViewModel
import `in`.testpress.exam.ui.ReviewStatsActivity
import `in`.testpress.ui.BaseToolBarActivity
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class QuizActivity : BaseToolBarActivity(), ShowQuizHandler, ExamEndHanlder {
    lateinit var viewModel: QuizExamViewModel
    private var examEndUrl: String? = null
    private var contentAttemptId: Long = -1
    private var examId: Long = -1
    private var alertDialog: AlertDialog? = null
    private lateinit var dialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.testpress_container_layout)
        examId = intent.getLongExtra("EXAM_ID", -1)
        dialog = ProgressDialog.progressDialog(this, "Ending Exam")
        initializeViewModel()
        initializeListeners()
        loadQuestions()
        customiseToolbar()
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

    private fun initializeViewModel() {
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return QuizExamViewModel(
                    QuizExamRepository(this@QuizActivity)
                ) as T
            }
        }).get(QuizExamViewModel::class.java)
    }

    private fun loadQuestions() {
        val fragment = LoadingQuestionsFragment().apply {
            arguments = intent.extras
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment).commitAllowingStateLoss()
    }

    private fun initializeListeners() {
        toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp)
        toolbar.setNavigationOnClickListener {
            showEndExamAlert()
        }

        viewModel.endExamState.observe(this, Observer {
            dialog.hide()
            when(it.status) {
                Status.SUCCESS -> {
                    val intent = ReviewStatsActivity.createIntent(this, examId, contentAttemptId)
                    finish()
                    startActivity(intent)
                }
                Status.ERROR -> {
                    handleExamEndError(it.exception!!)
                }
            }
        })
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

    override fun showQuiz(attemptId: Long, totalNoOfQuestions:Int, index: Int) {
        viewModel.loadAttempt(attemptId).observe(this, Observer {
            contentAttemptId = it?.data!!.id
            examEndUrl = it?.data?.assessment?.endUrl
            val examId = intent.getLongExtra("EXAM_ID", -1)
            val bundle = Bundle().apply {
                putLong("EXAM_ID", examId)
                putLong("ATTEMPT_ID", it.data.assessment!!.id)
                putInt("NO_OF_QUESTIONS", totalNoOfQuestions)
                putInt("START_INDEX", index)
            }
            val quizSlideFragment = QuizSlideFragment().apply { arguments=bundle }
            quizSlideFragment.endHanlder = this
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, quizSlideFragment).commitAllowingStateLoss()
        })
    }

    override fun endExam() {
        dialog.show()
        if (examEndUrl == null) {
            dialog.hide()
            finish()
        } else {
            viewModel.endExam(examEndUrl!!)
        }
    }
}