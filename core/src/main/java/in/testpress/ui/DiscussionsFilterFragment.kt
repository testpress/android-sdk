package `in`.testpress.ui

import `in`.testpress.R
import `in`.testpress.util.Extensions.dismissOnOutsideClick
import `in`.testpress.util.Misc.addDaysToMilliSeconds
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import com.skydoves.powerspinner.PowerSpinnerView
import java.text.SimpleDateFormat
import kotlin.collections.HashMap


class DiscussionsFilterFragment: Fragment() {
    lateinit var categorySpinner: PowerSpinnerView
    lateinit var sortSpinner: PowerSpinnerView
    lateinit var authorSpinner: PowerSpinnerView
    lateinit var commentedBySpinner: PowerSpinnerView
    lateinit var upvotedBySpinner: PowerSpinnerView
    lateinit var applyFilterButton: Button
    lateinit var clearFilterButton: Button
    lateinit var dateRange: EditText
    lateinit var dateRangePicker: MaterialDatePicker<androidx.core.util.Pair<Long, Long>>
    var publishedAfter: String? = null
    var publishedBefore: String? = null

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
        initializeFilters()
        initializeDateRangeFilter()
    }
    private fun initializeFilters() {
        authorSpinner = view!!.findViewById(R.id.author_spinner) as PowerSpinnerView
        commentedBySpinner = view!!.findViewById(R.id.commented_thread_spinner) as PowerSpinnerView
        upvotedBySpinner = view!!.findViewById(R.id.upvoted_spinner) as PowerSpinnerView

        authorSpinner.selectItemByIndex(0)
        authorSpinner.dismissOnOutsideClick()
        commentedBySpinner.dismissOnOutsideClick()
        upvotedBySpinner.dismissOnOutsideClick()
        commentedBySpinner.selectItemByIndex(0)
        upvotedBySpinner.selectItemByIndex(0)
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


    private fun initializeDateRangeFilter() {
        dateRange = requireView().findViewById<EditText>(R.id.date_range)
        dateRangePicker =
            MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select dates")
                .setTheme(R.style.ThemeOverlay_MaterialComponents_MaterialCalendar)
                .build()

        dateRange.setOnClickListener {
            dateRangePicker.show(requireActivity().supportFragmentManager, dateRangePicker.toString())
        }
        dateRangePicker.addOnPositiveButtonClickListener {
            val df = SimpleDateFormat("dd MMM yyyy")
            publishedAfter = SimpleDateFormat("yyyy-MM-dd").format(it.first)
            publishedBefore = SimpleDateFormat("yyyy-MM-dd").format(addDaysToMilliSeconds(it.second, noOfDays=1))

            if (df.format(it.first).equals(df.format(it.second))) {
                dateRange.setText("${df.format(it.first)}")
            } else {
                dateRange.setText("${df.format(it.first)} - ${df.format(it.second)}")
            }
        }
    }

    private fun setButtonClickListeners() {
        applyFilterButton.setOnClickListener {
            val sortBy: String = DiscussionsSort.getTag(sortSpinner.selectedIndex)
            var categoryKey = categories.keys.elementAt(categorySpinner.selectedIndex).toString()
            if (categoryKey == "-1") categoryKey = ""
            val data = hashMapOf("sort" to sortBy, "category" to categoryKey)

            if (authorSpinner.selectedIndex == 1) {
                data["posted_by_me"] = "true"
            }
            if (upvotedBySpinner.selectedIndex == 1) {
                data["upvoted_by_me"] = "true"
            }
            if (commentedBySpinner.selectedIndex == 1) {
                data["commented_by_me"] = "true"
            }

            publishedAfter?.let {data["posted_after"] = it}
            publishedBefore?.let {data["posted_before"] = it}
            discussionFilterListener?.onApplyFilterClick(data)
        }

        clearFilterButton.setOnClickListener {
            categorySpinner.selectItemByIndex(0)
            sortSpinner.selectItemByIndex(0)
            commentedBySpinner.selectItemByIndex(0)
            upvotedBySpinner.selectItemByIndex(0)
            authorSpinner.selectItemByIndex(0)
            publishedBefore = null
            publishedAfter = null
            dateRange.text.clear()
            discussionFilterListener?.onClearFilterClick()
        }
    }
}

interface DiscussionFilterListener {
    fun onApplyFilterClick(filters:HashMap<String, String>)
    fun onClearFilterClick()
}


enum class DiscussionsSort(val displayName: String, val tag: String) {
    CHOOSE_A_FILTER("Choose A Filter", ""),
    RECENTLY_ADDED("Recently Added", "-created"),
    MOST_VIEWED("Most Viewed", "views_count"),
    MOST_UPVOTED("Most Upvoted", "-upvotes"),
    OLD_TO_NEW("Old to New", "created");

    companion object {
        fun getValues() = values().map { it.displayName }
        fun getTag(ordinal: Int) = values()[ordinal].tag
    }
}