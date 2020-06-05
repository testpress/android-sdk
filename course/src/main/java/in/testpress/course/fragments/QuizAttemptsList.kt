package `in`.testpress.course.fragments

import `in`.testpress.course.R
import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.domain.DomainContentAttempt
import `in`.testpress.course.domain.getGreenDaoContent
import `in`.testpress.course.domain.getGreenDaoContentAttempts
import `in`.testpress.course.enums.Status
import `in`.testpress.course.repository.ExamContentRepository
import `in`.testpress.course.ui.ContentActivity
import `in`.testpress.course.ui.ContentAttemptListAdapter
import `in`.testpress.course.viewmodels.ExamContentViewModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class QuizAttemptsList : Fragment() {
    private lateinit var viewModel: ExamContentViewModel
    private lateinit var content: DomainContent
    private var contentId: Long = -1
    lateinit var contentAttempts: ArrayList<DomainContentAttempt>
    private lateinit var attemptList: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ExamContentViewModel(
                    ExamContentRepository(context!!)
                ) as T
            }
        }).get(ExamContentViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.quiz_attempts_list_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contentId = requireArguments().getLong(ContentActivity.CONTENT_ID)
        attemptList = view.findViewById(R.id.attempt_list)

        viewModel.getContent(contentId).observe(viewLifecycleOwner, Observer {
            when (it?.status) {
                Status.SUCCESS -> {
                    content = it.data!!
                    loadAttempts()
                }
            }
        })
    }

    private fun loadAttempts() {
        val url = content.attemptsUrl ?: "/api/v2.3/contents/${content.id}/attempts/"
        viewModel.loadContentAttempts(url, contentId)
            .observe(viewLifecycleOwner, Observer { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        contentAttempts = resource.data!!
                        display()
                    }
                }
            })
    }

    fun display() {
        val greenDaoContent = content.getGreenDaoContent(requireContext())
        val attempts = content.getGreenDaoContentAttempts(requireContext())
        attemptList.apply {
            isNestedScrollingEnabled = false
            layoutManager = LinearLayoutManager(activity)
            adapter = ContentAttemptListAdapter(activity, greenDaoContent, attempts.reversed())
            visibility = View.VISIBLE
        }
        attemptList.setHasFixedSize(true)
    }
}