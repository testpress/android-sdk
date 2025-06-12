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
import `in`.testpress.course.viewmodels.OfflineVideoViewModel
import `in`.testpress.models.InstituteSettings
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
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
import java.util.regex.Pattern

open class VideoContentFragment : BaseContentDetailFragment() {
    protected lateinit var titleView: TextView
    protected lateinit var description: TextView
    protected lateinit var titleLayout: LinearLayout
    protected lateinit var videoWidgetFragment: BaseVideoWidgetFragment
    protected lateinit var offlineVideoViewModel: OfflineVideoViewModel
    protected lateinit var videoDownloadProgress: RingProgressBar
    protected lateinit var menu: Menu
    protected lateinit var instituteSettings: InstituteSettings;
    protected var remainingDownloadCount :Int? = null

    override var isBookmarkEnabled: Boolean
        get() = false
        set(value) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        VideoDownloadService.start(requireContext())
        offlineVideoViewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return OfflineVideoViewModel(OfflineVideoRepository(requireContext())) as T
            }
        }).get(OfflineVideoViewModel::class.java)
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
            val isDescriptionVisible = description.visibility == View.VISIBLE
            toggleDescription(!isDescriptionVisible)
        }
    }

    private fun toggleDescription(show: Boolean) {
        if (show) {
            description.visibility = View.VISIBLE
            titleView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_up_chevron, 0)
            content.description?.let { swipeRefresh.isEnabled = false }
        } else {
            description.visibility = View.INVISIBLE
            swipeRefresh.isEnabled = true
            titleView.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.drawable.ic_down_chevron,
                0
            )
        }
    }

    override fun display() {
        if (content.video?.isTranscodingStatusComplete() == false){
            displayTranscodingMessage()
            if (::menu.isInitialized) {
                menu.findItem(R.id.download)?.isVisible = false
            }
            return
        }

        if (content.video?.isViewsExhausted == true) {
            emptyViewFragment.showViewsExhaustedMessage()
            setHasOptionsMenu(false)
            return
        }
        titleView.text = content.title
        videoWidgetFragment = VideoWidgetFragmentFactory.getWidget(content.video!!)
        videoWidgetFragment.arguments = arguments
        parseVideoDescription()
        if (content.video!!.isDownloadable() && instituteSettings.isVideoDownloadEnabled) {
            showDownloadStatus()
        } else if (::menu.isInitialized) {
            menu.clear()
        }
        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.video_widget_fragment, videoWidgetFragment)
        transaction.commit()
    }

    private fun displayTranscodingMessage(){
        emptyViewFragment.setEmptyText(
            R.string.transcoding_message_title,
            R.string.transcoding_message_description,
            null)
    }

    override fun onResume() {
        super.onResume()
        if(isContentInitialized() && instituteSettings.isVideoDownloadEnabled && content.video!!.isDownloadable()) {
            showDownloadStatus()
        }
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
        content.description?.let {
            description.text = HtmlCompat.fromHtml(it, HtmlCompat.FROM_HTML_MODE_LEGACY)
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
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == WebViewConstants.REQUEST_SELECT_FILE && resultCode == RESULT_OK){
            videoWidgetFragment.onActivityResult(requestCode, resultCode, data)
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