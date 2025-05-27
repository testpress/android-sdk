package `in`.testpress.course.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.launch

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineAttachmentList(
    attachments: List<OfflineAttachment>,
    onOpenFile: (OfflineAttachment) -> Unit,
    onDeleteDownload: (Long) -> Unit,
    onCancelDownload: (Long) -> Unit
) {

    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    var selectedAttachment by rememberSaveable { mutableStateOf<OfflineAttachment?>(null) }
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp)
    ) {
        items(attachments) { attachment ->
            OfflineAttachmentItem(
                attachment = attachment,
                onOpenFile = { onOpenFile(attachment) },
                onMoreClick = {
                    showBottomSheet = !showBottomSheet
                    selectedAttachment = attachment
                    scope.launch { sheetState.show() }
                }
            )
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
                selectedAttachment = null
            },
            sheetState = sheetState,
        ) {
            selectedAttachment?.let {
                AttachmentBottomSheet(
                    attachment = it,
                    onDeleteDownload = onDeleteDownload,
                    onCancelDownload = onCancelDownload
                )
            }
        }
    }

}

@Composable
fun OfflineAttachmentItem(
    attachment: OfflineAttachment,
    onOpenFile: () -> Unit,
    onMoreClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .height(72.dp)
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onOpenFile),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = attachment.title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = getAttachmentStatusText(attachment.status, attachment.progress),
                style = MaterialTheme.typography.bodySmall
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        if (attachment.status == OfflineAttachmentDownloadStatus.DOWNLOADING) {
            CircularProgressIndicator(
                progress = attachment.progress / 100f,
                modifier = Modifier
                    .size(48.0.dp)
                    .padding(8.dp)
                    .clickable(onClick = onMoreClick),
                trackColor = Color.LightGray
            )
        } else {
            IconButton(onClick = onMoreClick) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "More"
                )
            }
        }
    }
}

fun getAttachmentStatusText(attachmentStatus: OfflineAttachmentDownloadStatus,attachmentProgress: Int): String {
    return when (attachmentStatus) {
        OfflineAttachmentDownloadStatus.DOWNLOADED -> "Tap to open"
        OfflineAttachmentDownloadStatus.FAILED -> "Download Failed"
        OfflineAttachmentDownloadStatus.QUEUED -> "Waiting to download..."
        OfflineAttachmentDownloadStatus.DOWNLOADING -> "Downloading...${attachmentProgress}%"
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

@Composable
private fun AttachmentBottomSheet(
    attachment: OfflineAttachment,
    onDeleteDownload: (Long) -> Unit,
    onCancelDownload: (Long) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        when (attachment.status) {
            OfflineAttachmentDownloadStatus.QUEUED,
            OfflineAttachmentDownloadStatus.DOWNLOADING -> {
                TextButton(onClick = { onCancelDownload(attachment.id) }) {
                    Icon(Icons.Filled.Clear, contentDescription = "Cancel", tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cancel from Download", color = Color.Black)
                }
            }
            OfflineAttachmentDownloadStatus.DOWNLOADED,
            OfflineAttachmentDownloadStatus.FAILED -> {
                TextButton(onClick = { onDeleteDownload(attachment.id) }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete from Download", color = Color.Black)
                }
            }
        }
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
            id = 3,
            title = "Lecture Slides - Week 3",
            url = "",
            path = "",
            status = OfflineAttachmentDownloadStatus.DOWNLOADING,
            progress = 0
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

@Preview(showBackground = true, name = "Attachment Bottom Sheet - Delete")
@Composable
fun AttachmentBottomSheetForDeletePreview() {
    AttachmentBottomSheet(
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

@Preview(showBackground = true, name = "Attachment Bottom Sheet - Cancel")
@Composable
fun AttachmentBottomSheetForCancelPreview() {
    AttachmentBottomSheet(
        attachment = OfflineAttachment(
            id = 2,
            title = "Lecture Slides - Week 2.pdf",
            url = "",
            path = "",
            status = OfflineAttachmentDownloadStatus.QUEUED,
            progress = 20
        ),
        onDeleteDownload = {},
        onCancelDownload = {}
    )
}