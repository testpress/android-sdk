package `in`.testpress.ui.fragments

import `in`.testpress.R
import `in`.testpress.models.NetworkForum
import `in`.testpress.ui.*
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ImageView
import android.widget.Spinner
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.ExperimentalPagingApi
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.slidingpanelayout.widget.SlidingPaneLayout
import kotlinx.android.synthetic.main.discussion_list.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


open class DiscussionFragment: Fragment() {
    var sortBySelectedPosition = 0
    open val adapter = DiscussionsAdapter() { forum ->
    }
    lateinit var slidingPaneLayout: SlidingPaneLayout

    val viewModel: DiscussionViewModel by viewModels {
        DiscussionViewModelFactory(requireActivity().application)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view =  inflater.inflate(R.layout.discussion_list, container, false)
        val categorySpinnerAdapter = ExploreSpinnerAdapter(inflater, resources, false)
        val categorySpinner = view.findViewById<Spinner>(R.id.category_spinner)
        val sortSpinner = view.findViewById<Spinner>(R.id.sort_spinner)
        categorySpinner.adapter = categorySpinnerAdapter
        val sortBySpinnerAdapter = ExploreSpinnerAdapter(inflater, resources, false)
        sortSpinner.adapter = sortBySpinnerAdapter
        addSortByItemsInSpinner(sortBySpinnerAdapter, sortSpinner)
        setHasOptionsMenu(true);
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.testpress_time_analytics_filter, menu)
        val filterMenu = menu.findItem(R.id.options)
        val actionView = filterMenu.actionView
        val filterIcon = actionView.findViewById<ImageView>(R.id.filter)
        filterIcon.setOnClickListener {
            setPanelOpen(!slidingPaneLayout.isOpen)
            Log.d("TAG", "onCreateOptionsMenu: ")
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun setPanelOpen(open: Boolean) {
        if (open) {
            slidingPaneLayout.openPane()
        } else {
            slidingPaneLayout.closePane()
        }
    }

    private fun addSortByItemsInSpinner(sortBySpinnerAdapter: ExploreSpinnerAdapter, sortSpinner: Spinner) {
        sortBySpinnerAdapter.clear()
        sortBySpinnerAdapter.addItem("Choose A filter", "Choose A Filter", false, 0)
        sortBySpinnerAdapter.addItem("RECENTLY ADDED", "RECENTLY ADDED", true, 0)
        sortBySpinnerAdapter.addItem("Most Viewed", "Most Viewed", true, 0)
        sortBySpinnerAdapter.addItem("Most Upvoted", "Most Upvoted", true, 0)
        sortBySpinnerAdapter.addItem("Old to New", "Old to New", true, 0)
        if (sortBySelectedPosition == -1) {
            sortBySelectedPosition = 0
            sortBySpinnerAdapter.notifyDataSetChanged()
        } else {
            sortBySpinnerAdapter.notifyDataSetChanged()
            sortSpinner.setSelection(sortBySelectedPosition)
        }
    }

    @ExperimentalPagingApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar: Toolbar = (activity as DiscussionActivity?)?.actionBarToolbar!!
        slidingPaneLayout = view.findViewById<SlidingPaneLayout>(R.id.sliding_layout)
        val spinnerContainer = layoutInflater.inflate(R.layout.testpress_actionbar_spinner, toolbar, false)
        val spinner: Spinner = spinnerContainer.findViewById(R.id.actionbar_spinner)
        val spinnerAdapter = ExploreSpinnerAdapter(layoutInflater,
                resources, true)
        spinnerAdapter.addItem("", "All Discussions", false, 0)
        spinnerAdapter.addHeader("Categories")
        spinner.adapter = spinnerAdapter
        spinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(spinner: AdapterView<*>?, view: View, position: Int, itemId: Long) {

            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }
        setupViews()
        fetchPosts()

    }

    @ExperimentalPagingApi
    private fun fetchPosts() {
        lifecycleScope.launch {
            viewModel.fetchPosts().collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }
    }

    private fun setupViews() {
        rvPosts.adapter = adapter
        val itemDecor = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        rvPosts.addItemDecoration(itemDecor)
    }
}

interface OnForumClickListener {
    fun onClick(model: NetworkForum)
}
