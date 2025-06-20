package `in`.testpress.course.fragments

import `in`.testpress.core.TestpressSDKDatabase
import `in`.testpress.course.R
import `in`.testpress.course.TestpressCourse.PRODUCT_SLUG
import `in`.testpress.course.domain.DomainContent
import `in`.testpress.enums.Status
import `in`.testpress.course.repository.ContentRepository
import `in`.testpress.course.ui.ContentActivity.CONTENT_ID
import `in`.testpress.course.viewmodels.ContentViewModel
import `in`.testpress.fragments.EmptyViewFragment
import `in`.testpress.fragments.EmptyViewListener
import `in`.testpress.models.greendao.CourseAttempt
import `in`.testpress.models.greendao.CourseAttemptDao
import `in`.testpress.models.greendao.CourseDao
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

class ContentLoadingFragment : Fragment(),
    EmptyViewListener {
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
                        refetchContent(resource.data!!.id)
                    } else {
                        changeFragment(resource.data!!)
                    }
                }
                Status.ERROR -> {
                    loadingLayout.visibility = View.GONE
                    emptyViewFragment.displayError(resource.exception!!)
                }
                else -> {}
            }
        })
    }

    private fun isContentLoaded(content: DomainContent): Boolean {
        if(content.isLocked == true) return false
        return when(content.contentType) {
            "Exam" -> (content.exam != null)
            "Video" -> content.video != null && content.video.isTranscodingStatusComplete() && !hasCourseVideoViewsLimit(content)
            "Attachment" -> content.attachment != null
            "Html", "Notes" -> content.htmlContent != null
            "Quiz" -> content.exam != null
            else -> true
        }
    }

    private fun hasCourseVideoViewsLimit(content: DomainContent): Boolean {
        val courseDao = TestpressSDKDatabase.getCourseDao(requireContext())
        val course =
            courseDao.queryBuilder().where(CourseDao.Properties.Id.eq(content.courseId)).list()
        val maxAllowedViewsPerVideo: Int? = course[0].maxAllowedViewsPerVideo
        return maxAllowedViewsPerVideo != null && maxAllowedViewsPerVideo > 0
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
                    else -> {}
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

open class ContentFragmentFactory {
    companion object {
        fun getFragment(content: DomainContent): Fragment {
            return when (content.contentType) {
                "Exam" -> ExamContentFragment()
                "Quiz" -> QuizContentFragment()
                "Video" -> VideoContentFragment()
                "Attachment" -> {
                    if (content.attachment?.isRenderable == true) {
                        return DocumentViewerFragment()
                    }
                    return AttachmentContentFragment()
                }
                "Html" -> HtmlContentFragment()
                "Notes" -> HtmlContentFragment()
                "VideoConference" -> {
                    if (content.canShowRecordedVideo()) {
                        return VideoContentFragment()
                    }
                    return VideoConferenceFragment()
                }
                "Live Stream" -> {
                    if (content.canShowRecordedVideo()) {
                        return VideoContentFragment()
                    }
                    return LiveStreamFragment()
                }
                else -> ContentLoadingFragment()
            }
        }
    }
}
