package `in`.testpress.ui.fragments

import `in`.testpress.R
import `in`.testpress.ui.*
import android.app.SearchManager
import android.content.Context.SEARCH_SERVICE
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
import kotlinx.android.synthetic.main.discussion_list.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


open class DiscussionFragment: Fragment(), DiscussionFilterListener {
    open val adapter = DiscussionsAdapter() { forum ->
    }
    lateinit var slidingPaneLayout: SlidingPaneLayout


    private val viewModel: DiscussionViewModel by viewModels {
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.discussion_list, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.discussions_menu, menu)
        val filterMenu = menu.findItem(R.id.options)
        val actionView = filterMenu.actionView
        val filterIcon = actionView.findViewById<ImageView>(R.id.filter)
        filterIcon.setOnClickListener {
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
                    viewModel.sortAndFilter("recent", search_query = query)
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
        setupViews()
        fetchPosts()
        setCreateButtonClickListener()
    }

    private fun setupViews() {
        discussions.adapter = adapter.withLoadStateHeaderAndFooter(
                header = DiscussionsLoadingStateAdapter(adapter, requireContext()),
                footer = DiscussionsLoadingStateAdapter(adapter, requireContext())
        )
        discussions.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
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

        viewModel.sortAndFilter("recent")
    }

    open fun setCreateButtonClickListener() {
        create_button.setOnClickListener {

        }
    }

    override fun onApplyFilterClick(sortBy: String, category: String) {
        viewModel.sortAndFilter(sortBy, category)
    }

    override fun onClearFilterClick() {
        viewModel.sortAndFilter("recent")
        slidingPaneLayout.closePane()
    }
}