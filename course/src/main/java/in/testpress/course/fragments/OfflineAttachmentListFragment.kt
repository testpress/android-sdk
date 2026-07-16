package `in`.testpress.course.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import `in`.testpress.course.R
import `in`.testpress.course.helpers.OfflineAttachmentSyncManager
import `in`.testpress.course.util.extension.debouncedClickable
import `in`.testpress.course.viewmodels.OfflineAttachmentViewModel
import `in`.testpress.database.TestpressDatabase
import `in`.testpress.database.entities.OfflineAttachment
import `in`.testpress.database.entities.OfflineAttachmentDownloadStatus
import kotlinx.coroutines.launch

class OfflineAttachmentListFragment : Fragment() {

    private lateinit var viewModel: OfflineAttachmentViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = OfflineAttachmentViewModel.get(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                OfflineAttachmentScreen(
                    viewModel = viewModel,
                    onMoreClick = { attachment ->
                        if (childFragmentManager.findFragmentByTag(OfflineAttachmentOptionsBottomSheet.TAG) == null) {
                            val bottomSheet = OfflineAttachmentOptionsBottomSheet.newInstance(attachment.id)
                            bottomSheet.setListeners(
                                onDelete = { latestAttachment -> viewModel.delete(requireContext(), latestAttachment) },
                                onCancel = { latestAttachment -> viewModel.cancel(requireContext(), latestAttachment) }
                            )
                            bottomSheet.show(childFragmentManager, OfflineAttachmentOptionsBottomSheet.TAG)
                        }
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            val dao = TestpressDatabase.invoke(requireContext()).offlineAttachmentDao()
            OfflineAttachmentSyncManager(requireContext(), dao).syncDownloads()
        }
    }
}

class OfflineAttachmentOptionsBottomSheet : BottomSheetDialogFragment() {
    private var attachmentId: Long = -1L
    private var onDeleteDownload: ((OfflineAttachment) -> Unit)? = null
    private var onCancelDownload: ((OfflineAttachment) -> Unit)? = null

    fun setListeners(onDelete: (OfflineAttachment) -> Unit, onCancel: (OfflineAttachment) -> Unit) {
        this.onDeleteDownload = onDelete
        this.onCancelDownload = onCancel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        attachmentId = arguments?.getLong(ARG_ATTACHMENT_ID, -1L) ?: -1L
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewModel = OfflineAttachmentViewModel.get(requireActivity())
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val attachments by viewModel.files.collectAsState()
                val currentAttachment = attachments?.firstOrNull { it.id == attachmentId }

                currentAttachment?.let { attachment ->
                    Surface(color = Color.White) {
                        AttachmentBottomSheet(
                            attachment = attachment,
                            onDeleteDownload = { att ->
                                onDeleteDownload?.invoke(att)
                                dismiss()
                            },
                            onCancelDownload = { att ->
                                onCancelDownload?.invoke(att)
                                dismiss()
                            },
                            onDismiss = { dismiss() }
                        )
                    }
                } ?: run {
                    LaunchedEffect(Unit) {
                        dismiss()
                    }
                }
            }
        }
    }

    companion object {
        const val TAG = "OfflineAttachmentOptionsBottomSheet"
        private const val ARG_ATTACHMENT_ID = "arg_attachment_id"

        fun newInstance(attachmentId: Long): OfflineAttachmentOptionsBottomSheet {
            return OfflineAttachmentOptionsBottomSheet().apply {
                arguments = Bundle().apply {
                    putLong(ARG_ATTACHMENT_ID, attachmentId)
                }
            }
        }
    }
}

@Composable
fun OfflineAttachmentScreen(
    viewModel: OfflineAttachmentViewModel,
    onMoreClick: (OfflineAttachment) -> Unit
) {
    val attachments by viewModel.files.collectAsState()
    val context = LocalContext.current

    when {
        attachments == null -> {
            LoadingState()
        }

        attachments!!.isEmpty() -> {
            EmptyState()
        }

        else -> {
            OfflineAttachmentList(
                attachments = attachments!!,
                onOpenFile = { file ->
                    if (file.status == OfflineAttachmentDownloadStatus.COMPLETED) {
                        viewModel.openFile(context, file)
                    }
                },
                onMoreClick = onMoreClick
            )
        }
    }
}

@Composable
fun LoadingState() {
    val configuration = LocalConfiguration.current
    val screenHeightDp = configuration.screenHeightDp.dp
    val itemHeight = 72.dp
    val repeatCount = (screenHeightDp / itemHeight).toInt().coerceAtLeast(1)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(8.dp)
    ) {
        items(repeatCount) {
            OfflineAttachmentItemShimmer()
        }
    }
}

@Composable
fun OfflineAttachmentItemShimmer() {
    val brush = shimmerBrush()
    Row(
        modifier = Modifier
            .height(72.dp)
            .fillMaxWidth()
            .padding(start = 16.dp, top = 8.dp, end = 4.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(20.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(brush)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Spacer(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(14.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(brush)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(brush)
        )
    }
}

@Composable
fun shimmerBrush(): Brush {
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.6f)
    )

    val transition = rememberInfiniteTransition(label = "")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,  // large enough to move gradient across
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = ""
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = androidx.compose.ui.geometry.Offset(translateAnim, 0f),
        end = androidx.compose.ui.geometry.Offset(translateAnim + 200f, 200f)
    )
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.baseline_attach_file_24),
            contentDescription = "No Downloads",
            modifier = Modifier
                .height(96.dp)
                .width(96.dp),
            tint = Color(0xFFE77528)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Nothing Here",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Looks like you’ve not downloaded any attachments as of now.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun OfflineAttachmentList(
    attachments: List<OfflineAttachment>,
    onOpenFile: (OfflineAttachment) -> Unit,
    onMoreClick: (OfflineAttachment) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(vertical = 8.dp)
    ) {
        items(attachments, key = { it.id }) { attachment ->
            OfflineAttachmentItem(
                attachment = attachment,
                onOpenFile = { onOpenFile(attachment) },
                onMoreClick = { onMoreClick(attachment) }
            )
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
            .padding(start = 16.dp, top = 8.dp, end = 4.dp, bottom = 16.dp)
            .debouncedClickable(debounceDuration = 500, onClick = onOpenFile),
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

@Composable
private fun AttachmentBottomSheet(
    attachment: OfflineAttachment,
    onDeleteDownload: (OfflineAttachment) -> Unit,
    onCancelDownload: (OfflineAttachment) -> Unit,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        when (attachment.status) {
            OfflineAttachmentDownloadStatus.QUEUED,
            OfflineAttachmentDownloadStatus.DOWNLOADING -> {
                TextButton(
                    onClick = {
                        onCancelDownload(attachment)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Clear, contentDescription = "Cancel", tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cancel from Download", color = Color.Black)
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            OfflineAttachmentDownloadStatus.COMPLETED,
            OfflineAttachmentDownloadStatus.DELETE,
            OfflineAttachmentDownloadStatus.FAILED -> {
                TextButton(
                    onClick = {
                        onDeleteDownload(attachment)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete from Download", color = Color.Black)
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoadingStatePreview() {
    LoadingState()
}

@Preview(showBackground = true, name = "Empty State Preview")
@Composable
fun EmptyStatePreview() {
    EmptyState()
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
            contentUri = "content://media/downloaded/110111",
            downloadId = 100,
            status = OfflineAttachmentDownloadStatus.COMPLETED,
            progress = 100
        ),
        OfflineAttachment(
            id = 2,
            title = "Lecture Slides - Week 2",
            url = "",
            path = "",
            contentUri = "content://media/downloaded/110111",
            downloadId = 100,
            status = OfflineAttachmentDownloadStatus.DOWNLOADING,
            progress = 50
        ),
        OfflineAttachment(
            id = 3,
            title = "Lecture Slides - Week 3",
            url = "",
            path = "",
            contentUri = "content://media/downloaded/110111",
            downloadId = 100,
            status = OfflineAttachmentDownloadStatus.DOWNLOADING,
            progress = 0
        ),
        OfflineAttachment(
            id = 5,
            title = "Corrupted",
            url = "",
            path = "",
            contentUri = "content://media/downloaded/110111",
            downloadId = 100,
            status = OfflineAttachmentDownloadStatus.FAILED
        ),
        OfflineAttachment(
            id = 6,
            title = "QUEUED",
            url = "",
            path = "",
            contentUri = "content://media/downloaded/110111",
            downloadId = 100,
            status = OfflineAttachmentDownloadStatus.QUEUED
        )
    )

    OfflineAttachmentList(
        attachments = sampleAttachments,
        onOpenFile = {},
        onMoreClick = {}
    )
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
            contentUri = "content://media/downloaded/110111",
            downloadId = 100,
            status = OfflineAttachmentDownloadStatus.FAILED,
            progress = 0
        ),
        onDeleteDownload = {},
        onCancelDownload = {},
        onDismiss = {}
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
            contentUri = "content://media/downloaded/110111",
            downloadId = 100,
            status = OfflineAttachmentDownloadStatus.QUEUED,
            progress = 20
        ),
        onDeleteDownload = {},
        onCancelDownload = {},
        onDismiss = {}
    )
}