package `in`.testpress.course.fragments

import `in`.testpress.course.R
import `in`.testpress.course.TestpressCourse
import `in`.testpress.course.adapter.BaseListFooterAdapter
import `in`.testpress.course.adapter.RunningContentListAdapter
import `in`.testpress.course.databinding.BaseContentListLayoutBinding
import `in`.testpress.course.repository.RunningContentsRepository
import `in`.testpress.course.viewmodels.RunningContentsListViewModel
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.collect

class RunningContentListFragment : Fragment() {

    private var courseId: Long = -1
    private lateinit var binding: BaseContentListLayoutBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyContainer: LinearLayout
    private lateinit var adapter: RunningContentListAdapter
    private lateinit var viewModel: RunningContentsListViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        parseArguments()
        initializeViewModel()
        binding = BaseContentListLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun parseArguments() {
        courseId = arguments!!.getString(TestpressCourse.COURSE_ID)?.toLong()!!
    }

    private fun initializeViewModel() {
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return RunningContentsListViewModel(
                    RunningContentsRepository(
                        requireContext(),
                        courseId
                    )
                ) as T
            }
        }).get(RunningContentsListViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews()
        initializeListeners()
        initializeAdapter()
        initializeListView()
        initializeFooterView()
        bindListView()
    }

    private fun bindViews() {
        binding.apply {
            this@RunningContentListFragment.recyclerView = binding.recyclerView
            this@RunningContentListFragment.emptyContainer = binding.errorContainer
        }
    }

    private fun initializeListeners() {
        binding.retryButton.setOnClickListener {
            adapter.retry()
        }
    }

    private fun initializeAdapter() {
        adapter = RunningContentListAdapter()
        lifecycleScope.launchWhenCreated {
            viewModel.runningContentList.collect {
                adapter.submitData(it)
            }
        }
    }

    private fun initializeListView() {
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            adapter = this@RunningContentListFragment.adapter
        }
    }

    private fun initializeFooterView() {
        recyclerView.adapter = adapter.withLoadStateFooter(
            BaseListFooterAdapter {
                adapter.retry()
            }
        )
    }

    private fun bindListView() {
        binding.apply {
            lifecycleScope.launchWhenCreated {
                adapter.loadStateFlow.collect {
                    val notLoading = it.refresh is LoadState.NotLoading
                    val loading = it.refresh is LoadState.Loading
                    val error = it.refresh is LoadState.Error
                    val isItemCountZero = adapter.itemCount == 0

                    Log.d("TAG", "bindListView: $notLoading $loading $error $isItemCountZero")

                    shimmerViewContainer.isVisible = loading
                    recyclerView.isVisible = (notLoading || error) && adapter.itemCount != 0
                    showEmptyOrErrorMessage(
                        notLoading && isItemCountZero,
                        error && isItemCountZero
                    )
                }
            }
        }
    }

    private fun showEmptyOrErrorMessage(emptyCondition: Boolean, errorCondition: Boolean) {
        if (emptyCondition) showEmptyList()
        if (errorCondition) showNetworkErrorMessage()
        binding.errorContainer.isVisible = emptyCondition || errorCondition
    }

    private fun showEmptyList() {
        binding.apply {
            errorTitle.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_error_outline_black_18dp,
                0,
                0,
                0
            )
            errorTitle.text = resources.getString(R.string.testpress_no_content)
            errorDescription.text = resources.getString(R.string.testpress_no_running_contents)
            retryButton.isVisible = false
        }
    }

    private fun showNetworkErrorMessage() {
        binding.apply {
            errorTitle.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_error_outline_black_18dp,
                0,
                0,
                0
            )
            errorTitle.text = resources.getString(`in`.testpress.R.string.testpress_network_error)
            errorDescription.text =
                resources.getString(`in`.testpress.R.string.testpress_content_not_available_description)
            retryButton.isVisible = true
        }
    }
}