package `in`.testpress.course.fragments

import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.R
import `in`.testpress.course.TestpressCourse
import `in`.testpress.course.TestpressCourse.CONTENT_TYPE
import `in`.testpress.course.TestpressCourse.PRODUCT_SLUG
import `in`.testpress.course.di.InjectorUtils
import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.ui.ContentActivity
import `in`.testpress.enums.Status
import `in`.testpress.course.viewmodels.ContentViewModel
import `in`.testpress.fragments.EmptyViewFragment
import `in`.testpress.fragments.EmptyViewListener
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import `in`.testpress.course.ui.ContentActivity.*

abstract class BaseContentDetailFragment : Fragment(), BookmarkListener, ContentActivity.OnBackPressedListener,
    EmptyViewListener {
    protected lateinit var swipeRefresh: SwipeRefreshLayout
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    lateinit var emptyViewFragment: EmptyViewFragment
    private lateinit var toast: Toast
    private lateinit var contentView: RelativeLayout
    lateinit var bottomNavigationFragment: ContentBottomNavigationFragment

    protected var contentId: Long = -1
    private var productSlug: String? = null
    open var isBookmarkEnabled = true
    protected lateinit var content: DomainContent
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    lateinit var bookmarkFragment: BookmarkFragment
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    open lateinit var viewModel: ContentViewModel
    private var hideBottomNavigation: Boolean = false
    private var forceReloadContent: Boolean = false

    override val bookmarkId: Long?
        get() = if (!::content.isInitialized) null else content.bookmarkId
    override val bookmarkContentId: Long?
        get() = content.id

    protected fun isContentInitialized() = ::content.isInitialized

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val contentType = requireArguments().getString(CONTENT_TYPE)
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ContentViewModel(InjectorUtils.getContentRepository(contentType!!, context!!)) as T
            }
        }).get(ContentViewModel::class.java)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews()
        parseIntentArguments()
        initializeListenters()
        initializeEmptyViewFragment()
        loadContentAndInitializeBoomarkFragment()
        if(!hideBottomNavigation) {
            initBottomNavigationFragment()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val intent = Intent()
        intent.putExtra(TestpressSdk.ACTION_PRESSED_HOME, true)

        if (::content.isInitialized) {
            intent.putExtra(TestpressCourse.CHAPTER_URL, content.chapterUrl)
        }
        requireActivity().setResult(Activity.RESULT_CANCELED, intent)
        requireActivity().finish()
        return false
    }


    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun loadContentAndInitializeBoomarkFragment() {
        viewModel.getContent(contentId, forceReloadContent).observe(viewLifecycleOwner, Observer { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    content = resource.data!!
                    display()

                    if (isBookmarkEnabled && !::bookmarkFragment.isInitialized) {
                        initializeBookmarkFragment()
                    }
                }
                else -> {}
            }
        })
    }

    private fun bindViews() {
        contentView = requireView().findViewById(R.id.main_content)
        toast = Toast.makeText(activity, R.string.testpress_no_internet_try_again, Toast.LENGTH_SHORT)
        swipeRefresh = requireView().findViewById(R.id.swipe_container)
        swipeRefresh.setColorSchemeResources(R.color.testpress_color_primary)
    }

    private fun initializeListenters() {
        swipeRefresh.setOnRefreshListener {
            forceReloadContent()
        }
    }

    private fun parseIntentArguments() {
        contentId = requireArguments().getLong(CONTENT_ID, -1)
        productSlug = requireArguments().getString(PRODUCT_SLUG)
        hideBottomNavigation = requireArguments().getBoolean(HIDE_BOTTOM_NAVIGATION, false)
        forceReloadContent = requireArguments().getBoolean(FORCE_REFRESH_CONTENT, false)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    fun forceReloadContent(onSuccessCallback: (() -> Unit) = {}) {
        swipeRefresh.isRefreshing = true
        viewModel.getContent(contentId, forceRefresh = true).observe(viewLifecycleOwner,
            Observer { resource ->
                swipeRefresh.isRefreshing = false
                if (resource != null) {
                    when (resource.status) {
                        Status.SUCCESS -> {
                            content = resource.data!!
                            display()
                            onSuccessCallback.invoke()
                        }
                        Status.ERROR -> {
                            toast.show()
                        }
                        else -> {}
                    }
                }
            })
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun initializeBookmarkFragment() {
        arguments?.putLong(CONTENT_ID, contentId)
        bookmarkFragment = BookmarkFragment()
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
        bottomNavigationFragment = ContentBottomNavigationFragment()
        bottomNavigationFragment.arguments = arguments
        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.bottom_navigation_fragment, bottomNavigationFragment)
        transaction.commit()
    }

    protected fun checkAndUnlockNextContent() {
        if (content.nextContentId != null) {
            viewModel.getContent(content.nextContentId!!, forceRefresh = true).observe(viewLifecycleOwner, Observer {
                when(it.status) {
                    Status.SUCCESS -> bottomNavigationFragment.initializeAndShowNavigationButtons()
                    else -> {}
                }
            })
        }
    }

    override fun onBookmarkSuccess(bookmarkId: Long?) {
        content.bookmarkId = bookmarkId
        viewModel.storeBookmarkIdToContent(bookmarkId, contentId)
    }

    override fun onDeleteBookmarkSuccess() {
        content.bookmarkId = null
        viewModel.storeBookmarkIdToContent(null, contentId)
    }

    override fun onRetryClick() {
        forceReloadContent()
    }

    override fun onBackPressed() {
        // ChapterDetailActivity will force refresh content list if FORCE_REFRESH is true in preference
        val prefs: SharedPreferences = requireActivity().getSharedPreferences(
                ContentActivity.TESTPRESS_CONTENT_SHARED_PREFS,
                Context.MODE_PRIVATE
        )
        prefs.edit().putBoolean(ContentActivity.FORCE_REFRESH, true).apply()
    }

    abstract fun display()
}