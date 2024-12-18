package `in`.testpress.ui.fragments

import android.os.Bundle
import android.text.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import `in`.testpress.R
import `in`.testpress.databinding.GlobalSearchFragmentLayoutBinding
import `in`.testpress.network.APIClient
import `in`.testpress.repository.GlobalSearchRepository
import `in`.testpress.ui.adapter.BaseListFooterAdapter
import `in`.testpress.ui.adapter.GlobalSearchAdapter
import `in`.testpress.ui.viewmodel.GlobalSearchViewModel
import `in`.testpress.util.InternetConnectivityChecker
import kotlinx.coroutines.flow.collectLatest

class GlobalSearchFragment : Fragment() {

    private var _binding: GlobalSearchFragmentLayoutBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: GlobalSearchViewModel
    private lateinit var adapter: GlobalSearchAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeViewModel()
    }

    private fun initializeViewModel() {
        viewModel = ViewModelProvider(requireActivity(), object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return GlobalSearchViewModel(
                    GlobalSearchRepository(
                        APIClient(requireContext())
                    )
                ) as T
            }
        }).get(GlobalSearchViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = GlobalSearchFragmentLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.resultsList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            adapter = this@GlobalSearchFragment.getAdapter()
        }
        binding.resultsList.adapter = adapter.withLoadStateFooter(
            BaseListFooterAdapter {
                adapter.retry()
            }
        )

        lifecycleScope.launchWhenCreated {
            adapter.loadStateFlow.collectLatest { loadStates ->

                binding.loadingView.isVisible = loadStates.refresh is LoadState.Loading && adapter.itemCount == 0

                binding.noDataView.visibility = if (loadStates.refresh is LoadState.NotLoading && adapter.itemCount == 0) View.VISIBLE else View.GONE
                if (viewModel.hasQuery() && binding.noDataView.isVisible){
                    binding.noDataView.text = "No data found"
                } else {
                    binding.noDataView.text = "Search or type a command"
                }

                if (loadStates.refresh is LoadState.Error){
                    if (isNetworkError(loadStates.refresh as LoadState.Error)){
                        binding.noDataView.isVisible = true
                        binding.noDataView.text = "Please check your internet connection"
                    }
                }
            }
        }

        // Listen to global query text changes
        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.let {
                    if (it.isNotEmpty()) {
                        viewModel.updateSearchQuery(mapOf("q" to it.toString()))
                    } else {
                        viewModel.updateSearchQuery(mapOf())
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        for (i in 0 until binding.filterChipGroup.childCount) {
            val chip = binding.filterChipGroup.getChildAt(i) as? Chip
            chip?.setOnCheckedChangeListener { buttonView, isChecked ->
                search()
            }
        }
    }

    fun isNetworkError(error: LoadState.Error): Boolean {
        val networkErrorMessage =
            error.error.localizedMessage?.contains("Unable to resolve host", true) ?: false
        return networkErrorMessage && !InternetConnectivityChecker.isConnected(requireContext())
    }

    private fun getAdapter(): GlobalSearchAdapter {
        adapter = GlobalSearchAdapter()
        lifecycleScope.launchWhenCreated {
            viewModel.globalSearchResults.collectLatest {
                adapter.submitData(it)
            }
        }
        return adapter
    }

    private fun search() {
        val selectedIds = mutableListOf<Int>()
        for (i in 0 until binding.filterChipGroup.childCount) {
            val chip = binding.filterChipGroup.getChildAt(i) as? Chip
            if (chip?.isChecked == true) {
                selectedIds.add(chip.id)
            }
        }
        val pair = Pair(getFilterParams(selectedIds), getFilterForContentType(selectedIds))
        viewModel.updateFilterQuery(pair)
    }

    private fun getFilterParams(ids: List<Int>): List<String> {
        val idToParamMap = mapOf(
            R.id.course to "course",
            R.id.chapter to "chapter",
            R.id.content to "chaptercontent",
            R.id.live_stream to "live stream",
            R.id.discussion to "forumthread",
            R.id.discussion_category to "forumthreadcategory",
            R.id.post to "post",
            R.id.product to "product",
            R.id.doubts to "ticket"
        )
        return ids.mapNotNull { idToParamMap[it] }
    }

    private fun getFilterForContentType(ids: List<Int>): List<String> {
        val idToParamMap = mapOf(
            R.id.exam to "Exam",
            R.id.video to "Video",
            R.id.quiz to "Quiz",
            R.id.attachment to "Attachment",
            R.id.notes to "Notes",
            R.id.video_conferences to "VideoConference",
        )
        return ids.mapNotNull { idToParamMap[it] }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}