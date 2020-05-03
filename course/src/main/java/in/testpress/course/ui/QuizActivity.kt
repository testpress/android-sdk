package `in`.testpress.course.ui

import `in`.testpress.course.R
import `in`.testpress.course.enums.Status
import `in`.testpress.course.fragments.LoadingQuestionsFragment
import `in`.testpress.course.fragments.QuizSlideFragment
import `in`.testpress.course.fragments.ShowQuizHandler
import `in`.testpress.course.repository.QuizExamRepository
import `in`.testpress.course.util.ProgressDialog
import `in`.testpress.course.viewmodels.QuizExamViewModel
import `in`.testpress.exam.ui.ReviewStatsActivity
import `in`.testpress.ui.BaseToolBarActivity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class QuizActivity : BaseToolBarActivity(), ShowQuizHandler {
    lateinit var viewModel: QuizExamViewModel
    private var examEndUrl: String? = null
    private var contentAttemptId: Long = -1
    private var examId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.testpress_container_layout)
        examId = intent.getLongExtra("EXAM_ID", -1)
        initializeListeners()
        initializeViewModel()
        loadQuestions()
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
            val alertDialogBuilder = AlertDialog.Builder(this, R.style.TestpressAppCompatAlertDialogStyle)
            alertDialogBuilder.setTitle(R.string.testpress_end_title)
            alertDialogBuilder.setMessage("Are you sure that you want to end the Quiz ?")
            alertDialogBuilder.setPositiveButton(R.string.testpress_end) { _, _ ->
                val dialog = ProgressDialog.progressDialog(this)
                dialog.show()

                if (examEndUrl == null) {
                    dialog.hide()
                    finish()
                } else {
                    viewModel.endExam(examEndUrl!!).observe(this, Observer {
                        when(it.status) {
                            Status.SUCCESS -> {
                                dialog.hide()
                                val intent = ReviewStatsActivity.createIntent(this, examId, contentAttemptId)
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                                startActivity(intent)
                            }
                        }
                    })
                }

            }

            alertDialogBuilder.setNegativeButton(R.string.testpress_cancel, null)
            alertDialogBuilder.show()
        }
    }

    override fun showQuiz(attemptId: Long) {
        viewModel.loadAttempt(attemptId).observe(this, Observer {
            contentAttemptId = it?.data!!.id
            examEndUrl = it?.data?.assessment?.endUrl
            val examId = intent.getLongExtra("EXAM_ID", -1)
            val bundle = Bundle().apply {
                putLong("EXAM_ID", examId)
            }
            val quizSlideFragment = QuizSlideFragment().apply { arguments=bundle }
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, quizSlideFragment).commitAllowingStateLoss()
        })
    }
}