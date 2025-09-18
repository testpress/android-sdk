package `in`.testpress.course.fragments

import `in`.testpress.WebViewConstants
import `in`.testpress.course.R
import `in`.testpress.course.api.TestpressCourseApiClient
import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.domain.DomainVideoContent
import `in`.testpress.enums.Status
import `in`.testpress.course.ui.ContentActivity
import `in`.testpress.util.FullScreenChromeClient
import `in`.testpress.util.WebViewUtils
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.ProgressBar
import androidx.lifecycle.Observer

open class WebViewVideoFragment : BaseVideoWidgetFragment() {
    protected lateinit var webView: WebView
    protected lateinit var webViewUtils: WebViewUtils
    protected lateinit var fullScreenChromeClient: FullScreenChromeClient
    protected var video: DomainVideoContent? = null
    private var contentId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fullScreenChromeClient = FullScreenChromeClient(activity)
        fullScreenChromeClient.disableLongPress = true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.video_webview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initWebView(view)
        contentId = requireArguments().getLong(ContentActivity.CONTENT_ID)
        viewModel.getContent(contentId).observe(viewLifecycleOwner, Observer {
            when (it?.status) {
                Status.SUCCESS -> {
                    video = it.data?.video
                    loadVideo(it.data!!)
                }
                else -> {}
            }
        })
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // Load the video in fullscreen
            enableFullscreen();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            // Restore the WebView to its original size
            disableFullscreen();
        }
    }

    private fun enableFullscreen() {
        webView.loadUrl("javascript:document.getElementsByTagName('iframe')[0].webkitRequestFullscreen();")
    }

    private fun disableFullscreen() {
        webView.loadUrl("javascript:document.webkitExitFullscreen();")
    }

    fun initWebView(view: View) {
        webView = view.findViewById(R.id.web_view)
        webViewUtils = HtmlViewUtils(webView)
        webView.scrollBarStyle = WebView.SCROLLBARS_OUTSIDE_OVERLAY
        webView.setOnLongClickListener { true }
        webView.isLongClickable = false
    }

    open fun loadVideo(content: DomainContent) {
        val html = "<div style='margin-top: 15px padding-left: 20px padding-right: 20px'" +
            "class='videoWrapper'>" + video?.embedCode + "</div>"

        webViewUtils.initWebView(html, activity)
        webView.webChromeClient = fullScreenChromeClient
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (requestCode == WebViewConstants.REQUEST_SELECT_FILE && resultCode == RESULT_OK) {
            fullScreenChromeClient.SelectFile(resultCode, intent)
        }
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
    }

    override fun onPause() {
        super.onPause()
        webView.onPause()
    }

    inner class HtmlViewUtils(webView: WebView) : WebViewUtils(webView) {
        override fun onLoadFinished() {
            super.onLoadFinished()
            webView.visibility = View.VISIBLE
            view?.findViewById<ProgressBar>(R.id.progress_bar)?.visibility = View.GONE
            viewModel.createContentAttempt(contentId)
        }

        override fun shouldOverrideUrlLoading(activity: Activity?, url: String?): Boolean {
            // Prevent users from navigating to other apps when the video is embedded from YouTube
            if (isYoutubeEmbedCode(video?.embedCode)) return true

            if (url?.contains(TestpressCourseApiClient.EMBED_DOMAIN_RESTRICTED_VIDEO_PATH) == true) {
                return false
            }

            return super.shouldOverrideUrlLoading(activity, url)
        }

        private fun isYoutubeEmbedCode(embedCode: String?): Boolean {
            return embedCode?.contains("www.youtube.com/embed")?:false
        }
    }
}