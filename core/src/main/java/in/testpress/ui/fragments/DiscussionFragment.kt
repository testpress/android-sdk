package `in`.testpress.ui.fragments

import `in`.testpress.R
import `in`.testpress.models.NetworkForum
import `in`.testpress.ui.DiscussionViewModel
import `in`.testpress.ui.DiscussionViewModelFactory
import `in`.testpress.ui.DiscussionsAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import kotlinx.android.synthetic.main.discussion_list.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


open class DiscussionFragment: Fragment() {
    open val adapter = DiscussionsAdapter() { forum ->
        Log.d("TAG", "Clicked forum ${forum.title}")
    }

    val viewModel: DiscussionViewModel by viewModels {
        DiscussionViewModelFactory(requireActivity().application)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.discussion_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        fetchPosts()
    }

    private fun fetchPosts() {
        lifecycleScope.launch {
            viewModel.fetchPosts().collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }
    }

    private fun setupViews() {
        rvPosts.adapter = adapter
        val itemDecor = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        rvPosts.addItemDecoration(itemDecor)
    }
}

interface OnForumClickListener {
    fun onClick(model: NetworkForum)
}
