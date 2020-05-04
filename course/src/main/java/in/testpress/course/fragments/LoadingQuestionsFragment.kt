package `in`.testpress.course.fragments

import `in`.testpress.course.R
import `in`.testpress.course.domain.DomainContentAttempt
import `in`.testpress.course.enums.Status
import `in`.testpress.course.repository.UserSelectedAnswersRepository
import `in`.testpress.course.ui.ContentActivity.CONTENT_ID
import `in`.testpress.course.viewmodels.QuizViewModel
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class LoadingQuestionsFragment : Fragment(), EmptyViewListener {
    private lateinit var attemptUrl: String
    private var examId = -1L
    private var contentId = -1L
    lateinit var viewModel: QuizViewModel
    lateinit var fragmentChangeListener: ShowQuizHandler
    private lateinit var contentAttempt: DomainContentAttempt

    private lateinit var loadingLayout: LinearLayout
    private lateinit var emptyViewFragment: EmptyViewFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return QuizViewModel(
                    UserSelectedAnswersRepository(requireContext())
                ) as T
            }
        }).get(QuizViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.loading_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingLayout = view.findViewById(R.id.loading_layout)
        parseArguments()
        initializeEmptyViewFragment()
        loadAttempt()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (parentFragment != null) {
            fragmentChangeListener = parentFragment as ShowQuizHandler
        } else {
            fragmentChangeListener = context as ShowQuizHandler
        }
    }

    private fun parseArguments() {
        attemptUrl = requireArguments().getString("ATTEMPT_URL", "")
        examId = requireArguments().getLong("EXAM_ID", -1)
        contentId = requireArguments().getLong(CONTENT_ID, -1)
    }

    private fun initializeEmptyViewFragment() {
        emptyViewFragment = EmptyViewFragment()
        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.empty_view_fragment, emptyViewFragment)
        transaction.commit()
    }

    private fun initUserSelectedAnswers() {
        val attempt = contentAttempt.assessment!!
        viewModel.loadUserSelectedAnswers(attempt.id, attempt.questionsUrl!!).observe(viewLifecycleOwner, Observer { resource ->
            when(resource?.status) {
                Status.SUCCESS -> {
                    fragmentChangeListener.showQuiz(contentAttempt.id, resource.data!!.size)
                }
                Status.ERROR -> {
                    loadingLayout.visibility = View.GONE
                    emptyViewFragment.displayError(resource.exception!!)
                }
            }
        })
    }

    private fun loadAttempt() {
        viewModel.loadAttempt(contentId).observe(viewLifecycleOwner, Observer {resource ->
            when(resource?.status) {
                Status.SUCCESS -> resource.data?.let { domainContentAttempt ->
                    contentAttempt = domainContentAttempt
                    initUserSelectedAnswers()
                }
                Status.ERROR -> {
                    loadingLayout.visibility = View.GONE
                    emptyViewFragment.displayError(resource.exception!!)
                }
            }
        })
    }

    override fun onRetryClick() {
        loadingLayout.visibility = View.VISIBLE
        if (::contentAttempt.isInitialized) {
            initUserSelectedAnswers()
        } else {
            loadAttempt()
        }
    }
}

interface ShowQuizHandler {
    fun showQuiz(attemptId: Long, totalNoOfQuestions: Int)
}