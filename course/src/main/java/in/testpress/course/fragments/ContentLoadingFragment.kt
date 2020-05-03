package `in`.testpress.course.fragments

import `in`.testpress.course.R
import `in`.testpress.course.TestpressCourse.PRODUCT_SLUG
import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.enums.Status
import `in`.testpress.course.repository.ContentRepository
import `in`.testpress.course.ui.ContentActivity.CONTENT_ID
import `in`.testpress.course.viewmodels.ContentViewModel
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ContentLoadingFragment : Fragment(), EmptyViewListener {
    private var contentId: Long = -1
    lateinit var viewModel: ContentViewModel
    lateinit var fragmentChangeListener: ContentFragmentChangeListener
    private var productSlug: String? = null
    private lateinit var loadingLayout: LinearLayout
    private lateinit var emptyViewFragment: EmptyViewFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ContentViewModel(
                    ContentRepository(requireContext())
                ) as T
            }
        }).get(ContentViewModel::class.java)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (parentFragment != null) {
            fragmentChangeListener = parentFragment as ContentFragmentChangeListener
        } else {
            fragmentChangeListener = context as ContentFragmentChangeListener
        }
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
        initContent()
    }

    private fun parseArguments() {
        contentId = requireArguments().getLong(CONTENT_ID, -1)
        productSlug = requireArguments().getString(PRODUCT_SLUG)
    }

    fun initContent() {
        viewModel.getContent(contentId).observe(viewLifecycleOwner, Observer { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    if (!isContentLoaded(resource.data!!)) {
                        refetchContent(resource.data.id)
                    } else {
                        changeFragment(resource.data)
                    }
                }
                Status.ERROR -> {
                    loadingLayout.visibility = View.GONE
                    emptyViewFragment.displayError(resource.exception!!)
                }
            }
        })
    }

    private fun isContentLoaded(content: DomainContent): Boolean {
        return when(content.contentType) {
            "Exam", "Quiz" -> (content.exam != null) && (content.attemptsUrl != null)
            "Video" -> content.video != null
            "Attachment" -> content.attachment != null
            "Html", "Notes" -> content.htmlContent != null
            else -> true
        }
    }

    private fun refetchContent(id: Long) {
        viewModel.getContent(id, forceRefresh = true)
            .observe(viewLifecycleOwner, Observer { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        changeFragment(resource.data!!)
                    }
                    Status.ERROR -> {
                        loadingLayout.visibility = View.GONE
                        emptyViewFragment.displayError(resource.exception!!)
                    }
                }
            })
    }

    fun initializeEmptyViewFragment() {
        emptyViewFragment = EmptyViewFragment()
        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.empty_view_fragment, emptyViewFragment)
        transaction.commit()
    }

    private fun changeFragment(content: DomainContent) {
        fragmentChangeListener.changeFragment(content)
    }

    override fun onRetryClick() {
        loadingLayout.visibility = View.VISIBLE
        refetchContent(contentId)
    }
}

interface ContentFragmentChangeListener {
    fun changeFragment(content: DomainContent)
}

class ContentFragmentFactory {
    companion object {
        fun getFragment(content: DomainContent): Fragment {
            return when (content.contentType) {
                "Exam" -> ExamContentFragment()
                "Quiz" -> QuizDetailFragment()
                "Video" -> VideoContentFragment()
                "Attachment" -> AttachmentContentFragment()
                "Html" -> HtmlContentFragment()
                "Notes" -> HtmlContentFragment()
                else -> ContentLoadingFragment()
            }
        }
    }
}