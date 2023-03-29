package `in`.testpress.course.fragments

import `in`.testpress.course.R
import `in`.testpress.course.TestpressCourse
import `in`.testpress.course.adapter.BaseListFooterAdapter
import `in`.testpress.course.adapter.RunningContentListAdapter
import `in`.testpress.course.databinding.BaseContentListLayoutBinding
import `in`.testpress.course.repository.RunningContentsRepository
import `in`.testpress.course.viewmodels.RunningContentsListViewModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.collect

class RunningContentListFragment : Fragment() {

    private var courseId: Long = -1
    private lateinit var binding: BaseContentListLayoutBinding
    private lateinit var adapter: RunningContentListAdapter
    private lateinit var viewModel: RunningContentsListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parseArguments()
        initializeViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
        initializeListView()
        initializeListeners()
        updateListView()
    }

    private fun initializeListView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            adapter = this@RunningContentListFragment.getAdapter()
        }
        binding.recyclerView.adapter = adapter.withLoadStateFooter(
            BaseListFooterAdapter {
                adapter.retry()
            }
        )
    }

    private fun getAdapter(): RunningContentListAdapter {
        adapter = RunningContentListAdapter()
        lifecycleScope.launchWhenCreated {
            viewModel.runningContentList.collect {
                adapter.submitData(it)
            }
        }
        return adapter
    }

    private fun initializeListeners() {
        binding.retryButton.setOnClickListener {
            adapter.retry()
        }
    }

    private fun updateListView() {
        binding.apply {
            lifecycleScope.launchWhenCreated {
                adapter.loadStateFlow.collect {
                    showOrHidePlaceHolder(it.refresh is LoadState.Loading)
                    showOrHideListView(it.refresh)
                    showEmptyOrErrorMessage(it.refresh)
                }
            }
        }
    }

    private fun showOrHidePlaceHolder(showOrHide: Boolean) {
        binding.shimmerViewContainer.isVisible = showOrHide
    }

    private fun showOrHideListView(loadState: LoadState) {
        val showOrHide =
            (loadState is LoadState.NotLoading || loadState is LoadState.Error) && (adapter.itemCount != 0)
        binding.recyclerView.isVisible = showOrHide
    }

    private fun showEmptyOrErrorMessage(loadState: LoadState) {
        val showEmptyMessage = (loadState is LoadState.NotLoading) && (adapter.itemCount == 0)
        val showErrorMessage = (loadState is LoadState.Error) && (adapter.itemCount == 0)
        if (showEmptyMessage) showEmptyList()
        if (showErrorMessage) showNetworkErrorMessage()
        binding.errorContainer.isVisible = showEmptyMessage || showErrorMessage
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