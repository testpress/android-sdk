package `in`.testpress.ui.fragments

import `in`.testpress.R
import `in`.testpress.ui.DiscussionViewModel
import `in`.testpress.ui.DiscussionViewModelFactory
import `in`.testpress.ui.DiscussionsAdapter
import `in`.testpress.ui.DiscussionsLoadingStateAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.ExperimentalPagingApi
import androidx.recyclerview.widget.DividerItemDecoration
import kotlinx.android.synthetic.main.discussion_list.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


open class DiscussionFragment: Fragment() {
    open val adapter = DiscussionsAdapter() { forum ->
    }

    val viewModel: DiscussionViewModel by viewModels {
        DiscussionViewModelFactory(requireActivity().application)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.discussion_list, container, false)
    }

    @ExperimentalPagingApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        fetchPosts()
        setCreateButtonClickListener()
    }

    private fun setupViews() {
        discussions.adapter = adapter.withLoadStateHeaderAndFooter(
                header = DiscussionsLoadingStateAdapter(adapter, requireContext()),
                footer = DiscussionsLoadingStateAdapter(adapter, requireContext())
        )
        discussions.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
    }

    @ExperimentalPagingApi
    private fun fetchPosts() {
        lifecycleScope.launch {
            viewModel.fetchPosts().collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }
    }

    open fun setCreateButtonClickListener() {
        create_button.setOnClickListener {

        }
    }
}