package `in`.testpress.course.fragments

import `in`.testpress.course.TestpressCourse
import `in`.testpress.course.databinding.ContentStateListLayoutBinding
import `in`.testpress.course.databinding.UpcomingContentListViewBinding
import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.repository.UpcomingContentRepository
import `in`.testpress.course.viewmodels.UpcomingContentsListViewModel
import `in`.testpress.enums.Status
import `in`.testpress.fragments.EmptyViewListener
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ExpandableListView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class UpcomingContentListFragment(fragmentTag: String): BaseContentStateListFragment(fragmentTag), EmptyViewListener {
    private lateinit var viewModel : UpcomingContentsListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeViewModel()
    }

    private fun initializeViewModel() {
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return UpcomingContentsListViewModel(UpcomingContentRepository(requireContext(), courseId)) as T
            }
        }).get(UpcomingContentsListViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeObservers()
        viewModel.loadContents()
        swipeRefreshLayout.setOnRefreshListener { viewModel.loadContents() }
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
                    if (items.isEmpty()) showEmptyList("There are no currently available contents for you.")
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

    override fun onRetryClick() {
        Log.d("onRetryClick", "viewModel load contents")
        viewModel.loadContents()
    }
}