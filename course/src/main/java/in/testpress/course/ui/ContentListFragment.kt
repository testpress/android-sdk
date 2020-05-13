package `in`.testpress.course.ui

import `in`.testpress.core.TestpressException
import `in`.testpress.core.TestpressSDKDatabase
import `in`.testpress.course.R
import `in`.testpress.course.TestpressCourse
import `in`.testpress.course.api.TestpressCourseApiClient
import `in`.testpress.course.enums.Status
import `in`.testpress.course.repository.ContentsRepository
import `in`.testpress.course.viewmodels.ContentsListViewModel
import `in`.testpress.fragments.EmptyViewListener
import `in`.testpress.models.greendao.Content
import `in`.testpress.models.greendao.ContentDao
import `in`.testpress.ui.BaseListViewFragmentV2
import `in`.testpress.util.SingleTypeAdapter
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar

class ContentListFragment: BaseListViewFragmentV2<Content>(), EmptyViewListener {
    companion object{
        const val CONTENTS_URL_FRAG = "contentsUrlFrag"
        const val CHAPTER_ID = "chapterId"
    }

    private lateinit var apiClient: TestpressCourseApiClient
    private lateinit var contentsURL: String
    private var chapterId: Long = -1
    private var productSlug: String? = null
    private lateinit var contentDao: ContentDao
    private lateinit var viewModel: ContentsListViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parseArguments()
        initializeApiClientAndDao()
        initializeViewModel()
    }

    private fun parseArguments() {
        contentsURL = arguments!!.getString(CONTENTS_URL_FRAG)!!
        chapterId = arguments!!.getLong(CHAPTER_ID)
        productSlug = arguments!!.getString(TestpressCourse.PRODUCT_SLUG)
    }

    private fun initializeApiClientAndDao() {
        apiClient = TestpressCourseApiClient(activity)
        contentDao = TestpressSDKDatabase.getContentDao(requireContext())
    }

    private fun initializeViewModel() {
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ContentsListViewModel(ContentsRepository(requireContext(), chapterId)) as T
            }
        }).get(ContentsListViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initalizeObservers()
        viewModel.loadContents()
    }

    private fun initalizeObservers() {
        if (isItemsEmpty()) {
            swipeRefreshLayout.isRefreshing = true
        }

        viewModel.items.observe(viewLifecycleOwner, Observer { resource ->
            when(resource?.status) {
                Status.SUCCESS -> {
                    swipeRefreshLayout.isRefreshing = false
                    items = resource.data!! as List<Content>
                    getListAdapter().notifyDataSetChanged()
                }
                Status.ERROR -> {
                    swipeRefreshLayout.isRefreshing = false
                    val message = getErrorMessage(resource.exception)
                    emptyViewFragment.displayError(resource.exception!!)
                    Snackbar.make(swipeRefreshLayout, message, Snackbar.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun getErrorMessage(exception: TestpressException?): Int {
        when(exception != null) {
            exception!!.isUnauthenticated -> R.string.testpress_authentication_failed
            exception!!.isNetworkError -> R.string.testpress_no_internet_try_again
            else -> R.string.testpress_some_thing_went_wrong_try_again
        }
        return R.string.testpress_some_thing_went_wrong_try_again
    }

    override fun isItemsEmpty(): Boolean {
        return contentDao.queryBuilder()
            .where(ContentDao.Properties.ChapterId.eq(chapterId))
            .list().isEmpty()
    }

    override fun createAdapter(items: List<Content>): SingleTypeAdapter<Content> {
        return ContentsListAdapter(activity, chapterId, productSlug)
    }

    override fun refreshWithProgress() {
        swipeRefreshLayout.isRefreshing = true
        viewModel.loadContents()
    }

    override fun onRetryClick() {
        refreshWithProgress()
    }
}