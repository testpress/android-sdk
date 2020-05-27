package `in`.testpress.course.ui

import `in`.testpress.core.TestpressException
import `in`.testpress.course.R
import `in`.testpress.course.TestpressCourse
import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.enums.Status
import `in`.testpress.course.repository.ContentsRepository
import `in`.testpress.course.viewmodels.ContentsListViewModel
import `in`.testpress.fragments.EmptyViewFragment
import `in`.testpress.fragments.EmptyViewListener
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.base_list_layout.*

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
        mAdapter = ContentListAdapter(chapterId, productSlug)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter
        }
        initalizeObservers()
        viewModel.loadContents()
    }

    private fun initalizeObservers() {

        viewModel.items.observe(viewLifecycleOwner, Observer { resource ->
            when (resource?.status) {
                Status.SUCCESS -> {
                    val items = resource.data!! as List<DomainContent>
                    showEmptyList(items.isEmpty())
                    mAdapter.contents = items
                    mAdapter.notifyDataSetChanged()
                }
                Status.ERROR -> {
                    (emptyViewFragment as EmptyViewFragment).displayError(resource.exception!!)
                }
            }
        })
    }

    private fun showEmptyList(show: Boolean) {
        if (show) {
            (emptyViewFragment as EmptyViewFragment).setEmptyText(R.string.testpress_no_content,
                R.string.testpress_no_content_description,
                R.drawable.ic_error_outline_black_18dp
            )
        }
    }

    override fun onRetryClick() {
        viewModel.loadContents()
    }
}