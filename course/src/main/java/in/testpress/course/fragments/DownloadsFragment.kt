package `in`.testpress.course.fragments

import `in`.testpress.course.R
import `in`.testpress.course.enums.Status
import `in`.testpress.course.helpers.CourseRefreshDate
import `in`.testpress.course.helpers.DownloadedVideoRemoveHandler
import `in`.testpress.course.repository.CourseRepository
import `in`.testpress.course.repository.OfflineVideoRepository
import `in`.testpress.course.services.VideoDownloadService
import `in`.testpress.course.ui.OfflineVideoListAdapter
import `in`.testpress.course.util.CourseApplication
import `in`.testpress.course.viewmodels.CourseViewModel
import `in`.testpress.course.viewmodels.OfflineVideoViewModel
import `in`.testpress.fragments.EmptyViewFragment
import `in`.testpress.fragments.EmptyViewListener
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout

class DownloadsFragment : Fragment(), EmptyViewListener {
    private val TAG = "DownloadsFragment"
    private val viewModel by lazy {
        ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return OfflineVideoViewModel(OfflineVideoRepository(requireContext())) as T
            }
        }).get(OfflineVideoViewModel::class.java)
    }
    private val courseViewModel by lazy {
        ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return CourseViewModel(CourseRepository(requireContext())) as T
            }
        }).get(CourseViewModel::class.java)
    }
    private lateinit var courseRefreshDate: CourseRefreshDate
    private lateinit var emptyViewFragment: EmptyViewFragment
    private lateinit var recyclerView: RecyclerView
    private lateinit var loadingPlaceholder: ShimmerFrameLayout
    private lateinit var adapter: OfflineVideoListAdapter
    private lateinit var courseApplication: CourseApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        VideoDownloadService.start(requireContext())
        courseRefreshDate = CourseRefreshDate(requireContext())
        courseApplication = requireContext().applicationContext as CourseApplication
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.base_list_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        initializeRecyclerView()
        initializeObservers()
        initializeCourseRefreshStatusObserver()
    }

    private fun bindViews(view: View) {
        recyclerView = view.findViewById(R.id.recycler_view)
        loadingPlaceholder = view.findViewById(R.id.shimmer_view_container)
        initializeEmptyViewFragment()
    }

    private fun initializeEmptyViewFragment() {
        emptyViewFragment = EmptyViewFragment()
        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.empty_view_fragment, emptyViewFragment)
        transaction.commit()
    }

    private fun initializeRecyclerView() {
        adapter = OfflineVideoListAdapter()
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@DownloadsFragment.adapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
    }

    private fun initializeObservers() {
        showLoadingPlaceholder()
        viewModel.offlineVideos.observe(viewLifecycleOwner, Observer {
            val handler = DownloadedVideoRemoveHandler(it, requireContext())
            if (handler.hasVideosToRemove()) {
                handler.remove()
            }

            hideLoadingPlaceholder()
            if (courseApplication.isAutoTimeDisabledInDevice()) {
                showInCorrectDateScreen()
            }else if (courseRefreshDate.hasNotUpdated()) {
                showRefreshScreen()
            } else if (it.isEmpty()) {
                showEmptyScreen()
            } else {
                adapter.offlineVideos = it
                adapter.notifyDataSetChanged()
            }
        })
    }

    private fun initializeCourseRefreshStatusObserver() {
        courseViewModel.courses.observe(viewLifecycleOwner, Observer {
            hideLoadingPlaceholder()
            when (it.status) {
                Status.ERROR -> {
                    showRefreshScreen()
                    Toast.makeText(
                        requireContext(),
                        "Network Error. Please try again",
                        Toast.LENGTH_LONG
                    ).show()
                }
                Status.SUCCESS -> checkCourseRefreshDateAndDisplay()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        checkCourseRefreshDateAndDisplay()
    }

    private fun checkCourseRefreshDateAndDisplay() {
        when {
            courseApplication.isAutoTimeDisabledInDevice() -> showInCorrectDateScreen()
            courseRefreshDate.hasNotUpdated() -> showRefreshScreen()
            else -> {
                if(adapter.offlineVideos.isNotEmpty()) {
                    hideEmptyScreen()
                } else {
                    showEmptyScreen()
                }
            }
        }
    }

    private fun hideEmptyScreen() {
        emptyViewFragment.hide()
    }

    private fun showEmptyScreen() {
        emptyViewFragment.setEmptyText(R.string.nothing_here, R.string.no_downloads, null)
        emptyViewFragment.setImage(R.drawable.ic_empty_video)
        emptyViewFragment.showOrHideButton(false)
    }

    private fun showRefreshScreen() {
        emptyViewFragment.setEmptyText(R.string.refresh_videos, R.string.refresh_videos_description, null)
        emptyViewFragment.setImage(R.drawable.ic_empty_video)
    }

    private fun showInCorrectDateScreen() {
        emptyViewFragment.setEmptyText(
            R.string.auto_time_disabled,
            R.string.enable_auto_time_description,
            null
        )
        emptyViewFragment.setImage(R.drawable.ic_empty_video)
        emptyViewFragment.showOrHideButton(false)
    }

    private fun showLoadingPlaceholder() {
        loadingPlaceholder.visibility = View.VISIBLE
        loadingPlaceholder.startShimmer()
    }

    private fun hideLoadingPlaceholder() {
        loadingPlaceholder.stopShimmer()
        loadingPlaceholder.visibility = View.GONE
    }

    override fun onRetryClick() {
        courseViewModel.load()
        showLoadingPlaceholder()
    }
}