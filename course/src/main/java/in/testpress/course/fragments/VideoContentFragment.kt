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
import `in`.testpress.course.util.DateUtils.convertDurationStringToSeconds
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
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import java.util.regex.Pattern

class VideoContentFragment : BaseContentDetailFragment() {
    private lateinit var titleView: TextView
    private lateinit var description: TextView
    private lateinit var titleLayout: LinearLayout
    private lateinit var videoWidgetFragment: BaseVideoWidgetFragment
    private lateinit var offlineVideoViewModel: OfflineVideoViewModel
    private lateinit var videoDownloadProgress: RingProgressBar
    private lateinit var menu: Menu
    private lateinit var instituteSettings: InstituteSettings;
    private var remainingDownloadCount :Int? = null

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
        initializeObserver()
    }

    private fun initializeObserver(){
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
        progressView.setOnClickListener {
            requireContext().startActivity(DownloadsActivity.createIntent(requireContext()))
        }
        videoDownloadProgress = progressView.findViewById(R.id.video_download_progress)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.download) {
            showDownloadDialog()
            return true
        } else if (item.itemId == R.id.downloaded) {
            requireContext().startActivity(DownloadsActivity.createIntent(requireContext()))
            return true
        } else if (item.itemId == R.id.download_progress) {
            requireContext().startActivity(DownloadsActivity.createIntent(requireContext()))
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showDownloadUnavailableDialog(errorReason: String) {
        val builder =
            AlertDialog.Builder(requireContext(), R.style.TestpressAppCompatAlertDialogStyle)
        builder.setTitle("Download Unavailable")
        builder.setMessage(getDialogErrorMessage(errorReason))
        builder.setPositiveButton("Ok", null)
        builder.show()
    }

    private fun getDialogErrorMessage(errorReason: String):String{
        return if (errorReason == "CourseNotPurchased"){
            "This content is not available for download, please purchase it to watch it in offline."
        } else {
            "You are reached the maximum download limit of ${instituteSettings.maxAllowedDownloadedVideos}, please delete the video to continue the download."
        }
    }

    private fun showDownloadDialog() {
        if (content.isCourseNotPurchased) {
            showDownloadUnavailableDialog("CourseNotPurchased")
            return
        } else if (remainingDownloadCount != null && remainingDownloadCount!! < 1){
            showDownloadUnavailableDialog("Download Limit Reached")
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