package `in`.testpress.course.ui

import `in`.testpress.course.R
import `in`.testpress.course.TestpressCourse
import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.fragments.BaseContentListFragment
import `in`.testpress.enums.Status
import `in`.testpress.course.repository.ContentsRepository
import `in`.testpress.course.viewmodels.ContentsListViewModel
import `in`.testpress.fragments.EmptyViewListener
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager

class ContentListFragment : BaseContentListFragment(), EmptyViewListener {

    private var contentsURL: String = ""
    private var chapterId: Long = -1
    private var productSlug: String? = null
    private lateinit var viewModel: ContentsListViewModel
    private lateinit var mAdapter: ContentListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parseArguments()
        initializeViewModel()
    }

    private fun parseArguments() {
        contentsURL = arguments!!.getString(TestpressCourse.CONTENTS_URL_FRAG) ?: ""
        chapterId = arguments!!.getLong(TestpressCourse.CHAPTER_ID)
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
        mAdapter = ContentListAdapter(chapterId, productSlug)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
        initializeObservers()
        viewModel.loadContents()
        swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadContents()
        }
    }

    private fun initializeObservers() {

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
                    if (items.isEmpty()) showEmptyList(resources.getString(R.string.testpress_no_content))
                    mAdapter.contents = items
                    mAdapter.notifyDataSetChanged()
                    swipeRefreshLayout.isRefreshing = false
                }
                Status.ERROR -> {
                    Log.d("ContentListFragment", "Got status ERROR")
                    hideLoadingPlaceholder()
                    if (resource.data != null) {
                        mAdapter.contents = resource.data as List<DomainContent>
                        mAdapter.notifyDataSetChanged()
                    } else {
                        emptyViewFragment.displayError(resource.exception!!)
                    }
                }
                else -> {}
            }
        })
    }

    override fun onRetryClick() {
        Log.d("onRetryClick", "viewModel load contents")
        viewModel.loadContents()
    }
}