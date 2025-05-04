package `in`.testpress.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import `in`.testpress.R
import `in`.testpress.databinding.TestpressBaseListFooterAdapterBinding

class BaseListFooterAdapter(private val retry: () -> Unit) :
    LoadStateAdapter<BaseListFooterAdapter.BaseListFooterViewHolder>() {
    override fun onBindViewHolder(holder: BaseListFooterViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): BaseListFooterViewHolder {
        return BaseListFooterViewHolder.create(parent, retry)
    }

    class BaseListFooterViewHolder(
        private val binding: TestpressBaseListFooterAdapterBinding,
        retry: () -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.retryButton.setOnClickListener { retry.invoke() }
        }

        fun bind(loadState: LoadState) {
            if (loadState is LoadState.Error) {
                showErrorMessage(loadState)
            }
            binding.progressBar.isVisible = loadState is LoadState.Loading
            binding.retryButton.isVisible = loadState is LoadState.Error
            binding.errorMessageContainer.isVisible = loadState is LoadState.Error
        }

        private fun showErrorMessage(loadState: LoadState.Error) {
            if (loadState.error.localizedMessage?.contains("404") == true) {
                binding.emptyTitle.text = "Content Not Found"
                binding.emptyDescription.text = "Content Not Found, Please try after some time"
            }
        }

        companion object {
            fun create(parent: ViewGroup, retry: () -> Unit): BaseListFooterViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.testpress_base_list_footer_adapter, parent, false)
                val binding = TestpressBaseListFooterAdapterBinding.bind(view)
                return BaseListFooterViewHolder(binding, retry)
            }
        }
    }
}

