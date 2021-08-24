package `in`.testpress.course.fragments

import `in`.testpress.course.R
import `in`.testpress.course.helpers.CourseLastSyncedDate
import `in`.testpress.course.helpers.DownloadedVideoRemoveHandler
import `in`.testpress.course.repository.CourseRepository
import `in`.testpress.course.repository.OfflineVideoRepository
import `in`.testpress.course.repository.VideoWatchDataRepository
import `in`.testpress.course.services.VideoDownloadService
import `in`.testpress.course.ui.OfflineVideoListAdapter
import `in`.testpress.course.util.DateUtils
import `in`.testpress.course.viewmodels.CourseViewModel
import `in`.testpress.course.viewmodels.OfflineVideoViewModel
import `in`.testpress.database.TestpressDatabase
import `in`.testpress.enums.Status
import `in`.testpress.fragments.EmptyViewFragment
import `in`.testpress.fragments.EmptyViewListener
import `in`.testpress.util.Extensions.startRotation
import `in`.testpress.util.Misc.hasNetworkAvailable
import `in`.testpress.util.UIUtils.showAlert
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import kotlinx.coroutines.*
import java.util.*
import kotlin.concurrent.schedule


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
    private lateinit var courseLastSyncedDate: CourseLastSyncedDate
    private lateinit var emptyViewFragment: EmptyViewFragment
    private lateinit var recyclerView: RecyclerView
    private lateinit var loadingPlaceholder: ShimmerFrameLayout
    private lateinit var adapter: OfflineVideoListAdapter
    private lateinit var menu: Menu
    private lateinit var job: Job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        VideoDownloadService.start(requireContext())
        courseLastSyncedDate = CourseLastSyncedDate(requireContext())
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.downloads_fragment_menu, menu)
        this.menu = menu

        if (hasNetworkAvailable(requireContext())) {
            menu.findItem(R.id.sync).isVisible = true
        }

        menu.findItem(R.id.sync).actionView.setOnClickListener {
            if (job.isActive) {
                showAlert(requireContext(), "Syncing Video", "Video watched information is being synced with server.")
            } else {
                syncVideoWatchData()
            }
        }
        syncVideoWatchData()
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
            if (DateUtils.isAutoTimeUpdateDisabledInDevice(requireContext())) {
                showEnableAutoTimeUpdateScreen()
            } else if (courseLastSyncedDate.hasExpired()) {
                showRefreshScreen()
            } else if (it.isEmpty()) {
                showEmptyScreen()
            } else {
                hideEmptyScreen()
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
            DateUtils.isAutoTimeUpdateDisabledInDevice(requireContext()) -> showEnableAutoTimeUpdateScreen()
            courseLastSyncedDate.hasExpired() -> showRefreshScreen()
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

    private fun showEnableAutoTimeUpdateScreen() {
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

    private fun syncVideoWatchData() {
        job = viewModel.viewModelScope.launch(Dispatchers.IO) {
            val refreshItem = menu.findItem(R.id.sync)
            refreshItem.actionView.startRotation()

            VideoWatchDataRepository(requireContext(), TestpressDatabase(requireContext()).offlineVideoDao()).sync()
            launch(Dispatchers.Main) {
                refreshItem.actionView.clearAnimation()
            }
        }
    }

    override fun onRetryClick() {
        courseViewModel.load()
        showLoadingPlaceholder()
    }
}