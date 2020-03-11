package `in`.testpress.course.fragments

import `in`.testpress.course.R
import `in`.testpress.course.TestpressCourse.PRODUCT_SLUG
import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.enums.Status
import `in`.testpress.course.repository.ContentRepository
import `in`.testpress.course.ui.ContentActivity.CHAPTER_ID
import `in`.testpress.course.ui.ContentActivity.CONTENT_ID
import `in`.testpress.course.ui.ContentActivity.POSITION
import `in`.testpress.course.viewmodels.ContentViewModel
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ContentLoadingFragment : Fragment(), EmptyViewListener {
    private var position: Int = -1
    private var chapterId: Long = -1
    private var contentId: Long = -1
    lateinit var viewModel: ContentViewModel
    lateinit var fragmentChangeListener: ContentFragmentChangeListener
    private var productSlug: String? = null
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
        parseArguments()
        initializeEmptyViewFragment()
        initContent()
    }

    private fun parseArguments() {
        position = requireArguments().getInt(POSITION)
        chapterId = requireArguments().getLong(CHAPTER_ID)
        contentId = requireArguments().getLong(CONTENT_ID, -1)
        productSlug = requireArguments().getString(PRODUCT_SLUG)
    }

    fun initContent() {
        if (contentId != -1L) {
            viewModel.getContent(contentId).observe(viewLifecycleOwner, Observer { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        if (!isContentLoaded(resource.data!!)) {
                            refetchContent(resource.data.id)
                        } else {
                            changeFragment(resource.data)
                        }
                    }
                    Status.ERROR -> emptyViewFragment.displayError(resource.exception!!)
                }
            })
        } else {
            viewModel.getContentInChapterForPosition(position, chapterId)
                .observe(viewLifecycleOwner, Observer { content ->
                    contentId = content.id
                    if (!isContentLoaded(content)) {
                        refetchContent(content.id)
                    } else {
                        changeFragment(content)
                    }
                })
        }
    }

    private fun isContentLoaded(content: DomainContent): Boolean {
        if ((content.exam != null && content.attemptsUrl != null) || content.video != null || content.htmlContent != null || content.attachment != null) {
            return true
        }
        return false
    }

    private fun refetchContent(id: Long) {
        viewModel.getContent(id, forceRefresh = true)
            .observe(viewLifecycleOwner, Observer { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        changeFragment(resource.data!!)
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
                "Quiz" -> ExamContentFragment()
                "Video" -> VideoContentFragment()
                "Attachment" -> AttachmentContentFragment()
                "Html" -> HtmlContentFragment()
                "Notes" -> HtmlContentFragment()
                else -> ContentLoadingFragment()
            }
        }
    }
}