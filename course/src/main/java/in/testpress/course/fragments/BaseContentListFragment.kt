package `in`.testpress.course.fragments

import `in`.testpress.course.R
import `in`.testpress.course.TestpressCourse
import `in`.testpress.databinding.BaseContentListLayoutBinding
import `in`.testpress.fragments.EmptyViewFragment
import `in`.testpress.fragments.EmptyViewListener
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.facebook.shimmer.ShimmerFrameLayout

abstract class BaseContentListFragment: Fragment(), EmptyViewListener {

    private lateinit var binding : BaseContentListLayoutBinding
    private lateinit var loadingPlaceholder: ShimmerFrameLayout

    // Protected properties are used in sub classes
    protected lateinit var contentsURL: String
    protected var chapterId: Long = -1
    protected var productSlug: String? = null
    protected var courseId: Long = -1
    protected lateinit var swipeRefreshLayout: SwipeRefreshLayout
    protected lateinit var recyclerView: RecyclerView
    protected lateinit var progressBar: ProgressBar
    protected lateinit var emptyViewFragment: EmptyViewFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parseArguments()
        initializeViewModel()
    }

    private fun parseArguments() {
        contentsURL = arguments!!.getString(TestpressCourse.CONTENTS_URL_FRAG)?:""
        chapterId = arguments!!.getLong(TestpressCourse.CHAPTER_ID)
        productSlug = arguments!!.getString(TestpressCourse.PRODUCT_SLUG)
        courseId = arguments!!.getString(TestpressCourse.COURSE_ID)?.toLong()?:-1
    }

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
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
        setRecyclerViewScrollListener()
    }

    private fun bindViews() {
        recyclerView = binding.recyclerView
        loadingPlaceholder = binding.shimmerViewContainer
        loadingPlaceholder.visibility = View.GONE
        swipeRefreshLayout = binding.swipeRunningContentContainer
        swipeRefreshLayout.setColorSchemeResources(R.color.testpress_color_primary)
        progressBar = binding.bottomProgressBar
        initializeEmptyViewFragment()
    }

    private fun setRecyclerViewScrollListener(){
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (recyclerView.adapter?.itemCount == (layoutManager.findLastCompletelyVisibleItemPosition() + 1)){
                    fetchMore()
                }
            }
        })
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

    abstract fun fetchMore()
    abstract fun initializeViewModel()
}
