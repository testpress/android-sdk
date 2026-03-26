package `in`.testpress.course.ui

import `in`.testpress.course.R
import `in`.testpress.course.databinding.VideoTranscriptPanelBinding
import `in`.testpress.course.repository.TranscriptRepository
import `in`.testpress.course.util.TranscriptSegment
import android.content.Context
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class VideoTranscriptPanelView(
    private val onSeek: (seconds: Double) -> Unit,
    private val onCloseRequested: () -> Unit,
    private val repository: TranscriptRepository = TranscriptRepository(),
) {
    private companion object {
        private const val FOLLOW_RESUME_DELAY_MS = 1000L
        private const val PLAYBACK_POLL_INTERVAL_MS = 250L
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private lateinit var binding: VideoTranscriptPanelBinding

    private var mountedUrl: String? = null
    private var segments: List<TranscriptSegment> = emptyList()
    private var loadJob: Job? = null
    private var syncJob: Job? = null
    private var isFollowMode: Boolean = true
    private var activeIndex: Int = -1
    private var resumeFollowJob: Job? = null

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            when (newState) {
                RecyclerView.SCROLL_STATE_DRAGGING,
                RecyclerView.SCROLL_STATE_SETTLING -> {
                    resumeFollowJob?.cancel()
                    resumeFollowJob = null
                    if (isFollowMode) {
                        isFollowMode = false
                    }
                    updateJumpButtonVisibility()
                }
                RecyclerView.SCROLL_STATE_IDLE -> {
                    if (!isFollowMode && activeIndex != -1) {
                        // Give the user a moment to browse; then return to "follow" mode automatically.
                        resumeFollowJob?.cancel()
                        resumeFollowJob = scope.launch {
                            delay(FOLLOW_RESUME_DELAY_MS)
                            isFollowMode = true
                            scrollToIndexIfNeeded(activeIndex)
                            binding.jumpToCurrentButton.isVisible = false
                        }
                    }
                }
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (!isFollowMode) {
                updateJumpButtonVisibility()
            }
        }
    }

    var currentPositionSecondsProvider: (() -> Float)? = null

    private val adapter = TranscriptSegmentsAdapter(onClick = { segment -> onSeek.invoke(segment.startTimeSeconds) })

    fun createView(context: Context): View {
        if (!::binding.isInitialized) {
            binding = VideoTranscriptPanelBinding.inflate(android.view.LayoutInflater.from(context))

            binding.recycler.layoutManager = LinearLayoutManager(context)
            binding.recycler.adapter = adapter
            binding.recycler.addOnScrollListener(scrollListener)

            binding.closeButton.setOnClickListener { onCloseRequested.invoke() }
            binding.jumpToCurrentButton.setOnClickListener { jumpToCurrent() }
        }
        return binding.root
    }

    fun mount(subtitleUrl: String?) {
        val url = subtitleUrl?.trim().orEmpty()
        if (url.isBlank()) {
            mountedUrl = null
            setLoading(false)
            setMessage(R.string.transcript_not_available)
            adapter.submitList(emptyList())
            adapter.resetActiveIndex()
            activeIndex = -1
            isFollowMode = true
            resumeFollowJob?.cancel()
            resumeFollowJob = null
            binding.jumpToCurrentButton.isVisible = false
            return
        }

        if (mountedUrl == url && segments.isNotEmpty()) return

        mountedUrl = url
        isFollowMode = true
        resumeFollowJob?.cancel()
        resumeFollowJob = null
        binding.jumpToCurrentButton.isVisible = false
        loadTranscript(url)
    }

    fun startSync() {
        if (syncJob?.isActive == true) return

        syncJob = scope.launch {
            while (isActive) {
                val provider = currentPositionSecondsProvider
                val current = provider?.invoke()?.toDouble() ?: 0.0
                updateActiveIndex(current)
                delay(PLAYBACK_POLL_INTERVAL_MS)
            }
        }
    }

    fun stopSync() {
        syncJob?.cancel()
        syncJob = null
    }

    fun destroy() {
        loadJob?.cancel()
        loadJob = null
        resumeFollowJob?.cancel()
        resumeFollowJob = null
        stopSync()
        scope.cancel()
        if (::binding.isInitialized) {
            binding.recycler.removeOnScrollListener(scrollListener)
        }
        mountedUrl = null
        segments = emptyList()
    }

    private fun loadTranscript(url: String) {
        setLoading(true)
        setMessage(null)
        adapter.submitList(emptyList())
        adapter.resetActiveIndex()

        loadJob?.cancel()
        loadJob = scope.launch {
            val result = repository.getTranscriptSegments(url)
            if (result.isFailure) {
                setLoading(false)
                setMessage(R.string.transcript_loading_failed)
                return@launch
            }

            val parsed = result.getOrNull().orEmpty()
            segments = parsed
            setLoading(false)

            if (parsed.isEmpty()) {
                setMessage(R.string.transcript_not_available)
                adapter.submitList(emptyList())
                adapter.resetActiveIndex()
                activeIndex = -1
                isFollowMode = true
                binding.jumpToCurrentButton.isVisible = false
            } else {
                setMessage(null)
                adapter.submitList(parsed)
                adapter.resetActiveIndex()
                // Start in follow mode at the current position (if available).
                activeIndex = -1
                isFollowMode = true
                binding.jumpToCurrentButton.isVisible = false
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.pbLoading.isVisible = isLoading
        binding.recycler.isVisible = !isLoading
    }

    private fun setMessage(messageResId: Int?) {
        if (messageResId == null) {
            binding.message.isVisible = false
            binding.message.text = ""
        } else {
            binding.message.isVisible = true
            binding.message.setText(messageResId)
        }
    }

    private fun updateActiveIndex(timeSeconds: Double) {
        if (segments.isEmpty()) return
        val newIndex = findActiveIndex(timeSeconds)
        activeIndex = newIndex
        adapter.setActiveIndex(newIndex)

        if (newIndex == -1) {
            binding.jumpToCurrentButton.isVisible = false
            return
        }

        if (isFollowMode) {
            scrollToIndexIfNeeded(newIndex)
            binding.jumpToCurrentButton.isVisible = false
        } else {
            updateJumpButtonVisibility()
        }
    }

    private fun jumpToCurrent() {
        val index = activeIndex
        if (index == -1) return
        resumeFollowJob?.cancel()
        resumeFollowJob = null
        isFollowMode = true
        scrollToIndex(index)
        binding.jumpToCurrentButton.isVisible = false
    }

    private fun updateJumpButtonVisibility() {
        val index = activeIndex
        if (index == -1) {
            binding.jumpToCurrentButton.isVisible = false
            return
        }
        binding.jumpToCurrentButton.isVisible = !isIndexVisible(index)
    }

    private fun isIndexVisible(index: Int): Boolean {
        val lm = binding.recycler.layoutManager as? LinearLayoutManager ?: return true
        val first = lm.findFirstVisibleItemPosition()
        val last = lm.findLastVisibleItemPosition()
        if (first == RecyclerView.NO_POSITION || last == RecyclerView.NO_POSITION) return false
        return index in first..last
    }

    private fun scrollToIndexIfNeeded(index: Int) {
        if (!isIndexVisible(index)) {
            scrollToIndex(index)
        }
    }

    private fun scrollToIndex(index: Int) {
        binding.recycler.smoothScrollToPosition(index)
    }

    private fun findActiveIndex(timeSeconds: Double): Int {
        var low = 0
        var high = segments.lastIndex

        while (low <= high) {
            val mid = (low + high) ushr 1
            val s = segments[mid]
            when {
                timeSeconds >= s.startTimeSeconds && timeSeconds < s.endTimeSeconds -> return mid
                timeSeconds < s.startTimeSeconds -> high = mid - 1
                else -> low = mid + 1
            }
        }

        return -1
    }
}
