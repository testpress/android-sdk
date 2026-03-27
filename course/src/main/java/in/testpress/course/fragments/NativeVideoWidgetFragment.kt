package `in`.testpress.course.fragments

import `in`.testpress.course.R
import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.domain.getGreenDaoContent
import `in`.testpress.course.domain.VideoSubtitleJobStatus
import `in`.testpress.course.domain.jobStatusEnum
import `in`.testpress.enums.Status
import `in`.testpress.course.ui.ContentActivity.CONTENT_ID
import `in`.testpress.course.util.ExoPlayerUtil
import `in`.testpress.course.util.ExoplayerFullscreenHelper
import `in`.testpress.course.ui.VideoAISidePanelInterface
import `in`.testpress.course.ui.VideoAISidePanelView
import `in`.testpress.course.ui.VideoTranscriptSidePanelInterface
import `in`.testpress.course.ui.VideoTranscriptSidePanelView
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.airbnb.lottie.LottieAnimationView
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import `in`.testpress.network.Resource
import android.content.res.Configuration

class NativeVideoWidgetFragment : BaseVideoWidgetFragment(),
    VideoAISidePanelInterface,
    VideoTranscriptSidePanelInterface {
    private lateinit var exoPlayerMainFrame: AspectRatioFrameLayout
    private var exoPlayerUtil: ExoPlayerUtil? = null
    private var contentReloadObserver: Observer<Resource<DomainContent>>? = null
    private var pendingPositions: List<Int>? = null
    private var pendingCallbackHandler: Handler? = null
    private var pendingMarkerPositions: List<Int>? = null
    
    private val exoplayerFullscreenHelper: ExoplayerFullscreenHelper by lazy {
        ExoplayerFullscreenHelper(activity)
    }

    private var aiSidePanelView: VideoAISidePanelView? = null
    private var aiAssetId: String? = null
    private var aiNotesUrl: String? = null
    private var isAiPanelRequested: Boolean = false
    private var activeSubtitleUrl: String? = null
    private var isTranscriptPanelRequested: Boolean = false
    private var transcriptSidePanelView: VideoTranscriptSidePanelView? = null

    private enum class ActiveSidePanel {
        AI,
        TRANSCRIPT
    }

    private var activeSidePanel: ActiveSidePanel? = null

    interface VideoAIButtonHost {
        fun onVideoAIButtonClicked(isFullscreen: Boolean)
    }

    interface VideoAIPanelStateHost {
        fun onVideoAIPanelStateChanged(isOpen: Boolean)
    }

    interface VideoTranscriptButtonHost {
        fun onVideoTranscriptButtonClicked(isFullscreen: Boolean)
    }

    interface VideoTranscriptPanelStateHost {
        fun onVideoTranscriptPanelStateChanged(isOpen: Boolean)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.native_video_widget, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        val contentId = requireArguments().getLong(CONTENT_ID)

        contentReloadObserver = Observer<Resource<DomainContent>>{
            when (it.status) {
                Status.SUCCESS -> {
                    it.data?.let { domainContent ->
                        if (domainContent.video?.isTranscodingStatusComplete() == true) {
                            createAttemptAndInitializeExoplayer(it.data!!)
                        } else {
                            showVideoTranscodingScreen()
                        }
                    }
                }
                else -> {}
            }
        }

        viewModel.getContent(contentId).observe(viewLifecycleOwner, contentReloadObserver!!)

        val retryButton = exoPlayerMainFrame.findViewById<ImageButton>(R.id.retry_button)

        retryButton.setOnClickListener {
            hideVideoTranscodingScreen()

            contentReloadObserver?.let {
                viewModel.getContent(contentId).removeObservers(viewLifecycleOwner)
            }
            viewModel.getContent(contentId, true).observe(viewLifecycleOwner, contentReloadObserver!!)
        }
    }

    private fun showVideoTranscodingScreen() {
        exoPlayerMainFrame.findViewById<LottieAnimationView>(R.id.exo_player_progress).isVisible = false
        exoPlayerMainFrame.findViewById<LinearLayout>(R.id.error_message).isVisible = true
        exoPlayerMainFrame.findViewById<TextView>(R.id.error_message).setText(R.string.transcoding_message_description)
        exoPlayerMainFrame.findViewById<ImageButton>(R.id.retry_button).isVisible = true
    }

    private fun hideVideoTranscodingScreen() {
        exoPlayerMainFrame.findViewById<ImageButton>(R.id.retry_button).isVisible = false
        exoPlayerMainFrame.findViewById<LottieAnimationView>(R.id.exo_player_progress).isVisible = true
        exoPlayerMainFrame.findViewById<LinearLayout>(R.id.error_message).isVisible = false
    }

    fun bindViews(view: View) {
        exoPlayerMainFrame = view.findViewById(R.id.exo_player_main_frame)
        exoPlayerMainFrame.visibility = View.VISIBLE
        exoPlayerMainFrame.setAspectRatio(16f/9f)
        exoplayerFullscreenHelper.initializeOrientationListener()
    }

    private fun createAttemptAndInitializeExoplayer(content: DomainContent) {
        val greenDaoContent = content.getGreenDaoContent(requireContext())
        val video = content.video
        exoPlayerUtil = ExoPlayerUtil(activity, exoPlayerMainFrame, video?.getPlaybackURL(), 0F)
        exoPlayerUtil?.setContent(greenDaoContent!!)
        exoplayerFullscreenHelper.setExoplayerUtil(exoPlayerUtil)
        configureAiButton(content)
        configureTranscriptButton(content)
        registerPendingCallbacks()
        viewModel.createContentAttempt(content.id)
            .observe(viewLifecycleOwner, Observer { resource ->
                when(resource.status) {
                    Status.SUCCESS -> {
                        val contentAttempt = resource.data!!
                        val videoStartPosition = contentAttempt.video?.lastPosition?.toFloat() ?: 0F
                        val contentAttemptObjectId = contentAttempt.objectId?.toLong()
                        exoPlayerUtil?.setStartPosition(videoStartPosition)
                        exoPlayerUtil?.setVideoAttemptParameters(contentAttemptObjectId?:-1, greenDaoContent!!)
                        exoPlayerUtil?.initializePlayer()
                    }
                    else -> {
                        exoPlayerUtil?.setVideoAttemptParameters(-1, greenDaoContent!!)
                        exoPlayerUtil?.initializePlayer()
                    }
                }
            })

        exoPlayerUtil?.setOnSidePanelReadyListener {
            if (isAiPanelRequested) {
                aiAssetId?.let { assetId ->
                    attachAiToSidePanel(assetId, aiNotesUrl)
                }
            } else if (isTranscriptPanelRequested) {
                activeSubtitleUrl?.let { subtitleUrl ->
                    attachTranscriptToSidePanel(subtitleUrl)
                }
            }
        }
    }

    private fun isVideoAIEnabled(content: DomainContent): Boolean {
        return content.canEnableLearnLensAI == true && !content.learnlensAssetId.isNullOrBlank()
    }

    private fun isVideoTranscriptEnabled(content: DomainContent): Boolean {
        val subtitle = content.videoSubtitle
        val url = subtitle?.url
        val isReady = subtitle?.jobStatusEnum() == VideoSubtitleJobStatus.COMPLETED
        return content.enableTranscript == true && isReady && !url.isNullOrBlank()
    }

    private fun configureAiButton(content: DomainContent) {
        val enabled = isVideoAIEnabled(content)
        aiAssetId = content.learnlensAssetId
        aiNotesUrl = content.aiNotesUrl

        exoPlayerUtil?.setPlayerActionButtonVisible(R.id.exo_ai_button, enabled)
        if (enabled) {
            exoPlayerUtil?.setPlayerActionButtonOnClickListener(R.id.exo_ai_button) { handleAiButtonClick() }
        }
    }

    private fun configureTranscriptButton(content: DomainContent) {
        val enabled = isVideoTranscriptEnabled(content)
        activeSubtitleUrl = content.videoSubtitle?.url

        exoPlayerUtil?.setPlayerActionButtonVisible(R.id.exo_transcript_button, enabled)
        if (enabled) {
            exoPlayerUtil?.setPlayerActionButtonOnClickListener(R.id.exo_transcript_button) { handleTranscriptButtonClick() }
        }
    }

    private fun handleAiButtonClick() {
        val util = exoPlayerUtil ?: return
        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        
        if (util.isFullscreen || isLandscape) {
            toggleAiSidePanel()
        } else {
            getVideoAIHost()?.onVideoAIButtonClicked(false)
        }
    }

    private fun handleTranscriptButtonClick() {
        val util = exoPlayerUtil ?: return
        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        if (util.isFullscreen || isLandscape) {
            toggleTranscriptSidePanel()
        } else {
            getVideoTranscriptHost()?.onVideoTranscriptButtonClicked(false)
        }
    }

    private fun getVideoAIHost(): VideoAIButtonHost? {
        return (parentFragment as? VideoAIButtonHost) ?: (activity as? VideoAIButtonHost)
    }

    private fun getVideoAIPanelStateHost(): VideoAIPanelStateHost? {
        return (parentFragment as? VideoAIPanelStateHost) ?: (activity as? VideoAIPanelStateHost)
    }

    private fun getVideoTranscriptHost(): VideoTranscriptButtonHost? {
        return (parentFragment as? VideoTranscriptButtonHost) ?: (activity as? VideoTranscriptButtonHost)
    }

    private fun getVideoTranscriptPanelStateHost(): VideoTranscriptPanelStateHost? {
        return (parentFragment as? VideoTranscriptPanelStateHost) ?: (activity as? VideoTranscriptPanelStateHost)
    }

    override fun showVideoAISidePanel(assetId: String, notesUrl: String?) {
        closeTranscriptSidePanel(notifyHost = true)
        isAiPanelRequested = true
        activeSidePanel = ActiveSidePanel.AI
        getVideoAIPanelStateHost()?.onVideoAIPanelStateChanged(true)
        attachAiToSidePanel(assetId, notesUrl)
    }

    private fun attachAiToSidePanel(assetId: String, notesUrl: String?) {
        val util = exoPlayerUtil ?: return
        val act = activity ?: return

        if (util.isSidePanelAvailable) {
            val view = getOrCreateAiSidePanelView(act, util)
            util.showSidePanel(view.createView(act))
            view.mount(assetId, notesUrl)
            activeSidePanel = ActiveSidePanel.AI
        }
    }

    private fun getOrCreateAiSidePanelView(act: android.app.Activity, util: ExoPlayerUtil): VideoAISidePanelView {
        return aiSidePanelView ?: VideoAISidePanelView(
            activity = act,
            onSeek = { seconds -> util.seekTo((seconds * 1000).toLong()) },
            onCloseRequested = { 
                isAiPanelRequested = false
                util.hideSidePanel()
                getVideoAIPanelStateHost()?.onVideoAIPanelStateChanged(false)
            }
        ).also { aiSidePanelView = it }
    }

    private fun toggleAiSidePanel() {
        val util = exoPlayerUtil ?: return
        
        if (util.isSidePanelVisible && activeSidePanel == ActiveSidePanel.AI) {
            isAiPanelRequested = false
            util.hideSidePanel()
            activeSidePanel = null
            getVideoAIPanelStateHost()?.onVideoAIPanelStateChanged(false)
        } else {
            aiAssetId?.let { showVideoAISidePanel(it, aiNotesUrl) }
        }
    }

    override fun hideVideoAISidePanel(notifyHost: Boolean) {
        isAiPanelRequested = false
        if (activeSidePanel == ActiveSidePanel.AI) {
            exoPlayerUtil?.hideSidePanel()
            activeSidePanel = null
        }
        if (notifyHost) {
            getVideoAIPanelStateHost()?.onVideoAIPanelStateChanged(false)
        }
    }

    override fun showVideoTranscriptSidePanel(subtitleUrl: String) {
        closeAiSidePanel(notifyHost = true)
        isTranscriptPanelRequested = true
        activeSubtitleUrl = subtitleUrl
        activeSidePanel = ActiveSidePanel.TRANSCRIPT
        getVideoTranscriptPanelStateHost()?.onVideoTranscriptPanelStateChanged(true)
        attachTranscriptToSidePanel(subtitleUrl)
    }

    private fun attachTranscriptToSidePanel(subtitleUrl: String) {
        val util = exoPlayerUtil ?: return
        val act = activity ?: return

        if (util.isSidePanelAvailable) {
            val view = getOrCreateTranscriptSidePanelView(util)
            util.showSidePanel(view.createView(act))
            view.mount(subtitleUrl)
            activeSidePanel = ActiveSidePanel.TRANSCRIPT
        }
    }

    private fun getOrCreateTranscriptSidePanelView(util: ExoPlayerUtil): VideoTranscriptSidePanelView {
        return transcriptSidePanelView ?: VideoTranscriptSidePanelView(
            onSeek = { seconds -> util.seekTo((seconds * 1000).toLong()) },
            onCloseRequested = {
                closeTranscriptSidePanel(notifyHost = true, util = util)
            },
            currentPositionSecondsProvider = { util.getCurrentPosition() },
        ).also { transcriptSidePanelView = it }
    }

    private fun toggleTranscriptSidePanel() {
        val util = exoPlayerUtil ?: return

        if (util.isSidePanelVisible && activeSidePanel == ActiveSidePanel.TRANSCRIPT) {
            closeTranscriptSidePanel(notifyHost = true, util = util)
        } else {
            activeSubtitleUrl?.let { showVideoTranscriptSidePanel(it) }
        }
    }

    override fun hideVideoTranscriptSidePanel(notifyHost: Boolean) {
        closeTranscriptSidePanel(notifyHost = notifyHost)
    }

    private fun closeAiSidePanel(notifyHost: Boolean) {
        isAiPanelRequested = false
        if (activeSidePanel == ActiveSidePanel.AI) {
            exoPlayerUtil?.hideSidePanel()
            activeSidePanel = null
        }
        if (notifyHost) {
            getVideoAIPanelStateHost()?.onVideoAIPanelStateChanged(false)
        }
    }

    private fun closeTranscriptSidePanel(notifyHost: Boolean, util: ExoPlayerUtil? = exoPlayerUtil) {
        isTranscriptPanelRequested = false
        if (activeSidePanel == ActiveSidePanel.TRANSCRIPT) {
            transcriptSidePanelView?.onHidden()
            util?.hideSidePanel()
            activeSidePanel = null
        }
        if (notifyHost) {
            getVideoTranscriptPanelStateHost()?.onVideoTranscriptPanelStateChanged(false)
        }
    }

    override fun seekTo(milliSeconds: Long?) {
        exoPlayerUtil?.seekTo(milliSeconds)
    }

    fun getCurrentPositionSeconds(): Float {
        return exoPlayerUtil?.getCurrentPosition() ?: 0f
    }

    override fun onPause() {
        super.onPause()
        exoPlayerUtil?.onPause()
    }

    override fun onResume() {
        super.onResume()
        exoPlayerUtil?.onResume()
    }

    override fun onStart() {
        super.onStart()
        exoPlayerUtil?.onStart()
    }

    override fun onStop() {
        super.onStop()
        exoPlayerUtil?.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        aiSidePanelView?.destroy()
        aiSidePanelView = null
        transcriptSidePanelView?.destroy()
        transcriptSidePanelView = null
        exoplayerFullscreenHelper?.disableOrientationListener()
    }

    override val enabledVideoQuestion: Boolean = true

    fun registerPositionCallbacks(
        positions: List<Int>,
        callbackHandler: Handler
    ) {
        if (exoPlayerUtil != null) {
            exoPlayerUtil?.registerPositionCallbacks(positions, callbackHandler)
            pendingMarkerPositions?.let {
                exoPlayerUtil?.addPlaybackMarkers(it)
                pendingMarkerPositions = null
            }
        } else {
            pendingPositions = positions
            pendingCallbackHandler = callbackHandler
        }
    }

    fun addPlaybackMarkers(positions: List<Int>) {
        if (exoPlayerUtil != null) {
            exoPlayerUtil?.addPlaybackMarkers(positions)
        } else {
            pendingMarkerPositions = positions
        }
    }
    private fun registerPendingCallbacks() {
        if (exoPlayerUtil != null) {
            pendingPositions?.let { positions ->
                pendingCallbackHandler?.let { handler ->
                    exoPlayerUtil?.registerPositionCallbacks(positions, handler)
                    pendingPositions = null
                    pendingCallbackHandler = null
                }
            }
            pendingMarkerPositions?.let { positions ->
                exoPlayerUtil?.addPlaybackMarkers(positions)
                pendingMarkerPositions = null
            }
        }
    }

    fun pauseVideo() {
        exoPlayerUtil?.pauseVideo()
    }

    fun playVideo() {
        exoPlayerUtil?.playVideo()
    }
}
