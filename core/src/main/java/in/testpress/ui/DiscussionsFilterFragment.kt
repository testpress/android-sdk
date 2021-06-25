package `in`.testpress.ui

import `in`.testpress.R
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class DiscussionsFilterFragment: Fragment() {
    lateinit var sortBySpinnerAdapter: ExploreSpinnerAdapter
    lateinit var categorySpinnerAdapter: ExploreSpinnerAdapter
    lateinit var categorySpinner: Spinner
    lateinit var sortSpinner: Spinner
    lateinit var applyFilterButton: Button
    lateinit var clearFilterButton: Button

    var sortBySelectedPosition = 0
    var categorySelectedPosition = 0
    var discussionFilterListener: DiscussionFilterListener? = null

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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        initializeListeners()
    }

    private fun initializeListeners() {
        discussionFilterListener = if (parentFragment != null) {
            parentFragment as? DiscussionFilterListener
        } else {
            context as? DiscussionFilterListener
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.discussions_filter, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyFilterButton = view.findViewById<Button>(R.id.apply_filter)
        clearFilterButton = view.findViewById<Button>(R.id.clear_filter)
        initializeSortByDropdown(view)
        initializeCategoryDropdown(view)
        setButtonClickListeners()
    }

    private fun initializeSortByDropdown(view: View) {
        sortBySpinnerAdapter = ExploreSpinnerAdapter(layoutInflater, resources, false)
        sortSpinner = view.findViewById(R.id.sort_spinner)
        sortSpinner.adapter = sortBySpinnerAdapter
        sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(spinner: AdapterView<*>?, view: View, position: Int, itemId: Long) {
                sortBySelectedPosition = position
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }
        populateSortItems()
        sortSpinner.setSelection(0)
        sortBySpinnerAdapter.notifyDataSetChanged()
    }

    private fun populateSortItems() {
        sortBySpinnerAdapter.clear()
        sortBySpinnerAdapter.addItem("", "Choose A Filter", false, 0)
        sortBySpinnerAdapter.addItem("recent", "Recently Added", true, 0)
        sortBySpinnerAdapter.addItem("views", "Most Viewed", true, 0)
        sortBySpinnerAdapter.addItem("upvotes", "Most Upvoted", true, 0)
        sortBySpinnerAdapter.addItem("old", "Old to New", true, 0)
    }


    private fun initializeCategoryDropdown(view: View) {
        categorySpinnerAdapter = ExploreSpinnerAdapter(layoutInflater, resources, false)
        categorySpinner = view.findViewById(R.id.category_spinner)
        categorySpinner.adapter = categorySpinnerAdapter

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(spinner: AdapterView<*>?, view: View, position: Int, itemId: Long) {
                categorySelectedPosition = position
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }
        populateCategories()
    }

    private fun populateCategories() {
        viewModel.categories.observe(viewLifecycleOwner, Observer {
            categorySpinnerAdapter.clear()
            categorySpinnerAdapter.addItem("", "All Discussions", false, 0)
            categorySpinnerAdapter.addHeader("Categories")
            it.forEach { category ->
                categorySpinnerAdapter.addItem("" + category.id, category.name, true, Color.parseColor("#" + category.color))
            }
            categorySpinnerAdapter.notifyDataSetChanged()
            categorySpinner.setSelection(categorySelectedPosition)
        })
    }

    private fun setButtonClickListeners() {
        applyFilterButton.setOnClickListener {
            val sortBy: String = sortBySpinnerAdapter.getTag(sortBySelectedPosition)
            val category: String = categorySpinnerAdapter.getTag(categorySelectedPosition)
            discussionFilterListener?.onApplyFilterClick(sortBy, category)
        }

        clearFilterButton.setOnClickListener {
            categorySpinner.setSelection(1)
            sortSpinner.setSelection(1)
            discussionFilterListener?.onClearFilterClick()
        }
    }
}

interface DiscussionFilterListener {
    fun onApplyFilterClick(sortBy: String, category: String)
    fun onClearFilterClick()
}