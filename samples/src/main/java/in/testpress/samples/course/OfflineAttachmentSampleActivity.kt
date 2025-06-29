package `in`.testpress.samples.course

import android.os.Bundle
import android.os.Environment
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import `in`.testpress.course.domain.DomainAttachmentContent
import `in`.testpress.course.services.DownloadQueueManager.restartPendingDownloads
import `in`.testpress.course.services.DownloadQueueManager.syncDownloadedFileWithDatabase
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
        restartPendingDownloads(this)
        syncDownloadedFileWithDatabase(this)
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
        binding.btnDownload1.setOnClickListener {
            val attachment = getDummyAttachment(1)
            startDownload(attachment)
        }

        binding.btnDownload2.setOnClickListener {
            val attachment = getDummyAttachment(2)
            startDownload(attachment)
        }

        binding.btnDownload3.setOnClickListener {
            val attachment = getDummyAttachment(3)
            startDownload(attachment)
        }
    }

    private fun getDummyAttachment(index: Long): DomainAttachmentContent {
        return DomainAttachmentContent(
            id = index,
            title = "File $index",
            attachmentUrl = "https://d36vpug2b5drql.cloudfront.net/institute/lmsdemo/private/courses/492/attachments/1bcdb2781fe5429bb30171296586aa5d.pdf?response-content-disposition=attachment%3B%20filename%3D100%20MB%20File.pdf&Expires=1751019079&Signature=KucUUmOHRWbG4X-YUfvRemwoVcEwxYoP6l3isK-x0VaimoGCrup4W63i4COnCDxFBlBaH~LYkv83YjVhTxhjCv4S8o75ttVYwHZqfWX0Z6RqPzEObX6TzpS9dZrTl~ACa8yiX1MhNqA5B1z17UiXvn6fYqusNw5OZviFqeoja~ngjOX4FdypnTA2GXQlv7JOmGAM3cDOC~YA1G879viq~BiqDDVx1T9zycPzVPN7kas7A6Uj-Ew1PpYIDGJte2Y2d5lvv3KyqNQ8FXZZxWEWP20B1EDW2DgDJ~1klEIKB~AsX1Eq~MIvnkV~67Hoyr246IQKMqYUE5eZNx12jsaFZw__&Key-Pair-Id=K2XWKDWM065EGO",
            description = "",
            isRenderable = false
        )
    }

    private fun startDownload(attachment: DomainAttachmentContent) {
        val destinationPath = File(
            Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS), attachment.title!!
        ).path + getFileExtensionFromUrl(attachment.attachmentUrl)

        val fileName = File(destinationPath).name
        viewModel.requestDownload(
            attachment,
            destinationPath = destinationPath,
            fileName = fileName
        )
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
