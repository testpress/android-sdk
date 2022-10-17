package `in`.testpress.course.ui

import `in`.testpress.course.R
import `in`.testpress.course.repository.BookmarksRepository
import `in`.testpress.course.viewmodels.BookmarkListViewModel
import `in`.testpress.enums.Status
import `in`.testpress.exam.api.TestpressExamApiClient
import `in`.testpress.fragments.EmptyViewFragment
import `in`.testpress.fragments.EmptyViewListener
import `in`.testpress.models.greendao.Bookmark
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout

class BookmarkListFragment : Fragment(), EmptyViewListener {

    val TAG = "BookmarkListFragment"
    private lateinit var apiClient: TestpressExamApiClient
    private var _bookmark = mutableListOf<Bookmark>()
    private val bookmark get() = filteredBookmarks(_bookmark)
    private var folderID = 0L
    private var folder = ""

    lateinit var viewModel: BookmarkListViewModel
    private lateinit var mAdapter: BookmarksListAdapter2
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyViewFragment: EmptyViewFragment
    private lateinit var loadingPlaceholder: ShimmerFrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        apiClient = TestpressExamApiClient(requireActivity())
        initializeViewModel()
    }

    private fun initializeViewModel() {
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return BookmarkListViewModel(
                    BookmarksRepository(
                        requireActivity(),
                        apiClient,
                        folder
                    )
                ) as T
            }
        }).get(BookmarkListViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(`in`.testpress.R.layout.base_list_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews()
        mAdapter = BookmarksListAdapter2(requireActivity(), bookmark, null)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireActivity())
            adapter = mAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
        initalizeObservers()
        viewModel.loadBookmarks()
    }

    private fun initalizeObservers() {
        viewModel.items.observe(viewLifecycleOwner, Observer { resource ->
            when (resource?.status) {
                Status.LOADING -> {
                    Log.d("BookmarkListFragment", "Got status LOADING")
                    showLoadingPlaceholder()
                }
                Status.SUCCESS -> {
                    //showLoadingPlaceholder()
                    Log.d("BookmarkListFragment", "Got status SUCCESS${resource.data?.size}")
                    val items = resource?.data
                    Log.d("Items", "" + items?.size)
                    if (items?.isEmpty()!!) showEmptyList()
                    mAdapter.bookmarks = filteredBookmarks(items)
                    mAdapter.notifyDataSetChanged()
                    hideLoadingPlaceholder()
                }
                Status.ERROR -> {
                    Log.d("BookmarkListFragment", "Got status ERROR")
                    hideLoadingPlaceholder()
                    if (resource.data != null) {
                        mAdapter.bookmarks = filteredBookmarks(resource.data as List<Bookmark>)
                        mAdapter.notifyDataSetChanged()
                    } else {
                        emptyViewFragment.displayError(resource.exception!!)
                    }
                }
            }
        })
    }

    private fun bindViews() {
        recyclerView = view!!.findViewById(R.id.recycler_view)
        loadingPlaceholder = view!!.findViewById(R.id.shimmer_view_container)
        loadingPlaceholder.visibility = View.GONE
        view!!.findViewById<FrameLayout>(R.id.empty_view_fragment).visibility = View.GONE
        initializeEmptyViewFragment()
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun initializeEmptyViewFragment() {
        emptyViewFragment = EmptyViewFragment()
        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.empty_view_fragment, emptyViewFragment)
        transaction.commit()
    }

    private fun showEmptyList() {
        view!!.findViewById<FrameLayout>(R.id.empty_view_fragment).visibility = View.VISIBLE
        emptyViewFragment.setEmptyText(
            R.string.testpress_no_bookmarks,
            R.string.testpress_no_bookmarks_description,
            R.drawable.ic_error_outline_black_18dp
        )
        emptyViewFragment.retryButton.visibility = View.GONE
    }

    override fun onRetryClick() {
        Log.d("onRetryClick", "viewModel load contents")
        viewModel.loadBookmarks()
    }

    private fun showLoadingPlaceholder() {
        loadingPlaceholder.visibility = View.VISIBLE
        loadingPlaceholder.startShimmer()
    }

    private fun hideLoadingPlaceholder() {
        loadingPlaceholder.stopShimmer()
        loadingPlaceholder.visibility = View.GONE
    }

    fun onRefreshing() {
        viewModel.loadBookmarks()
        view!!.findViewById<FrameLayout>(R.id.empty_view_fragment).visibility = View.GONE
    }

    private fun filteredBookmarks(bookmarks: List<Bookmark>): List<Bookmark> {
        if (folderID != 0L) {
            return bookmarks.filter { it.folderId == folderID }
            }
        return bookmarks
    }

    fun setFolderID(folderId:Long){
        this.folderID=folderId
        Log.d(TAG, "setFolderID: $folderId")
        onRefreshing()
    }
}