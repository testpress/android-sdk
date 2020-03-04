package `in`.testpress.course.fragments

import `in`.testpress.course.R
import `in`.testpress.course.TestpressCourse.PRODUCT_SLUG
import `in`.testpress.course.di.InjectorUtils
import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.enums.Status
import `in`.testpress.course.network.Resource
import `in`.testpress.course.ui.ContentActivity.CHAPTER_ID
import `in`.testpress.course.ui.ContentActivity.CONTENT_ID
import `in`.testpress.course.ui.ContentActivity.POSITION
import `in`.testpress.course.viewmodels.ContentViewModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

abstract class BaseContentDetailFragment : Fragment(), BookmarkListener {
    protected lateinit var swipeRefresh: SwipeRefreshLayout
    protected lateinit var emptyViewFragment: EmptyViewFragment
    private lateinit var toast: Toast
    private lateinit var contentView: RelativeLayout

    private var chapterId: Long = -1
    protected var contentId: Long = -1
    private var productSlug: String? = null
    open var isBookmarkEnabled = true
    private var position: Int = -1
    protected lateinit var content: DomainContent
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    open lateinit var viewModel: ContentViewModel

    override val bookmarkId: Long?
        get() = content.bookmarkId
    override val bookmarkContentId: Long?
        get() = content.id

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ContentViewModel(InjectorUtils.getContentRepository(context!!)) as T
            }
        }).get(ContentViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.base_content_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews()
        parseIntentArguments()
        initializeListenters()
        initializeEmptyViewFragment()
        loadContentAndInitializeBoomarkFragment()
        initBottomNavigationFragment()
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun loadContentAndInitializeBoomarkFragment() {
        val onContentLoad = Observer<Resource<DomainContent>> { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    content = resource.data!!
                    contentId = content.id
                    display()
                    initializeBookmarkFragment()
                }
            }
        }

        if (contentId != -1L) {
            viewModel.getContent(contentId).observe(viewLifecycleOwner, onContentLoad)
        } else {
            content = viewModel.getContentInChapterForPosition(position, chapterId)
            contentId = content.id
            initBookmarkFragmentIfEnabled()
        }
    }

    private fun initBookmarkFragmentIfEnabled() {
        if (isBookmarkEnabled) {
            initializeBookmarkFragment()
        }
    }

    private fun bindViews() {
        contentView = view!!.findViewById(R.id.main_content)
        toast = Toast.makeText(activity, R.string.testpress_no_internet_try_again, Toast.LENGTH_SHORT)
        swipeRefresh = view!!.findViewById(R.id.swipe_container)
        swipeRefresh.setColorSchemeResources(R.color.testpress_color_primary)
    }

    private fun initializeListenters() {
        swipeRefresh.setOnRefreshListener {
            updateContent()
        }
    }

    private fun parseIntentArguments() {
        chapterId = arguments!!.getLong(CHAPTER_ID)
        contentId = arguments!!.getLong(CONTENT_ID, -1)
        position = arguments!!.getInt(POSITION)
        productSlug = arguments!!.getString(PRODUCT_SLUG)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    fun updateContent() {
        viewModel.getContent(contentId, forceRefresh = true).observe(viewLifecycleOwner,
            Observer { resource ->
                swipeRefresh.isRefreshing = false
                if (resource != null) {
                    when (resource.status) {
                        Status.SUCCESS -> {
                            content = resource.data!!
                            display()
                        }
                        Status.ERROR -> {
                            toast.show()
                        }
                    }
                }
            })
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun initializeBookmarkFragment() {
        val bookmarkFragment = BookmarkFragment()
        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.bookmark_fragment_layout, bookmarkFragment)
        transaction.commit()
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun initializeEmptyViewFragment() {
        emptyViewFragment = EmptyViewFragment()
        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.empty_view_fragment, emptyViewFragment)
        transaction.commit()
    }

    private fun initBottomNavigationFragment() {
        arguments?.putLong(CONTENT_ID, content.id)
        val bottomNavigationFragment = ContentBottomNavigationFragment()
        bottomNavigationFragment.arguments = arguments
        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.bottom_navigation_fragment, bottomNavigationFragment)
        transaction.commit()
    }

    override fun onBookmarkSuccess(bookmarkId: Long?) {
        content.bookmarkId = bookmarkId
        viewModel.storeBookmarkIdToContent(bookmarkId, contentId)
    }

    override fun onDeleteBookmarkSuccess() {
        content.bookmarkId = null
        viewModel.storeBookmarkIdToContent(null, contentId)
    }

    abstract fun display()
}