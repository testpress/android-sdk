package `in`.testpress.course.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import `in`.testpress.course.databinding.OfflineAttachmentListFragmentBinding
import `in`.testpress.course.viewmodels.OfflineAttachmentViewModel
import `in`.testpress.database.entities.OfflineAttachment
import `in`.testpress.database.entities.OfflineAttachmentDownloadStatus

class OfflineAttachmentListFragment : Fragment() {

    private var _binding: OfflineAttachmentListFragmentBinding? = null
    private val binding: OfflineAttachmentListFragmentBinding get() = _binding!!
    private lateinit var viewModel: OfflineAttachmentViewModel // Renamed for clarity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = OfflineAttachmentViewModel.get(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = OfflineAttachmentListFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.offlineAttachmentComposeView.setContent {
            OfflineAttachmentScreen(viewModel = viewModel)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

@Composable
fun OfflineAttachmentScreen(viewModel: OfflineAttachmentViewModel) {
    val attachments by viewModel.files.collectAsState()
    val context = LocalContext.current

    if (attachments.isEmpty()) {
        EmptyState()
    } else {
        OfflineAttachmentList(
            attachments = attachments,
            onOpenFile = { file -> viewModel.openFile(context, file) },
            onDeleteDownload = { fileId -> viewModel.delete(fileId) },
            onCancelDownload = { fileId -> viewModel.cancel(fileId) }
        )
    }
}

@Composable
fun OfflineAttachmentList(
    attachments: List<OfflineAttachment>,
    onOpenFile: (OfflineAttachment) -> Unit,
    onDeleteDownload: (Long) -> Unit,
    onCancelDownload: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(attachments) { attachment ->
            OfflineAttachmentItem(
                attachment = attachment,
                onDeleteDownload = onDeleteDownload,
                onCancelDownload = onCancelDownload
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun OfflineAttachmentItem(
    attachment: OfflineAttachment,
    onDeleteDownload: (Long) -> Unit,
    onCancelDownload: (Long) -> Unit
) {
    Row(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = attachment.title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            when (attachment.status) {
                OfflineAttachmentDownloadStatus.DOWNLOADED -> {
                    Text(
                        text = "Tap to open",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                OfflineAttachmentDownloadStatus.FAILED -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Download Failed",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = "Delete File",
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
                OfflineAttachmentDownloadStatus.QUEUED -> {
                    Text(
                        text = "Waiting to download...",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                OfflineAttachmentDownloadStatus.DOWNLOADING -> {
                    Text(
                        text = "Downloading...${attachment.progress}%",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        if (attachment.status == OfflineAttachmentDownloadStatus.DOWNLOADING && attachment.progress > 0) {
            CircularProgressIndicator(
                progress = attachment.progress / 100f,
                modifier = Modifier
                    .size(24.dp),
                strokeWidth = 3.dp,
                trackColor = Color.LightGray
            )
        }
        AttachmentActions(
            attachmentId = attachment.id,
            attachmentState = attachment.status,
            onDeleteDownload = onDeleteDownload,
            onCancelDownload = onCancelDownload
        )
    }
}

@Composable
fun AttachmentActions(
    attachmentId: Long,
    attachmentState: OfflineAttachmentDownloadStatus,
    onDeleteDownload: (Long) -> Unit,
    onCancelDownload: (Long) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        when (attachmentState) {
            OfflineAttachmentDownloadStatus.DOWNLOADED -> {
                IconButton(onClick = { onDeleteDownload(attachmentId) }) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete File",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            OfflineAttachmentDownloadStatus.FAILED -> {
                IconButton(onClick = { onDeleteDownload(attachmentId) }) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete File",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            OfflineAttachmentDownloadStatus.QUEUED -> {
                IconButton(onClick = { onCancelDownload(attachmentId) }) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Cancel Download",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            OfflineAttachmentDownloadStatus.DOWNLOADING -> {
                IconButton(onClick = { onCancelDownload(attachmentId) }) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Cancel Download",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.Close, // Or any relevant icon
            contentDescription = "No Downloads",
            modifier = Modifier
                .height(96.dp)
                .width(96.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Nothing Here",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "Looks like you’ve not downloaded any attachments as of now.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OfflineAttachmentListPreview() {
    val sampleAttachments = listOf(
        OfflineAttachment(
            id = 1,
            title = "Chapter 1 Notes",
            url = "",
            path = "/path/to/file1.pdf",
            status = OfflineAttachmentDownloadStatus.DOWNLOADED,
            progress = 100
        ),
        OfflineAttachment(
            id = 2,
            title = "Lecture Slides - Week 2",
            url = "",
            path = "",
            status = OfflineAttachmentDownloadStatus.DOWNLOADING,
            progress = 50
        ),
        OfflineAttachment(
            id = 5,
            title = "Corrupted",
            url = "",
            path = "",
            status = OfflineAttachmentDownloadStatus.FAILED
        ),
        OfflineAttachment(
            id = 6,
            title = "QUEUED",
            url = "",
            path = "",
            status = OfflineAttachmentDownloadStatus.QUEUED
        )
    )

    OfflineAttachmentList(
        attachments = sampleAttachments,
        onOpenFile = {},
        onDeleteDownload = {},
        onCancelDownload = {}
    )
}

@Preview(showBackground = true, name = "Empty State Preview")
@Composable
fun EmptyStatePreview() {
    EmptyState()
}

@Preview(showBackground = true, name = "Single Item - Downloaded")
@Composable
fun SingleItemDownloadedPreview() {
    OfflineAttachmentItem(
        attachment = OfflineAttachment(
            id = 1,
            title = "Downloaded File.pdf",
            url = "",
            path = "/path/to/file.pdf",
            status = OfflineAttachmentDownloadStatus.DOWNLOADED
        ),
        onDeleteDownload = {},
        onCancelDownload = {}
    )
}

@Preview(showBackground = true, name = "Single Item - Downloading")
@Composable
fun SingleItemDownloadingPreview() {
    OfflineAttachmentItem(
        attachment = OfflineAttachment(
            id = 2,
            title = "Lecture Slides - Week 2.pdf",
            url = "",
            path = "",
            status = OfflineAttachmentDownloadStatus.DOWNLOADING,
            progress = 50
        ),
        onDeleteDownload = {},
        onCancelDownload = {}
    )
}

@Preview(showBackground = true, name = "Single Item - Queued")
@Composable
fun SingleItemQueuedPreview() {
    OfflineAttachmentItem(
        attachment = OfflineAttachment(
            id = 2,
            title = "Lecture Slides - Week 2.pdf",
            url = "",
            path = "",
            status = OfflineAttachmentDownloadStatus.QUEUED,
            progress = 0
        ),
        onDeleteDownload = {},
        onCancelDownload = {}
    )
}

@Preview(showBackground = true, name = "Single Item - Failed")
@Composable
fun SingleItemFailedPreview() {
    OfflineAttachmentItem(
        attachment = OfflineAttachment(
            id = 2,
            title = "Lecture Slides - Week 2.pdf",
            url = "",
            path = "",
            status = OfflineAttachmentDownloadStatus.FAILED,
            progress = 0
        ),
        onDeleteDownload = {},
        onCancelDownload = {}
    )
}