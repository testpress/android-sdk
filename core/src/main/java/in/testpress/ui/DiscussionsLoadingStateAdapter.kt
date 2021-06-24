package `in`.testpress.ui

import `in`.testpress.R
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView

class DiscussionsLoadingStateAdapter(
        private val adapter: DiscussionsAdapter,
        private val context: Context
) : LoadStateAdapter<DiscussionsLoadingStateAdapter.NetworkStateItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState) =
            NetworkStateItemViewHolder(
                    LayoutInflater.from(context)
                                    .inflate(R.layout.item_network_state, parent, false)
            ) { adapter.retry() }

    override fun onBindViewHolder(holder: NetworkStateItemViewHolder, loadState: LoadState) =
            holder.bind(loadState)

    class NetworkStateItemViewHolder(
            view: View,
            private val retryCallback: () -> Unit
    ) : RecyclerView.ViewHolder(view) {
        val progressBar = view.findViewById<ProgressBar>(R.id.progress_bar)
        val retryButton = view.findViewById<Button>(R.id.retry_button)
        val errorMsg = view.findViewById<TextView>(R.id.error_msg)

        init {
            retryButton.setOnClickListener { retryCallback() }
        }

        fun bind(loadState: LoadState) {
            progressBar.isVisible = loadState is LoadState.Loading
            retryButton.isVisible = loadState is LoadState.Error
            errorMsg.isVisible =
                    !(loadState as? LoadState.Error)?.error?.message.isNullOrBlank()
            errorMsg.text = (loadState as? LoadState.Error)?.error?.message
        }
    }
}