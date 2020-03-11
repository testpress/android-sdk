package `in`.testpress.course.fragments

import `in`.testpress.course.R
import `in`.testpress.course.TestpressCourse
import `in`.testpress.course.api.TestpressCourseApiClient
import `in`.testpress.course.di.InjectorUtils
import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.enums.Status
import `in`.testpress.course.ui.ContentActivity
import `in`.testpress.course.viewmodels.ContentViewModel
import `in`.testpress.util.FullScreenChromeClient
import `in`.testpress.util.WebViewUtils
import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

open class WebViewVideoFragment : Fragment() {
    protected lateinit var webView: WebView
    protected lateinit var webViewUtils: WebViewUtils
    private lateinit var viewModel: ContentViewModel
    protected lateinit var fullScreenChromeClient: FullScreenChromeClient
    private var contentId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fullScreenChromeClient = FullScreenChromeClient(activity)
        val contentType = requireArguments().getString(TestpressCourse.CONTENT_TYPE)
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ContentViewModel(
                    InjectorUtils.getContentRepository(contentType!!, context!!)
                ) as T
            }
        }).get(ContentViewModel::class.java)
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
                    loadVideo(it.data!!)
                }
            }
        })
    }

    fun initWebView(view: View) {
        webView = view.findViewById(R.id.web_view)
        webViewUtils = HtmlViewUtils(webView)
        webView.scrollBarStyle = WebView.SCROLLBARS_OUTSIDE_OVERLAY
        webView.webChromeClient = fullScreenChromeClient
    }

    open fun loadVideo(content: DomainContent) {
        val video = content.video
        val html = "<div style='margin-top: 15px padding-left: 20px padding-right: 20px'" +
            "class='videoWrapper'>" + video?.embedCode + "</div>"

        webViewUtils.initWebView(html, activity)
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
            viewModel.createContentAttempt(contentId)
        }

        override fun shouldOverrideUrlLoading(activity: Activity?, url: String?): Boolean {
            if (url?.contains(TestpressCourseApiClient.EMBED_DOMAIN_RESTRICTED_VIDEO_PATH) == true) {
                return false
            }

            return super.shouldOverrideUrlLoading(activity, url)
        }
    }
}