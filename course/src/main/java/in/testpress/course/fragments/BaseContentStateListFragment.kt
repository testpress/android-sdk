package `in`.testpress.course.fragments

import `in`.testpress.course.R
import `in`.testpress.course.TestpressCourse
import `in`.testpress.course.adapter.ContentStateListAdapter
import `in`.testpress.course.adapter.RunningContentAdapter
import `in`.testpress.course.databinding.ContentStateListLayoutBinding
import `in`.testpress.fragments.EmptyViewFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.facebook.shimmer.ShimmerFrameLayout

open class BaseContentStateListFragment(val fragmentTag:String): Fragment() {

    private lateinit var binding : ContentStateListLayoutBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var loadingPlaceholder: ShimmerFrameLayout
    // Protected properties are used in sub classes
    protected var courseId: Long = -1
    protected lateinit var mAdapter: RunningContentAdapter
    protected lateinit var emptyViewFragment: EmptyViewFragment
    protected lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parseArguments()
    }

    private fun parseArguments() {
        courseId = arguments!!.getString(TestpressCourse.COURSE_ID)?.toLong()!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ContentStateListLayoutBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews()
        mAdapter = RunningContentAdapter(fragmentTag)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
    }

    private fun bindViews() {
        recyclerView = binding.recyclerView
        loadingPlaceholder = binding.shimmerViewContainer
        loadingPlaceholder.visibility = View.GONE
        initializeEmptyViewFragment()
        swipeRefreshLayout = binding.swipeRunningContentContainer
    }

    private fun initializeEmptyViewFragment() {
        emptyViewFragment = EmptyViewFragment()
        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.empty_view_fragment, emptyViewFragment)
        transaction.commit()
    }

    // Protected functions are used in sub classes
    protected fun showEmptyList(description: String) {
        emptyViewFragment.setEmptyText(R.string.testpress_no_running_content,
            description,
            R.drawable.ic_error_outline_black_18dp
        )
    }

    protected fun showLoadingPlaceholder() {
        loadingPlaceholder.visibility = View.VISIBLE
        loadingPlaceholder.startShimmer()
    }

    protected fun hideLoadingPlaceholder() {
        loadingPlaceholder.stopShimmer()
        loadingPlaceholder.visibility = View.GONE
    }
}