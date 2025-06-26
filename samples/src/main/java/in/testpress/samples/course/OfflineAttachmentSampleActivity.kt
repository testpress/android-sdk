package `in`.testpress.samples.course

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import `in`.testpress.course.domain.DomainAttachmentContent
import `in`.testpress.course.viewmodels.OfflineAttachmentViewModel
import `in`.testpress.database.entities.OfflineAttachment
import `in`.testpress.database.entities.OfflineAttachmentDownloadStatus
import `in`.testpress.samples.databinding.ActivityOfflineAttachmentSampleBinding
import `in`.testpress.samples.databinding.ListItemDownloadBinding
import `in`.testpress.util.getFileExtensionFromUrl
import java.io.File

class OfflineAttachmentSampleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOfflineAttachmentSampleBinding
    private lateinit var viewModel: OfflineAttachmentViewModel
    private lateinit var adapter: DownloadsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOfflineAttachmentSampleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = OfflineAttachmentViewModel.get(this)
        setupRecyclerView()
        observeViewModel()
        initializeExitText()
    }

    private fun setupRecyclerView() {
        adapter = DownloadsAdapter(
            onCancel = { viewModel.cancel(it.id) },
            onDelete = { viewModel.delete(it.id) },
            onOpen = { viewModel.openFile(this, it) }
        )
        binding.listViewDownloads.layoutManager = LinearLayoutManager(this)
        binding.listViewDownloads.adapter = adapter
    }

    private fun observeViewModel() {
        lifecycleScope.launchWhenStarted {
            viewModel.files.collect { files ->
                adapter.submitList(files)
            }
        }
    }

    private fun initializeExitText() {
        binding.btnDownload.setOnClickListener {
            val attachment = DomainAttachmentContent(
                id = 1,
                title = "Attach 1",
                attachmentUrl = "https://d36vpug2b5drql.cloudfront.net/institute/lmsdemo/private/courses/492/attachments/bc037ba7f7654d9a958bb405c6e8f49d.png?response-content-disposition=attachment%3B%20filename%3Dimage.png&Expires=1748513422&Signature=QZgtgNGzaXAZEbUvtjDVA1ur4mm47NiUtBnAy6IkDoj6jaWCqz5yyJSxgVpMv-rVEb-JNgDnXqBLcCyqO9jQu-vb5FNw2nlw4PQ7mFn2fBHx3HhFUWFZXqcN4kMPyHo4AYLPPwcx00Ri-HpujyZDOudMaH0~rdgMB9roBBpVCa2Ta5WXJ~h4tWcrsMd~U9COwrN2o0CHO62sJ~z2lsw7PTahFj8ly2TWRgAO0n8UIN-3-JknUaZPqd0nocZEzurxaf6tia9qHfx5B4q8JQZfnjcCAMzWZ3xViZFjD7EECFR-NNvqsVyKfA~iygCHfYmAUDXhBs~HAFjAa4Mgy8jjCg__&Key-Pair-Id=K2XWKDWM065EGO",
                description = "",
                isRenderable = false
            )
            viewModel.requestDownload(
                attachment,
                destinationPath = File(
                    "${this.filesDir}/offline_attachments/",
                    attachment.title!!
                ).path + getFileExtensionFromUrl(attachment.attachmentUrl)
            )
        }
    }

}

class DownloadsAdapter(
    private val onCancel: (OfflineAttachment) -> Unit,
    private val onDelete: (OfflineAttachment) -> Unit,
    private val onOpen: (OfflineAttachment) -> Unit
) : ListAdapter<OfflineAttachment, DownloadsAdapter.DownloadViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadViewHolder {
        val binding = ListItemDownloadBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DownloadViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DownloadViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class DownloadViewHolder(
        private val binding: ListItemDownloadBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: OfflineAttachment) {
            binding.textTitle.text = item.title
            binding.textSubtitle.text = getAttachmentStatusText(item.status, item.progress)

            when (item.status) {
                OfflineAttachmentDownloadStatus.COMPLETED,
                OfflineAttachmentDownloadStatus.DELETE,
                OfflineAttachmentDownloadStatus.FAILED -> {
                    binding.progressBar.visibility = android.view.View.GONE
                    binding.btnCancel.visibility = android.view.View.GONE
                    binding.btnDelete.visibility = android.view.View.VISIBLE
                }

                OfflineAttachmentDownloadStatus.QUEUED,
                OfflineAttachmentDownloadStatus.DOWNLOADING -> {
                    binding.progressBar.visibility = android.view.View.VISIBLE
                    binding.btnCancel.visibility = android.view.View.VISIBLE
                    binding.btnDelete.visibility = android.view.View.GONE
                }
            }

            binding.btnCancel.setOnClickListener { onCancel(item) }
            binding.btnDelete.setOnClickListener { onDelete(item) }
            binding.root.setOnClickListener { onOpen(item) }
        }
    }

    fun getAttachmentStatusText(
        attachmentStatus: OfflineAttachmentDownloadStatus,
        attachmentProgress: Int
    ): String {
        return when (attachmentStatus) {
            OfflineAttachmentDownloadStatus.COMPLETED -> "Tap to open"
            OfflineAttachmentDownloadStatus.FAILED -> "Download Failed"
            OfflineAttachmentDownloadStatus.QUEUED -> "Waiting to download..."
            OfflineAttachmentDownloadStatus.DOWNLOADING -> "Downloading...${attachmentProgress}%"
            OfflineAttachmentDownloadStatus.DELETE -> "This content has been removed"
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<OfflineAttachment>() {
        override fun areItemsTheSame(
            oldItem: OfflineAttachment,
            newItem: OfflineAttachment
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: OfflineAttachment,
            newItem: OfflineAttachment
        ): Boolean {
            return oldItem == newItem
        }
    }
}
