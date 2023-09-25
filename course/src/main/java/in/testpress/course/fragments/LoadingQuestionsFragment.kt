package `in`.testpress.course.fragments

import `in`.testpress.course.R
import `in`.testpress.course.domain.DomainAttempt
import `in`.testpress.course.domain.DomainContentAttempt
import `in`.testpress.enums.Status
import `in`.testpress.course.repository.QuizQuestionsRepository
import `in`.testpress.course.ui.ContentActivity.CONTENT_ID
import `in`.testpress.course.viewmodels.QuizViewModel
import `in`.testpress.fragments.EmptyViewFragment
import `in`.testpress.fragments.EmptyViewListener
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

class LoadingQuestionsFragment : Fragment(),
    EmptyViewListener {
    private lateinit var attemptUrl: String
    private var examId = -1L
    private var contentId = -1L
    private var attemptId = -1L
    lateinit var viewModel: QuizViewModel
    lateinit var fragmentChangeListener: ShowQuizHandler
    private var contentAttempt: DomainContentAttempt? = null
    private var attempt: DomainAttempt? = null

    private lateinit var loadingLayout: LinearLayout
    private lateinit var emptyViewFragment: EmptyViewFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return QuizViewModel(
                    QuizQuestionsRepository(requireContext())
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
        attemptId = requireArguments().getLong("ATTEMPT_ID", -1)
    }

    private fun initializeEmptyViewFragment() {
        emptyViewFragment = EmptyViewFragment()
        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.empty_view_fragment, emptyViewFragment)
        transaction.commit()
    }

    private fun loadAttempt() {
        if (examId == -1L) {
            viewModel.loadAttempt(attemptId).observe(viewLifecycleOwner, Observer { resource ->
                when(resource?.status) {
                    Status.SUCCESS -> resource.data?.let { domainAttempt ->
                        attempt = domainAttempt
                        initUserSelectedAnswers()
                    }
                    Status.ERROR -> {
                        loadingLayout.visibility = View.GONE
                        emptyViewFragment.displayError(resource.exception!!)
                    }
                }
            })
        } else {
            viewModel.loadContentAttempt(contentId).observe(viewLifecycleOwner, Observer { resource ->
                when(resource?.status) {
                    Status.SUCCESS -> resource.data?.let { domainContentAttempt ->
                        contentAttempt = domainContentAttempt
                        attempt = domainContentAttempt.assessment
                        initUserSelectedAnswers()
                    }
                    Status.ERROR -> {
                        loadingLayout.visibility = View.GONE
                        emptyViewFragment.displayError(resource.exception!!)
                    }
                }
            })
        }

    }

    private fun initUserSelectedAnswers() {
        val questionsUrl = "/api/v2.5/attempts/${attempt?.id}/questions/"
        viewModel.loadUserSelectedAnswers(examId, attempt?.id!!, questionsUrl).observe(viewLifecycleOwner, Observer { resource ->
            when(resource?.status) {
                Status.SUCCESS -> {
                    val index = resource.data!!.indexOfFirst{
                        it.duration == null
                    }
                    fragmentChangeListener.showQuiz(contentAttempt?.id, attempt?.id, resource.data!!.size, index)
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
        loadAttempt()
    }
}

interface ShowQuizHandler {
    fun showQuiz(contentAttemptId: Long?, attemptId: Long?, totalNoOfQuestions: Int, index: Int)
}