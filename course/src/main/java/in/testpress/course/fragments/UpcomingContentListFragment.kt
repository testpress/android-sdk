package `in`.testpress.course.fragments

import `in`.testpress.course.TestpressCourse
import `in`.testpress.course.databinding.ContentStateListLayoutBinding
import `in`.testpress.course.databinding.UpcomingContentListViewBinding
import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.repository.UpcomingContentRepository
import `in`.testpress.course.viewmodels.UpcomingContentsListViewModel
import `in`.testpress.enums.Status
import `in`.testpress.fragments.EmptyViewListener
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ExpandableListView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class UpcomingContentListFragment: Fragment(), EmptyViewListener {
    private lateinit var viewModel : UpcomingContentsListViewModel
    private lateinit var binding : UpcomingContentListViewBinding
    private var courseId: Long = -1
    lateinit var listView : ExpandableListView

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
        binding = UpcomingContentListViewBinding.inflate(inflater,container,false)
        return binding.root
    }

    private fun parseArguments() {
        courseId = arguments!!.getString(TestpressCourse.COURSE_ID)?.toLong()!!
    }

    private fun initializeViewModel() {
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return UpcomingContentsListViewModel(UpcomingContentRepository(requireContext(), courseId)) as T
            }
        }).get(UpcomingContentsListViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listView = binding.upcomingContentList
        initializeObservers()
        viewModel.loadContents()
    }

    private fun initializeObservers() {
        viewModel.items.observe(viewLifecycleOwner, Observer { resource ->
            when (resource?.status) {
                Status.LOADING -> {

                }
                Status.SUCCESS -> {

                }
                Status.ERROR -> {

                }
            }
        })
    }

    override fun onRetryClick() {
        Log.d("onRetryClick", "viewModel load contents")
        viewModel.loadContents()
    }
}