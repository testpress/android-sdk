package `in`.testpress.course.ui

import `in`.testpress.course.R
import `in`.testpress.course.TestpressCourse
import `in`.testpress.course.domain.DomainContent
import `in`.testpress.enums.Status
import `in`.testpress.course.repository.ContentsRepository
import `in`.testpress.course.viewmodels.ContentsListViewModel
import `in`.testpress.fragments.EmptyViewFragment
import `in`.testpress.fragments.EmptyViewListener
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout

class ContentListFragment : Fragment(), EmptyViewListener {
    companion object {
        const val CONTENTS_URL_FRAG = "contentsUrlFrag"
        const val CHAPTER_ID = "chapterId"
    }

    private lateinit var contentsURL: String
    private var chapterId: Long = -1
    private var productSlug: String? = null
    private lateinit var viewModel: ContentsListViewModel
    private lateinit var mAdapter: ContentListAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyViewFragment: EmptyViewFragment
    private lateinit var loadingPlaceholder: ShimmerFrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parseArguments()
        initializeViewModel()
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(`in`.testpress.R.layout.base_list_layout, container, false)
    }

    private fun parseArguments() {
        contentsURL = arguments!!.getString(CONTENTS_URL_FRAG)!!
        chapterId = arguments!!.getLong(CHAPTER_ID)
        productSlug = arguments!!.getString(TestpressCourse.PRODUCT_SLUG)
    }

    private fun initializeViewModel() {
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ContentsListViewModel(ContentsRepository(requireContext(), chapterId)) as T
            }
        }).get(ContentsListViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews()
        mAdapter = ContentListAdapter(chapterId, productSlug)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
        initalizeObservers()
        viewModel.loadContents()
    }

    private fun bindViews() {
        recyclerView = view!!.findViewById(R.id.recycler_view)
        loadingPlaceholder = view!!.findViewById(R.id.shimmer_view_container)
        loadingPlaceholder.visibility = View.GONE
        initializeEmptyViewFragment()
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun initializeEmptyViewFragment() {
        emptyViewFragment = EmptyViewFragment()
        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.empty_view_fragment, emptyViewFragment)
        transaction.commit()
    }

    private fun initalizeObservers() {

        viewModel.items.observe(viewLifecycleOwner, Observer { resource ->
            when (resource?.status) {
                Status.LOADING -> {
                    Log.d("ContentListFragment", "Got status LOADING")
                    showLoadingPlaceholder()
                }
                Status.SUCCESS -> {
                    Log.d("ContentListFragment", "Got status SUCCESS")
                    hideLoadingPlaceholder()
                    val items = resource.data!! as List<DomainContent>
                    Log.d("Items", "" + items.isEmpty())
                    if (items.isEmpty()) showEmptyList()
                    mAdapter.contents = items
                    mAdapter.notifyDataSetChanged()
                }
                Status.ERROR -> {
                    Log.d("ContentListFragment", "Got status ERROR")
                    hideLoadingPlaceholder()
                    emptyViewFragment.displayError(resource.exception!!)
                }
            }
        })
    }

    private fun showEmptyList() {
        emptyViewFragment.setEmptyText(R.string.testpress_no_content,
                R.string.testpress_no_content_description,
                R.drawable.ic_error_outline_black_18dp
        )
    }

    override fun onRetryClick() {
        Log.d("onRetryClick", "viewModel load contents")
        viewModel.loadContents()
    }

    fun showLoadingPlaceholder() {
        loadingPlaceholder.visibility = View.VISIBLE
        loadingPlaceholder.startShimmer()
    }

    fun hideLoadingPlaceholder() {
        loadingPlaceholder.stopShimmer()
        loadingPlaceholder.visibility = View.GONE
    }
}