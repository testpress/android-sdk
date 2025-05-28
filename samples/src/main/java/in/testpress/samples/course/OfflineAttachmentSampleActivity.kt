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
import `in`.testpress.course.util.FileUtils.getFileExtensionFromUrl
import `in`.testpress.course.viewmodels.OfflineAttachmentViewModel
import `in`.testpress.database.entities.OfflineAttachment
import `in`.testpress.database.entities.OfflineAttachmentDownloadStatus
import `in`.testpress.samples.databinding.ActivityOfflineAttachmentSampleBinding
import `in`.testpress.samples.databinding.ListItemDownloadBinding
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
                attachmentUrl = "https://d36vpug2b5drql.cloudfront.net/institute/lmsdemo/private/courses/492/attachments/1bcdb2781fe5429bb30171296586aa5d.pdf?response-content-disposition=attachment%3B%20filename%3D100%20MB%20File.pdf&Expires=1748505400&Signature=KnJreC9pUlwfydNJBaUbgm8adETZIc7u5PggSsgsPZ4rcX1P1Ko0ydYn4pNX2IxWVdKP40LCXeN2YGUo~8kpjLlsz1XRb230ucV4t2tzAiqS8L4bz8LXNXuWCj6ehPNZapKjWUG6gzIf2qka13hSwvB8irYumHcSxYeCsH8d1P1HQBZvtrvqA0SJrSEcQdQASHv-2nxsdby9DJp68vETRosZ-fQXza1Sq8kHNLm4AoCJfSc-BOROP59N~JExTmgRpfeO3~0DAONOwKe7pa1o-h84gnSGbAYoOs9biRkZI5rFXXxY6WjqtMof4OnCZJSkBh2mY0~PzTqFz6XqHXgL2g__&Key-Pair-Id=K2XWKDWM065EGO",
                description = "",
                isRenderable = false
            )
            viewModel.requestDownload(
                attachment,
                destinationPath = File(
                    this.filesDir,
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
                OfflineAttachmentDownloadStatus.DOWNLOADED,
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
            OfflineAttachmentDownloadStatus.DOWNLOADED -> "Tap to open"
            OfflineAttachmentDownloadStatus.FAILED -> "Download Failed"
            OfflineAttachmentDownloadStatus.QUEUED -> "Waiting to download..."
            OfflineAttachmentDownloadStatus.DOWNLOADING -> "Downloading...${attachmentProgress}%"
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
