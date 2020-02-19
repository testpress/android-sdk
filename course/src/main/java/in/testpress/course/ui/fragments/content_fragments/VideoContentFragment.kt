package `in`.testpress.course.ui.fragments.content_fragments

import `in`.testpress.core.TestpressSDKDatabase
import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.R
import `in`.testpress.course.enums.Status
import `in`.testpress.course.network.TestpressCourseApiClient.EMBED_CODE
import `in`.testpress.course.network.TestpressCourseApiClient.EMBED_DOMAIN_RESTRICTED_VIDEO_PATH
import `in`.testpress.course.util.ExoPlayerUtil
import `in`.testpress.course.util.ExoplayerFullscreenHelper
import `in`.testpress.models.greendao.Content
import `in`.testpress.models.greendao.Video
import `in`.testpress.models.greendao.VideoDao
import `in`.testpress.util.FullScreenChromeClient
import `in`.testpress.util.WebViewUtils
import android.app.Activity
import android.arch.lifecycle.Observer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.google.gson.JsonObject


class VideoContentFragment : BaseContentDetailFragment() {
    private lateinit var webView: WebView
    private lateinit var webViewUtils: WebViewUtils
    private lateinit var titleView: TextView
    private lateinit var titleLayout: LinearLayout
    private lateinit var exoPlayerMainFrame: FrameLayout

    private lateinit var videoDao: VideoDao
    private lateinit var fullScreenChromeClient: FullScreenChromeClient
    private val exoplayerFullscreenHelper: ExoplayerFullscreenHelper by lazy {
        ExoplayerFullscreenHelper(activity)
    }
    private var exoPlayerUtil: ExoPlayerUtil? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        videoDao = TestpressSDKDatabase.getVideoDao(activity)
        fullScreenChromeClient = FullScreenChromeClient(activity)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.video_content_detail, container, false);
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        webView = view.findViewById(R.id.web_view)
        titleView = view.findViewById(R.id.title)
        titleLayout = view.findViewById(R.id.title_layout)
        exoPlayerMainFrame = view.findViewById(R.id.exo_player_main_frame)
        webViewUtils = HtmlViewUtils(webView)
        webView.scrollBarStyle = WebView.SCROLLBARS_OUTSIDE_OVERLAY;
    }

    override fun onUpdateContent(fetchedContent: Content) {
        val video = fetchedContent.rawVideo
        video?.let {
            videoDao.insertOrReplace(video)
            fetchedContent.videoId = video.id
        }
        contentDao.insertOrReplace(fetchedContent)
    }

    override fun loadContent() {
        titleView.text = content.title
        titleLayout.visibility = View.VISIBLE
        val video = content.rawVideo

        if (video == null) {
            println("I am going to fetch video")
            swipeRefresh.isRefreshing = true
            updateContent()
            return
        }

        if (video.isDomainRestricted) {
            loadDomainRestrictedVideo(video);
        } else if (content.isEmbeddableVideo) {
            loadEmbeddableVideo(video);
        } else {
            loadNativeVideo(video);
        }
    }

    private fun loadDomainRestrictedVideo(video: Video) {
        val jsonObject = JsonObject()
        jsonObject.addProperty(EMBED_CODE, video.getEmbedCode())
        val url = courseApiClient.baseUrl + EMBED_DOMAIN_RESTRICTED_VIDEO_PATH
        webViewUtils.initWebViewAndPostUrl(url, jsonObject.toString(), activity)
        webView.webChromeClient = fullScreenChromeClient
    }

    private fun loadEmbeddableVideo(video: Video) {
        val html = "<div style='margin-top: 15px; padding-left: 20px; padding-right: 20px;'" +
                "class='videoWrapper'>" + video.getEmbedCode() + "</div>"

        webViewUtils.initWebView(html, activity)
        webView.webChromeClient = fullScreenChromeClient
    }

    private fun loadNativeVideo(video: Video) {
        val session = TestpressSdk.getTestpressSession(activity!!)
        if (session != null && session.instituteSettings.isDisplayUserEmailOnVideo) {
            swipeRefresh.isRefreshing = true
            viewModel.getUserProfileDetails().observe(this, Observer {
                swipeRefresh.isRefreshing = false
                createAttemptOrInitPlayer()
            })
        } else {
            createAttemptOrInitPlayer()
        }
    }

    fun createAttemptOrInitPlayer() {
        if (viewModel.contentAttempt.value?.data != null) {
            initExoPlayer(content.rawVideo.hlsUrl)
        } else {
            swipeRefresh.isRefreshing = true
            viewModel.createContentAttempt().observe(this, Observer { resource ->
                swipeRefresh.isRefreshing = false
                when (resource?.status) {
                    Status.SUCCESS -> initExoPlayer(content.rawVideo.hlsUrl)
                    Status.ERROR -> handleError(resource.exception!!)
                }
            })
        }
    }

    private fun initExoPlayer(videoUrl: String) {
        exoPlayerMainFrame.visibility = View.VISIBLE
        val videoAttempt = viewModel.contentAttempt.value!!.data!!.rawVideoAttempt

        if (exoPlayerUtil != null) {
            exoPlayerUtil?.releasePlayer()
        }

        exoPlayerUtil = ExoPlayerUtil(activity, exoPlayerMainFrame, videoUrl, videoAttempt.lastWatchedPosition)
        exoPlayerUtil?.setVideoAttemptParameters(videoAttempt.id, content)
        exoPlayerUtil?.initializePlayer()
        exoplayerFullscreenHelper.initializeOrientationListener()
        exoplayerFullscreenHelper.setExoplayerUtil(exoPlayerUtil)
    }

    override fun onPause() {
        super.onPause()
        if (exoPlayerUtil != null) {
            exoPlayerUtil?.onPause()
        }
    }

    override fun onResume() {
        super.onResume()
        if (exoPlayerUtil != null) {
            exoPlayerUtil?.onResume()
        }
    }

    override fun onStart() {
        super.onStart()
        if (exoPlayerUtil != null) {
            exoPlayerUtil?.onStart()
        }
    }

    override fun onStop() {
        super.onStop()
        if (exoPlayerUtil != null) {
            exoPlayerUtil?.onStop()
        }
    }

    inner class HtmlViewUtils(webView: WebView) : WebViewUtils(webView) {
        override fun onPageStarted() {
            super.onPageStarted()
            swipeRefresh.isRefreshing = true
            emptyContainer.visibility = View.GONE
        }

        override fun onLoadFinished() {
            super.onLoadFinished()
            swipeRefresh.isRefreshing = false
            webView.visibility = View.VISIBLE
            viewModel.createContentAttempt()
        }

        override fun getHeader(): String {
            return super.getHeader() + getBookmarkHandlerScript()
        }

        override fun onNetworkError() {
            super.onNetworkError()
            setEmptyText(R.string.testpress_network_error,
                    R.string.testpress_no_internet_try_again,
                    R.drawable.ic_error_outline_black_18dp)
            retryButton.setOnClickListener { updateContent() }
        }

        override fun shouldOverrideUrlLoading(activity: Activity?, url: String?): Boolean {
            if (url?.contains(EMBED_DOMAIN_RESTRICTED_VIDEO_PATH) == true) {
                return false
            }

            return super.shouldOverrideUrlLoading(activity, url)
        }
    }
}