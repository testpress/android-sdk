package `in`.testpress.ui.fragments

import `in`.testpress.R
import `in`.testpress.ui.*
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
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
        inflater.inflate(R.menu.testpress_time_analytics_filter, menu)
        val filterMenu = menu.findItem(R.id.options)
        val actionView = filterMenu.actionView
        val filterIcon = actionView.findViewById<ImageView>(R.id.filter)
        filterIcon.setOnClickListener {
            toggledSideBar()
        }
        super.onCreateOptionsMenu(menu, inflater)
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

    private fun setupViews() {
        rvPosts.adapter = adapter
        val itemDecor = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        rvPosts.addItemDecoration(itemDecor)
    }

    override fun onApplyFilterClick(sortBy: String, category: String) {
        viewModel.sortAndFilter(sortBy, category)
    }

    override fun onClearFilterClick() {
        viewModel.sortAndFilter("recent")
        slidingPaneLayout.closePane()
    }
}