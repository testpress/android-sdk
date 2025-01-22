package `in`.testpress.course.fragments

import `in`.testpress.course.R
import `in`.testpress.course.databinding.BaseContentListLayoutBinding
import `in`.testpress.fragments.EmptyViewFragment
import `in`.testpress.fragments.EmptyViewListener
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.facebook.shimmer.ShimmerFrameLayout

abstract class BaseContentListFragment: Fragment(), EmptyViewListener {

    private lateinit var binding : BaseContentListLayoutBinding
    private lateinit var loadingPlaceholder: ShimmerFrameLayout

    // Protected properties are used in sub classes
    protected lateinit var recyclerView: RecyclerView
    protected lateinit var emptyViewFragment: EmptyViewFragment
    protected lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = BaseContentListLayoutBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews()
    }

    private fun bindViews() {
        recyclerView = binding.recyclerView
        loadingPlaceholder = binding.shimmerViewContainer
        loadingPlaceholder.visibility = View.GONE
        swipeRefreshLayout = binding.swipeContainer
        initializeEmptyViewFragment()
    }

    private fun initializeEmptyViewFragment() {
        emptyViewFragment = EmptyViewFragment()
        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.empty_view_fragment, emptyViewFragment)
        transaction.commit()
    }

    // Protected functions are used in sub classes
    protected fun showEmptyList(description: String) {
        emptyViewFragment.setEmptyText(R.string.testpress_no_content,
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
