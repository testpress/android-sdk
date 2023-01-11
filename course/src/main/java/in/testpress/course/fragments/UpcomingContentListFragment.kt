package `in`.testpress.course.fragments

import `in`.testpress.course.R
import `in`.testpress.course.TestpressCourse
import `in`.testpress.course.adapter.RunningContentListAdapter
import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.repository.UpcomingContentRepository
import `in`.testpress.course.viewmodels.UpcomingContentsListViewModel
import `in`.testpress.database.entities.UpcomingContentEntity
import `in`.testpress.databinding.RunningContentListLayoutBinding
import `in`.testpress.enums.Status
import `in`.testpress.fragments.EmptyViewFragment
import `in`.testpress.fragments.EmptyViewListener
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.facebook.shimmer.ShimmerFrameLayout

class UpcomingContentListFragment: Fragment(), EmptyViewListener {
    private lateinit var binding : RunningContentListLayoutBinding
    private var courseId: Long = -1
    private lateinit var viewModel : UpcomingContentsListViewModel
    private lateinit var mAdapter: RunningContentListAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyViewFragment: EmptyViewFragment
    private lateinit var loadingPlaceholder: ShimmerFrameLayout
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parseArguments()
        initializeViewModel()
    }

    private fun parseArguments() {
        courseId = arguments!!.getString(TestpressCourse.COURSE_ID)?.toLong()!!
    }

    private fun initializeViewModel() {
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return UpcomingContentsListViewModel(UpcomingContentRepository(requireContext(), courseId)) as T
            }
        }).get(UpcomingContentsListViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = RunningContentListLayoutBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews()
        mAdapter = RunningContentListAdapter()
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
        initializeObservers()
        viewModel.loadContents()
        swipeRefreshLayout.setOnRefreshListener { viewModel.loadContents() }
    }

    private fun bindViews() {
        recyclerView = binding.recyclerView
        loadingPlaceholder = binding.shimmerViewContainer
        loadingPlaceholder.visibility = View.GONE
        initializeEmptyViewFragment()
        swipeRefreshLayout = binding.swipeRunningContentContainer
    }

    private fun initializeEmptyViewFragment() {
        emptyViewFragment = EmptyViewFragment()
        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.empty_view_fragment, emptyViewFragment)
        transaction.commit()
    }

    private fun initializeObservers() {

        viewModel.items.observe(viewLifecycleOwner, Observer { resource ->
            when (resource?.status) {
                Status.LOADING -> {
                    showLoadingPlaceholder()
                }
                Status.SUCCESS -> {
                    hideLoadingPlaceholder()
                    val items = resource.data!! as List<DomainContent>
                    Log.d("Items", "" + items.isEmpty())
                    if (items.isEmpty()) showEmptyList()
                    mAdapter.contents = items
                    mAdapter.notifyDataSetChanged()
                    swipeRefreshLayout.isRefreshing = false
                }
                Status.ERROR -> {
                    hideLoadingPlaceholder()
                    if (resource.data != null) {
                        mAdapter.contents = resource.data as List<DomainContent>
                        mAdapter.notifyDataSetChanged()
                    } else {
                        swipeRefreshLayout.isRefreshing = false
                        emptyViewFragment.displayError(resource.exception!!)
                    }
                }
            }
        })
    }

    private fun showEmptyList() {
        emptyViewFragment.setEmptyText(
            R.string.testpress_no_running_content,
            R.string.testpress_no_running_content_description,
            R.drawable.ic_error_outline_black_18dp
        )
    }

    override fun onRetryClick() {
        Log.d("onRetryClick", "viewModel load contents")
        viewModel.loadContents()
    }

    private fun showLoadingPlaceholder() {
        loadingPlaceholder.visibility = View.VISIBLE
        loadingPlaceholder.startShimmer()
    }

    private fun hideLoadingPlaceholder() {
        loadingPlaceholder.stopShimmer()
        loadingPlaceholder.visibility = View.GONE
    }
}