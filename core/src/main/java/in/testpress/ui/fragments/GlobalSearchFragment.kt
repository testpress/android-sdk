package `in`.testpress.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import `in`.testpress.R
import `in`.testpress.databinding.GlobalSearchFragmentLayoutBinding
import `in`.testpress.databinding.SearchResultItemBinding
import `in`.testpress.databinding.TestpressBaseListFooterAdapterBinding
import `in`.testpress.models.SearchResult
import `in`.testpress.network.APIClient
import `in`.testpress.repository.GlobalSearchRepository
import `in`.testpress.ui.viewmodel.GlobalSearchViewModel
import kotlinx.coroutines.flow.collectLatest

class GlobalSearchFragment:Fragment() {

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

        // Listen to global query text changes
        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.let {
                    if (it.isNotEmpty()){
                        Log.d("TAG", "onTextChanged: $it")
                        viewModel.updateSearchQuery(mapOf("q" to it.toString()))
                    } else {
                        viewModel.updateSearchQuery(mapOf())
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        Log.d("TAG", "onViewCreated: ${binding.filterChipGroup.childCount}")

        for (i in 0 until binding.filterChipGroup.childCount) {
            val chip = binding.filterChipGroup.getChildAt(i) as? Chip
            chip?.setOnCheckedChangeListener { buttonView, isChecked ->
                search()
            }
        }
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
        val selectedParams = mutableListOf<String>()
        for (i in 0 until binding.filterChipGroup.childCount) {
            val chip = binding.filterChipGroup.getChildAt(i) as? Chip
            if (chip?.isChecked == true) {
                selectedParams.addAll(getValue(chip.id))
            }
        }
        viewModel.updateFilterQuery(selectedParams)
    }

    fun getValue(id: Int): List<String> {
        return when (id) {
            R.id.course -> listOf("course")
            R.id.chapter -> listOf("chapter")
            R.id.content -> listOf("chaptercontent")
            R.id.exam -> listOf("chaptercontent", "exam")
            R.id.video -> listOf("chaptercontent", "video")
            R.id.quiz -> listOf("chaptercontent", "quiz")
            R.id.attachment -> listOf("chaptercontent", "attachment")
            R.id.notes -> listOf("chaptercontent", "notes")
            R.id.video_conferences -> listOf("chaptercontent", "video_conferences")
            R.id.live_stream -> listOf("chaptercontent", "live_stream")
            R.id.discussion -> listOf("discussion")
            R.id.discussion_category -> listOf("discussion_category")
            R.id.post -> listOf("post")
            R.id.product -> listOf("product")
            R.id.doubts -> listOf("doubts")
            else -> emptyList()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}

class GlobalSearchAdapter : PagingDataAdapter<SearchResult, GlobalSearchAdapter.SearchResultHolder>(ARTICLE_DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultHolder =
        SearchResultHolder(
            SearchResultItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )
        )

    override fun onBindViewHolder(holder: SearchResultHolder, position: Int) {
        val tile = getItem(position)
        if (tile != null) {
            Log.d("TAG", "onBindViewHolder: ${tile.title}")
            holder.bind(tile)
        }
    }

    class SearchResultHolder(
        private val binding: SearchResultItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(searchResult: SearchResult) {
            binding.apply {
                binding.title.text = searchResult.title
                binding.type.text = searchResult.type
                binding.active.text = searchResult.active.toString()
            }
        }
    }

    companion object {
        private val ARTICLE_DIFF_CALLBACK = object : DiffUtil.ItemCallback<SearchResult>() {
            override fun areItemsTheSame(oldItem: SearchResult, newItem: SearchResult): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: SearchResult, newItem: SearchResult): Boolean =
                oldItem == newItem
        }
    }
}

class BaseListFooterAdapter(private val retry: () -> Unit) :
    LoadStateAdapter<BaseListFooterViewHolder>() {
    override fun onBindViewHolder(holder: BaseListFooterViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): BaseListFooterViewHolder {
        return BaseListFooterViewHolder.create(parent, retry)
    }
}

class BaseListFooterViewHolder(
    private val binding: TestpressBaseListFooterAdapterBinding,
    retry: () -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    init {
        binding.retryButton.setOnClickListener { retry.invoke() }
    }

    fun bind(loadState: LoadState) {
        if (loadState is LoadState.Error) {
            showErrorMessage(loadState)
        }
        binding.progressBar.isVisible = loadState is LoadState.Loading
        binding.retryButton.isVisible = loadState is LoadState.Error
        binding.errorMessageContainer.isVisible = loadState is LoadState.Error
    }

    private fun showErrorMessage(loadState: LoadState.Error) {
        if (loadState.error.localizedMessage?.contains("404") == true){
            binding.emptyTitle.text = "Content Not Found"
            binding.emptyDescription.text = "Content Not Found, Please try after some time"
        }
    }

    companion object {
        fun create(parent: ViewGroup, retry: () -> Unit): BaseListFooterViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.testpress_base_list_footer_adapter, parent, false)
            val binding = TestpressBaseListFooterAdapterBinding.bind(view)
            return BaseListFooterViewHolder(binding, retry)
        }
    }
}