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
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.netopen.hotbitmapgg.library.view.RingProgressBar
import java.util.regex.Pattern

// vvv Make your fragment implement the new listener vvv
open class VideoContentFragment : BaseContentDetailFragment(), VideoQuizSheetFragment.OnQuizCompleteListener {
    
    protected lateinit var titleView: TextView
    protected lateinit var description: TextView
    protected lateinit var titleLayout: LinearLayout
    protected lateinit var videoWidgetFragment: BaseVideoWidgetFragment
    protected lateinit var offlineVideoViewModel: OfflineVideoViewModel
    protected lateinit var videoDownloadProgress: RingProgressBar
    protected lateinit var menu: Menu
    protected lateinit var instituteSettings: InstituteSettings;
    protected var remainingDownloadCount :Int? = null

    private var mockQuestionIndex = 0
    private val mockQuestions = createMockQuestions()
    private var quizTriggerHandler: android.os.Handler? = null
    private var quizTriggerRunnable: Runnable? = null

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

        view.findViewById<Button>(R.id.test_quiz_button)?.setOnClickListener {
            mockQuestionIndex = 0 // Reset the test
            showMockQuestion(mockQuestionIndex)
        }
        
        // Auto-trigger quiz after 5 seconds of video playback (for testing)
        startQuizAutoTrigger()
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

    override fun onResume() {
        super.onResume()
        if(isContentInitialized() && instituteSettings.isVideoDownloadEnabled && content.video!!.isDownloadable()) {
            showDownloadStatus()
        }
        // Re-trigger quiz auto-trigger if needed
        startQuizAutoTrigger()
    }

    override fun onPause() {
        super.onPause()
        stopQuizAutoTrigger()
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
    private fun showMockQuestion(index: Int) {
        val question = mockQuestions.getOrNull(index)
        if (question != null) {
            VideoQuizSheetFragment.newInstance(question)
                .show(requireActivity().supportFragmentManager, "VideoQuizSheetFragment")
        }
    }
 
    private fun startQuizAutoTrigger() {
        stopQuizAutoTrigger() // Cancel any existing trigger
        
        quizTriggerHandler = android.os.Handler(android.os.Looper.getMainLooper())
        quizTriggerRunnable = Runnable {
            mockQuestionIndex = 0 // Reset to first question
            showMockQuestion(mockQuestionIndex)
        }
        // Trigger after 5 seconds (5000ms)
        quizTriggerHandler?.postDelayed(quizTriggerRunnable!!, 2000)
    }

    private fun stopQuizAutoTrigger() {
        quizTriggerRunnable?.let { quizTriggerHandler?.removeCallbacks(it) }
        quizTriggerRunnable = null
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        stopQuizAutoTrigger()
    }
    override fun onQuizCompleted(questionId: Long) {
        mockQuestionIndex++
        showMockQuestion(mockQuestionIndex)
    }

    private fun createMockQuestions(): List<DummyQuestion> {
        // RadioButton (R) question with long question and long answers
        val rAnswers = listOf(
            DummyAnswer(1, "A. This is a very long answer option that tests how the UI handles multiple lines of text when the answer text is quite extensive and needs to wrap properly within the layout constraints", true),
            DummyAnswer(2, "B. This is another long wrong answer option that demonstrates how incorrect answers are displayed when they contain substantial amounts of text that needs to be rendered across multiple lines", false),
            DummyAnswer(3, "C. Short option", false),
            DummyAnswer(4, "D. Yet another incorrect answer option with a moderate amount of text to test the layout rendering capabilities", false)
        )
        val rQuestion = DummyQuestion(
            101, 
            "This is a comprehensive test question with a very long question text that spans multiple lines to verify how the quiz bottom sheet handles extensive content. The question asks: What are the key considerations when designing a user interface that must accommodate varying amounts of text content while maintaining readability and visual appeal?", 
            "R", 
            rAnswers
        )

        // CheckBox (C) question with long question and long answers
        val cAnswers = listOf(
            DummyAnswer(5, "A. First correct answer option with extensive text content that demonstrates how multiple selection options appear when they contain long descriptions that need to wrap properly", true),
            DummyAnswer(6, "B. Second correct answer option with detailed explanation text that shows how the checkbox layout handles substantial amounts of text content spanning multiple lines", true),
            DummyAnswer(7, "C. This is an incorrect multiple choice option with considerable text length to test the visual rendering and spacing of checkbox items when they contain lengthy descriptions", false),
            DummyAnswer(8, "D. Another wrong answer option with moderate text length to ensure proper alignment and spacing in the checkbox list layout", false),
            DummyAnswer(9, "E. Final incorrect option with brief text", false)
        )
        val cQuestion = DummyQuestion(
            102, 
            "Select all the correct statements from the following options. This question contains a substantial amount of text to test how the quiz interface handles lengthy question content: When designing responsive user interfaces, developers must consider various factors including screen sizes, content variability, user interaction patterns, accessibility requirements, and performance optimization strategies. Which of the following options correctly describe these considerations?", 
            "C", 
            cAnswers
        )

        // Gap-fill (G) question with 3 boxes
        val gAnswers = listOf(
            DummyAnswer(10, "quick", true),
            DummyAnswer(11, "brown", true),
            DummyAnswer(12, "fox", true)
        )
        val gQuestion = DummyQuestion(
            103, 
            "The [quick] [brown] [fox] jumps over the lazy dog.", 
            "G", 
            gAnswers
        )

        // Another RadioButton question with medium length
        val r2Answers = listOf(
            DummyAnswer(13, "A. Answer option with moderate text length to test spacing and alignment", true),
            DummyAnswer(14, "B. Wrong answer", false),
            DummyAnswer(15, "C. Another wrong option", false),
            DummyAnswer(16, "D. Yet another incorrect choice", false)
        )
        val r2Question = DummyQuestion(
            104,
            "This is a medium-length question that tests how the quiz handles questions with moderate amounts of text content.",
            "R",
            r2Answers
        )

        // Another Gap-fill question with 2 boxes
        val g2Answers = listOf(
            DummyAnswer(17, "cat", true),
            DummyAnswer(18, "mat", true)
        )
        val g2Question = DummyQuestion(
            105,
            "The [cat] sat on the [mat].",
            "G",
            g2Answers
        )

        return listOf(rQuestion, cQuestion, gQuestion, r2Question, g2Question)
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