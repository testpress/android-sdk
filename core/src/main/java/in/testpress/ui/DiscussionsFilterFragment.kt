package `in`.testpress.ui

import `in`.testpress.R
import `in`.testpress.util.Extensions.dismissOnOutsideClick
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
import com.skydoves.powerspinner.PowerSpinnerView
import com.skydoves.powerspinner.createPowerSpinnerView
import `in`.testpress.util.ViewUtils.toast
import android.util.Log

import com.skydoves.powerspinner.OnSpinnerItemSelectedListener




class DiscussionsFilterFragment: Fragment() {
    lateinit var categorySpinner: PowerSpinnerView
    lateinit var sortSpinner: PowerSpinnerView
    lateinit var applyFilterButton: Button
    lateinit var clearFilterButton: Button

    val categories = linkedMapOf(-1L to "All Discussions")
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
        sortSpinner = view.findViewById(R.id.sort_spinner) as PowerSpinnerView
        sortSpinner.setItems(DiscussionsSort.getValues())
        sortSpinner.selectItemByIndex(0)
        sortSpinner.dismissOnOutsideClick()
    }

    private fun initializeCategoryDropdown(view: View) {
        categorySpinner = view.findViewById(R.id.category_spinner) as PowerSpinnerView
        categorySpinner.setItems(categories.values.toList())
        categorySpinner.selectItemByIndex(0)
        categorySpinner.dismissOnOutsideClick()
        populateCategories()
    }

    private fun populateCategories() {
        viewModel.categories.observe(viewLifecycleOwner, Observer {
            it.forEach { category ->
                categories[category.id!!] = category.name!!
            }
            categorySpinner.setItems(categories.values.toList())
        })
    }

    private fun setButtonClickListeners() {
        applyFilterButton.setOnClickListener {
            val sortBy: String = DiscussionsSort.getTag(sortSpinner.selectedIndex)
            val categoryKey = categories.keys.elementAt(categorySpinner.selectedIndex)
            discussionFilterListener?.onApplyFilterClick(sortBy, categoryKey.toString())
        }

        clearFilterButton.setOnClickListener {
            categorySpinner.selectItemByIndex(0)
            sortSpinner.selectItemByIndex(0)
            discussionFilterListener?.onClearFilterClick()
        }
    }
}

interface DiscussionFilterListener {
    fun onApplyFilterClick(sortBy: String, category: String)
    fun onClearFilterClick()
}


enum class DiscussionsSort(val displayName: String, val tag: String) {
    CHOOSE_A_FILTER("Choose A Filter", ""),
    RECENTLY_ADDED("Recently Added", "recent"),
    MOST_VIEWED("Most Viewed", "views"),
    MOST_UPVOTED("Most Upvoted", "upvotes"),
    OLD_TO_NEW("Old to New", "old");

    companion object {
        fun getValues() = values().map { it.displayName }
        fun getTag(ordinal: Int) = values()[ordinal].tag
    }
}