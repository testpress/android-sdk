package `in`.testpress.ui.fragments

import `in`.testpress.R
import `in`.testpress.databinding.DiscussionListBinding
import `in`.testpress.ui.*
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.paging.ExperimentalPagingApi
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.slidingpanelayout.widget.SlidingPaneLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


open class DiscussionFragment: Fragment(), DiscussionFilterListener {
    private var _binding: DiscussionListBinding? = null
    private val binding get() = _binding!!

    open val adapter = DiscussionsAdapter() { forum ->
    }
    lateinit var slidingPaneLayout: SlidingPaneLayout
    open lateinit var createButton: FloatingActionButton


    open val viewModel: DiscussionViewModel by viewModels {
        object : AbstractSavedStateViewModelFactory(this, null) {
            override fun <T : ViewModel?> create(
                    key: String,
                    modelClass: Class<T>,
                    handle: SavedStateHandle
            ): T {
                return DiscussionViewModel(requireActivity().application, handle) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DiscussionListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.discussions_menu, menu)
        val filterMenu = menu.findItem(R.id.options)
        val actionView = filterMenu.actionView
        val filterIcon = actionView?.findViewById<ImageView>(R.id.filter)
        filterIcon?.setOnClickListener {
            toggledSideBar()
        }
        initializeSearchView(menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun initializeSearchView(menu: Menu) {
        val searchView = menu.findItem(R.id.search).actionView as SearchView
        searchView.queryHint = "Type text "
        searchView.isSubmitButtonEnabled = true
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    val data = hashMapOf("sort" to "-created")
                    viewModel.sortAndFilter(data, search_query = query)
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }

    private fun toggledSideBar() {
        if (slidingPaneLayout.isOpen) {
            slidingPaneLayout.closePane()
        } else {
            slidingPaneLayout.openPane()
        }
    }

    @ExperimentalPagingApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        slidingPaneLayout = view.findViewById(R.id.sliding_layout)
        createButton = view.findViewById(R.id.create_button)
        setupViews()
        fetchPosts()
        setCreateButtonClickListener()
    }

    private fun setupViews() {
        binding.discussions.adapter = adapter.withLoadStateHeaderAndFooter(
                header = DiscussionsLoadingStateAdapter(adapter, requireContext()),
                footer = DiscussionsLoadingStateAdapter(adapter, requireContext())
        )
        binding.discussions.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        initializeFilterFragment()
    }


    private fun initializeFilterFragment() {
        val fragment = DiscussionsFilterFragment()
        fragment.discussionFilterListener = this
        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.filter_fragment_layout, fragment)
        transaction.commit()
    }

    @ExperimentalPagingApi
    private fun fetchPosts() {
        lifecycleScope.launch {
            viewModel.discussions.collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }

        val data = hashMapOf("sort" to "-created")
        viewModel.sortAndFilter(data)
    }

    open fun setCreateButtonClickListener() {
        createButton.setOnClickListener {

        }
    }

    override fun onApplyFilterClick(filters: HashMap<String, String>) {
        viewModel.sortAndFilter(filters)
        slidingPaneLayout.closePane()
    }

    override fun onClearFilterClick() {
        val data = hashMapOf("sortBy" to "-created")
        viewModel.sortAndFilter(data)
        slidingPaneLayout.closePane()
    }
}