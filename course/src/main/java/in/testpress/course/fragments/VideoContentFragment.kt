package `in`.testpress.course.fragments

import `in`.testpress.WebViewConstants
import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.R
import `in`.testpress.course.domain.DomainVideoContent
import `in`.testpress.course.helpers.DownloadTask
import `in`.testpress.course.repository.OfflineVideoRepository
import `in`.testpress.course.services.VideoDownloadService
import `in`.testpress.course.ui.DownloadsActivity
import `in`.testpress.course.ui.VideoDownloadQualityChooserDialog
import `in`.testpress.util.DateUtils.convertDurationStringToSeconds
import `in`.testpress.course.util.PatternEditableBuilder
import `in`.testpress.course.ui.VideoAISidePanelInterface
import `in`.testpress.course.ui.VideoToolsPanelFragmentSwitcher
import `in`.testpress.course.ui.VideoTranscriptSidePanelInterface
import `in`.testpress.course.domain.VideoSubtitleJobStatus
import `in`.testpress.course.domain.jobStatusEnum
import `in`.testpress.course.viewmodels.OfflineVideoViewModel
import `in`.testpress.models.InstituteSettings
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.netopen.hotbitmapgg.library.view.RingProgressBar
import `in`.testpress.course.repository.ContentRepository
import `in`.testpress.course.viewmodels.VideoQuestionViewModel
import `in`.testpress.course.network.NetworkVideoQuestion
import `in`.testpress.enums.Status
import `in`.testpress.core.TestpressException
import android.widget.Toast
import android.content.res.Configuration
import androidx.core.view.isVisible
import java.util.regex.Pattern
import android.text.SpannableStringBuilder

open class VideoContentFragment : BaseContentDetailFragment(),
    VideoQuestionSheetFragment.OnQuestionCompleteListener,
    NativeVideoWidgetFragment.VideoAIButtonHost,
    NativeVideoWidgetFragment.VideoAIPanelStateHost,
    NativeVideoWidgetFragment.VideoTranscriptButtonHost,
    NativeVideoWidgetFragment.VideoTranscriptPanelStateHost,
    VideoTranscriptHost,
    VideoAIFragment.Host {
    protected lateinit var titleView: TextView
    protected lateinit var description: TextView
    protected lateinit var titleLayout: LinearLayout
    protected lateinit var videoWidgetFragment: BaseVideoWidgetFragment
    protected lateinit var offlineVideoViewModel: OfflineVideoViewModel
    protected lateinit var videoDownloadProgress: RingProgressBar
    protected lateinit var menu: Menu
    protected lateinit var instituteSettings: InstituteSettings;
    protected var remainingDownloadCount :Int? = null

    private lateinit var contentRepository: ContentRepository
    private lateinit var videoQuestionViewModel: VideoQuestionViewModel
    private var askAiFab: View? = null
    private var transcriptFab: View? = null
    private var videoToolsPanelContainer: ViewGroup? = null
    private var videoToolsPanelSwitcher: VideoToolsPanelFragmentSwitcher? = null
    private var openPanel: OpenPanel? = null
    
    private val nativeVideoWidgetFragment: NativeVideoWidgetFragment?
        get() = if (::videoWidgetFragment.isInitialized) videoWidgetFragment as? NativeVideoWidgetFragment else null

    companion object {
        private const val STATE_OPEN_PANEL = "state_open_panel"
    }

    private enum class OpenPanel {
        AI,
        TRANSCRIPT,
    }

    private fun showVideoToolsAiPanel(fragment: VideoAIFragment) {
        videoToolsPanelSwitcher?.showAi(fragment)
    }

    private fun showVideoToolsTranscriptPanel(fragment: VideoTranscriptFragment) {
        videoToolsPanelSwitcher?.showTranscript(fragment)
    }

    private fun hideVideoToolsAiPanel(remove: Boolean = false) {
        videoToolsPanelSwitcher?.hideAi(remove = remove)
    }

    private fun hideVideoToolsTranscriptPanel(remove: Boolean = false) {
        videoToolsPanelSwitcher?.hideTranscript(remove = remove)
    }

    private fun hideAllVideoToolsPanels(remove: Boolean = false) {
        videoToolsPanelSwitcher?.hideAll(remove = remove)
    }

    private val questionCallbackHandler = Handler(Looper.getMainLooper()) { message ->
        val positionInSeconds = message.what.toLong()
        handleQuestionTrigger(positionInSeconds) 
        true
    }

    override var isBookmarkEnabled: Boolean
        get() = false
        set(value) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        openPanel = savedInstanceState
            ?.getString(STATE_OPEN_PANEL)
            ?.let { name ->
                try {
                    OpenPanel.valueOf(name)
                } catch (_: IllegalArgumentException) {
                    null
                }
            }
        VideoDownloadService.start(requireContext())
        offlineVideoViewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return OfflineVideoViewModel(OfflineVideoRepository(requireContext())) as T
            }
        }).get(OfflineVideoViewModel::class.java)

        videoQuestionViewModel = ViewModelProvider(this).get(VideoQuestionViewModel::class.java)
        contentRepository = ContentRepository(requireContext())
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(STATE_OPEN_PANEL, openPanel?.name)
        super.onSaveInstanceState(outState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.video_content_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        titleView = view.findViewById(R.id.title)
        description = view.findViewById(R.id.description)
        titleLayout = view.findViewById(R.id.title_layout)
        askAiFab = view.findViewById(R.id.ask_ai_fab)
        askAiFab?.setOnClickListener { toggleVideoAIPanel() }
        transcriptFab = view.findViewById(R.id.transcript_fab)
        transcriptFab?.setOnClickListener { toggleVideoTranscriptPanel() }
        videoToolsPanelContainer = view.findViewById(R.id.video_tools_panel_container)
        videoToolsPanelContainer?.let { container ->
            videoToolsPanelSwitcher = VideoToolsPanelFragmentSwitcher(
                fragmentManager = childFragmentManager,
                container = container,
                containerId = R.id.video_tools_panel_container,
            )
        }
        initializeListeners()
        instituteSettings = TestpressSdk.getTestpressSession(requireContext())!!.instituteSettings;
        initializeRemainingDownloadsCount()
    }

    private fun initializeRemainingDownloadsCount(){
        offlineVideoViewModel.offlineVideos.observe(viewLifecycleOwner){
            if (instituteSettings.maxAllowedDownloadedVideos != null && instituteSettings.maxAllowedDownloadedVideos != 0){
                remainingDownloadCount = instituteSettings.maxAllowedDownloadedVideos - it.size
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        if (instituteSettings.isVideoDownloadEnabled) {
            inflater.inflate(R.menu.video_content_menu, menu)
        }
        this.menu = menu
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if (instituteSettings.isVideoDownloadEnabled) {
            setProgressBarInMenuItem()
        }
    }

    private fun setProgressBarInMenuItem() {
        menu.findItem(R.id.download_progress).setActionView(R.layout.download_progress)
        val progressView = menu.findItem(R.id.download_progress).actionView
        progressView?.setOnClickListener {
            requireContext().startActivity(DownloadsActivity.createIntent(requireContext()))
        }
        videoDownloadProgress = progressView?.findViewById(R.id.video_download_progress)!!
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.download -> {
                showDownloadDialog()
                true
            }
            R.id.downloaded -> {
                requireContext().startActivity(DownloadsActivity.createIntent(requireContext()))
                true
            }
            R.id.download_progress -> {
                requireContext().startActivity(DownloadsActivity.createIntent(requireContext()))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showDownloadUnavailableDialog(errorReason: VideoDownloadError) {
        val builder =
            AlertDialog.Builder(requireContext(), R.style.TestpressAppCompatAlertDialogStyle)
        builder.setTitle(getDialogTitle(errorReason))
        builder.setMessage(getDialogErrorMessage(errorReason))
        builder.setPositiveButton("Ok", null)
        builder.show()
    }

    private fun getDialogTitle(errorReason: VideoDownloadError):String {
        return when(errorReason) {
            VideoDownloadError.COURSE_NOT_PURCHASED -> "Download Unavailable"
            VideoDownloadError.DOWNLOAD_LIMIT_REACHED -> "Maximum download limit reached"
        }
    }

    private fun getDialogErrorMessage(errorReason: VideoDownloadError):String{
        return when(errorReason) {
            VideoDownloadError.COURSE_NOT_PURCHASED -> {
                "This content is not available for download, please purchase it to watch it in offline."
            }
            VideoDownloadError.DOWNLOAD_LIMIT_REACHED -> {
                "You have reached the maximum download limit of ${instituteSettings.maxAllowedDownloadedVideos}. Delete one or more videos to download this video."
            }
        }
    }

    private fun showDownloadDialog() {
        if (content.isCourseNotPurchased) {
            showDownloadUnavailableDialog(VideoDownloadError.COURSE_NOT_PURCHASED)
            return
        } else if (remainingDownloadCount != null && remainingDownloadCount!! < 1){
            showDownloadUnavailableDialog(VideoDownloadError.DOWNLOAD_LIMIT_REACHED)
            return
        }
        val videoQualityChooserDialog =
            VideoDownloadQualityChooserDialog(content)
        videoQualityChooserDialog.show(childFragmentManager, null)
        videoQualityChooserDialog.setOnSubmitListener {downloadRequest ->
            DownloadTask(downloadRequest.uri.toString(), requireContext()).start(downloadRequest, content)
        }
    }

    private fun initializeListeners() {
        titleLayout.setOnClickListener {
            if (!isContentInitialized() || content.description.isNullOrBlank()) return@setOnClickListener
            val isExpanded = description.isVisible
            toggleDescription(!isExpanded)
        }
    }

    private fun toggleDescription(show: Boolean) {
        val hasDescription = isContentInitialized() && !content.description.isNullOrBlank()
        if (!hasDescription) {
            description.visibility = View.GONE
            titleView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            updateVideoToolsUIState()
            return
        }

        description.visibility = if (show) View.VISIBLE else View.GONE
        titleView.setCompoundDrawablesWithIntrinsicBounds(
            0,
            0,
            if (show) R.drawable.ic_up_chevron else R.drawable.ic_down_chevron,
            0,
        )
        updateVideoToolsUIState()
    }

    override fun display() {
        if (content.video?.isViewsExhausted == true) {
            emptyViewFragment.showViewsExhaustedMessage()
            setHasOptionsMenu(false)
            return
        }
        titleView.text = content.title
        videoWidgetFragment = VideoWidgetFragmentFactory.getWidget(content.video!!)
        videoWidgetFragment.arguments = arguments
        parseVideoDescription()
        updateVideoToolsUIState()
        if (content.video!!.isDownloadable() && instituteSettings.isVideoDownloadEnabled) {
            showDownloadStatus()
        } else if (::menu.isInitialized) {
            menu.clear()
        }
        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.video_widget_fragment, videoWidgetFragment)
        transaction.runOnCommit {
            syncOpenPanelForOrientation()
        }
        transaction.commit()
        if (videoWidgetFragment.enabledVideoQuestion) {
            fetchVideoQuestions()
        }
    }

    override fun onResume() {
        super.onResume()
        if(isContentInitialized() && instituteSettings.isVideoDownloadEnabled && content.video!!.isDownloadable()) {
            showDownloadStatus()
        }
        updateVideoToolsUIState()
        syncOpenPanelForOrientation()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateVideoToolsUIState()
        syncOpenPanelForOrientation()
    }

    override fun onDestroyView() {
        hideAllVideoToolsPanels(remove = true)
        videoToolsPanelSwitcher = null
        videoToolsPanelContainer = null
        askAiFab = null
        transcriptFab = null
        super.onDestroyView()
    }

    private fun canUseVideoAI(): Boolean {
        if (!::videoWidgetFragment.isInitialized) return false
        return videoWidgetFragment is VideoAISidePanelInterface &&
               content.canEnableLearnLensAI == true && 
               !content.learnlensAssetId.isNullOrBlank()
    }

    private fun canUseVideoTranscript(): Boolean {
        if (!::videoWidgetFragment.isInitialized) return false
        val subtitle = content.videoSubtitle
        val url = subtitle?.url
        val isReady = subtitle?.jobStatusEnum() == VideoSubtitleJobStatus.COMPLETED
        return nativeVideoWidgetFragment != null &&
                content.enableTranscript == true &&
                isReady &&
                !url.isNullOrBlank()
    }

    private fun updateVideoToolsUIState() {
        val isPortrait = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        val showFabs = isPortrait && openPanel == null
        askAiFab?.isVisible = canUseVideoAI() && showFabs
        transcriptFab?.isVisible = canUseVideoTranscript() && showFabs

        // Avoid swipe-to-refresh gesture fighting with panel scrolling/interactions.
        val isDescriptionExpanded = description.isVisible
        val canSwipeRefresh = !isContentInitialized() ||
            content.description.isNullOrBlank() ||
            !isDescriptionExpanded
        swipeRefresh.isEnabled = openPanel == null && canSwipeRefresh
    }

    private fun toggleVideoAIPanel() {
        val config = getVideoAIConfigOrNull() ?: return
        if (openPanel == OpenPanel.AI) {
            closeVideoAIPanel()
        } else {
            if (openPanel == OpenPanel.TRANSCRIPT) closeVideoTranscriptPanel()
            openVideoAIPanel(config)
        }
    }

    private data class VideoAIConfig(
        val assetId: String,
        val notesUrl: String?,
    )

    private fun getVideoAIConfigOrNull(): VideoAIConfig? {
        if (!canUseVideoAI()) return null
        val assetId = content.learnlensAssetId ?: return null
        if (assetId.isBlank()) return null
        return VideoAIConfig(assetId = assetId, notesUrl = content.aiNotesUrl)
    }

    private fun getVideoAISidePanelContractOrNull(): VideoAISidePanelInterface? {
        return nativeVideoWidgetFragment
    }

    private fun openVideoAIPanel(config: VideoAIConfig) {
        openPanel = OpenPanel.AI
        showVideoAIPanelForCurrentOrientation(config)
        updateVideoToolsUIState()
    }

    private fun closeVideoAIPanel() {
        openPanel = null
        hideVideoAIPanel()
        updateVideoToolsUIState()
    }

    private fun hideVideoAIPanel(notifySidePanel: Boolean = true) {
        getVideoAISidePanelContractOrNull()?.hideVideoAISidePanel(notifyHost = notifySidePanel)
        hideVideoToolsAiPanel()
    }

    private data class VideoTranscriptConfig(
        val subtitleUrl: String,
    )

    private fun getVideoTranscriptConfigOrNull(): VideoTranscriptConfig? {
        if (!canUseVideoTranscript()) return null
        val url = content.videoSubtitle?.url?.trim().orEmpty()
        if (url.isBlank()) return null
        return VideoTranscriptConfig(subtitleUrl = url)
    }

    private fun getVideoTranscriptSidePanelContractOrNull(): VideoTranscriptSidePanelInterface? {
        return nativeVideoWidgetFragment
    }

    private fun toggleVideoTranscriptPanel() {
        val config = getVideoTranscriptConfigOrNull() ?: return
        if (openPanel == OpenPanel.TRANSCRIPT) {
            closeVideoTranscriptPanel()
        } else {
            if (openPanel == OpenPanel.AI) closeVideoAIPanel()
            openVideoTranscriptPanel(config)
        }
    }

    private fun openVideoTranscriptPanel(config: VideoTranscriptConfig) {
        openPanel = OpenPanel.TRANSCRIPT
        showVideoTranscriptPanelForCurrentOrientation(config)
        updateVideoToolsUIState()
    }

    private fun closeVideoTranscriptPanel() {
        openPanel = null
        hideVideoTranscriptPanel()
        updateVideoToolsUIState()
    }

    private fun hideVideoTranscriptPanel(notifySidePanel: Boolean = true) {
        getVideoTranscriptSidePanelContractOrNull()?.hideVideoTranscriptSidePanel(notifyHost = notifySidePanel)
        hideVideoToolsTranscriptPanel()
    }

    private fun syncOpenPanelForOrientation() {
        val aiConfig = getVideoAIConfigOrNull()
        val transcriptConfig = getVideoTranscriptConfigOrNull()

        when (openPanel) {
            OpenPanel.AI -> {
                if (aiConfig == null) {
                    closeVideoAIPanel()
                    return
                }
                hideVideoTranscriptPanel(notifySidePanel = false)
                showVideoAIPanelForCurrentOrientation(aiConfig)
            }
            OpenPanel.TRANSCRIPT -> {
                if (transcriptConfig == null) {
                    closeVideoTranscriptPanel()
                    return
                }
                hideVideoAIPanel(notifySidePanel = false)
                showVideoTranscriptPanelForCurrentOrientation(transcriptConfig)
            }
            null -> {
                hideVideoAIPanel(notifySidePanel = false)
                hideVideoTranscriptPanel(notifySidePanel = false)
            }
        }
    }

    private fun showVideoAIPanelForCurrentOrientation(config: VideoAIConfig) {
        val sidePanel = getVideoAISidePanelContractOrNull() ?: return

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            hideVideoToolsAiPanel()
            sidePanel.showVideoAISidePanel(config.assetId, config.notesUrl)
        } else {
            sidePanel.hideVideoAISidePanel(notifyHost = false)
            showVideoToolsAiPanel(VideoAIFragment.newInstance(config.assetId, config.notesUrl))
        }
    }

    private fun showVideoTranscriptPanelForCurrentOrientation(config: VideoTranscriptConfig) {
        val sidePanel = getVideoTranscriptSidePanelContractOrNull() ?: return

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            hideVideoToolsTranscriptPanel()
            sidePanel.showVideoTranscriptSidePanel(config.subtitleUrl)
        } else {
            sidePanel.hideVideoTranscriptSidePanel(notifyHost = false)
            showVideoToolsTranscriptPanel(VideoTranscriptFragment.newInstance(config.subtitleUrl))
        }
    }

    override fun onVideoAIButtonClicked(isFullscreen: Boolean) {
        toggleVideoAIPanel()
    }

    override fun onVideoAISeek(seconds: Double) {
        if (::videoWidgetFragment.isInitialized) {
            videoWidgetFragment.seekTo((seconds * 1000).toLong())
        }
    }

    override fun onVideoAICloseRequested() {
        closeVideoAIPanel()
    }

    override fun onVideoAIPanelStateChanged(isOpen: Boolean) {
        openPanel = if (isOpen) OpenPanel.AI else null
        updateVideoToolsUIState()
    }

    override fun onVideoTranscriptButtonClicked(isFullscreen: Boolean) {
        toggleVideoTranscriptPanel()
    }

    override fun onVideoTranscriptPanelStateChanged(isOpen: Boolean) {
        openPanel = if (isOpen) OpenPanel.TRANSCRIPT else null
        updateVideoToolsUIState()
    }

    override fun onVideoTranscriptSeek(seconds: Double) {
        if (::videoWidgetFragment.isInitialized) {
            videoWidgetFragment.seekTo((seconds * 1000).toLong())
        }
    }

    override fun onVideoTranscriptCloseRequested() {
        closeVideoTranscriptPanel()
    }

    override fun getVideoTranscriptCurrentPositionSeconds(): Float {
        return nativeVideoWidgetFragment?.getCurrentPositionSeconds() ?: 0f
    }


    private fun showDownloadStatus() {
        offlineVideoViewModel.get(content.video!!.getPlaybackURL()!!).observe(viewLifecycleOwner, Observer {
            if (::menu.isInitialized) {
                if(it != null && !it.isDownloadCompleted) {
                    showProgress(it.percentageDownloaded)
                } else if(it != null && it.isDownloadCompleted){
                    showDownloadedIcon()
                } else {
                    showDownloadIcon()
                }
            }
        })
    }

    private fun showProgress(percentage: Int) {
        menu.findItem(R.id.download_progress).isVisible = true
        menu.findItem(R.id.download).isVisible = false
        videoDownloadProgress.progress = percentage
    }

    private fun showDownloadedIcon() {
        menu.findItem(R.id.download).isVisible = false
        menu.findItem(R.id.download_progress).isVisible = false
        menu.findItem(R.id.downloaded).isVisible = true
    }

    private fun showDownloadIcon() {
        menu.findItem(R.id.download).isVisible = true
        menu.findItem(R.id.download_progress).isVisible = false
        menu.findItem(R.id.downloaded).isVisible = false
    }

    private fun parseVideoDescription() {
        val raw = content.description?.trim().orEmpty()
        if (raw.isBlank()) {
            description.text = ""
            description.visibility = View.GONE
            titleLayout.isClickable = false
            titleView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            return
        }

        titleLayout.isClickable = true
        description.text = trimTrailingWhitespace(HtmlCompat.fromHtml(raw, HtmlCompat.FROM_HTML_MODE_LEGACY))
        val durationRegex = "([0-2]?[0-9]?:?[0-5]?[0-9]:[0-5][0-9])"
        val pattern: Pattern = Pattern.compile(durationRegex)
        PatternEditableBuilder().addPattern(
            pattern,
            Color.parseColor("#2D9BE8"),
            object : PatternEditableBuilder.SpannableClickedListener {
                override fun onSpanClicked(text: String) {
                    val seconds = convertDurationStringToSeconds(text)
                    videoWidgetFragment.seekTo(seconds * 1000L)
                }
            }).into(description)
        toggleDescription(true)
    }

    private fun trimTrailingWhitespace(text: CharSequence): CharSequence {
        val builder = SpannableStringBuilder(text)
        while (builder.isNotEmpty() && builder.last().isWhitespace()) {
            builder.delete(builder.length - 1, builder.length)
        }
        return builder
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == WebViewConstants.REQUEST_SELECT_FILE && resultCode == RESULT_OK){
            videoWidgetFragment.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun fetchVideoQuestions() {
        content.video?.id?.let { videoId ->
            contentRepository.loadVideoQuestions(videoContentId = videoId).observe(viewLifecycleOwner, Observer { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        resource.data?.let { questions ->
                            if (questions.isNotEmpty()) {
                                setupQuestionLogic(questions)
                            }
                        }
                    }
                    Status.ERROR -> {
                        val exception = resource.exception
                        val errorMessage = when {
                            exception is TestpressException -> {
                                when (exception.kind) {
                                    TestpressException.Kind.HTTP -> {
                                        val statusCode = exception.response?.code() ?: 0
                                        "API Error: $statusCode - ${exception.message}"
                                    }
                                    TestpressException.Kind.NETWORK -> {
                                        "Network Error: ${exception.message}"
                                    }
                                    else -> {
                                        "API Error: ${exception.message}"
                                    }
                                }
                            }
                            exception?.message?.contains("database", ignoreCase = true) == true ||
                            exception?.message?.contains("Database", ignoreCase = true) == true -> {
                                "Database Error: ${exception.message}"
                            }
                            else -> {
                                "Error: ${exception?.message ?: "Unknown error occurred"}"
                            }
                        }
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                    }
                    else -> {}
                }
            })
        }
    }

    private fun setupQuestionLogic(questions: List<NetworkVideoQuestion>) {
        val validQuestions = questions.filter { q ->
            when (q.question.type) {
                "G" -> !q.question.answers.isNullOrEmpty()
                "R", "C" -> true
                else -> false
            }
        }
        
        if(validQuestions.isEmpty()) return

        videoQuestionViewModel.setQuestions(validQuestions)
        val positions = videoQuestionViewModel.getUniquePositions()

        nativeVideoWidgetFragment?.let {
            it.registerPositionCallbacks(
                positions,
                questionCallbackHandler
            )
            it.addPlaybackMarkers(positions)
        }
    }

    private fun handleQuestionTrigger(position: Long) {
        val question = videoQuestionViewModel.getNextQuestionForPosition(position.toInt())

        if (question == null) {
            return
        }

        nativeVideoWidgetFragment?.pauseVideo()
        VideoQuestionSheetFragment.newInstance(question)
            .show(childFragmentManager, "VideoQuestionSheetFragment")
    }

    override fun onQuestionCompleted(questionId: Long) {
        val position = videoQuestionViewModel.markQuestionAsCompleted(questionId)
        val nextQuestion = position?.let { videoQuestionViewModel.getNextQuestionForPosition(it) }

        if (nextQuestion != null) {
            VideoQuestionSheetFragment.newInstance(nextQuestion)
                .show(childFragmentManager, "VideoQuestionSheetFragment")
        } else {
            nativeVideoWidgetFragment?.playVideo()
        }
    }
}

class VideoWidgetFragmentFactory {
    companion object {
        fun getWidget(video: DomainVideoContent): BaseVideoWidgetFragment {
            return when {
                video.isDomainRestricted!! -> DomainRestrictedVideoFragment()
                video.hasEmbedCode() -> WebViewVideoFragment()
                else -> NativeVideoWidgetFragment()
            }
        }
    }
}

enum class VideoDownloadError {
    COURSE_NOT_PURCHASED, DOWNLOAD_LIMIT_REACHED
}
