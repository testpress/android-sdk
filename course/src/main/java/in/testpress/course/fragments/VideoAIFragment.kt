package `in`.testpress.course.fragments

import `in`.testpress.core.TestpressException
import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.R
import `in`.testpress.course.util.VideoAIUserIdProvider
import `in`.testpress.course.util.VideoAIJsInterface
import `in`.testpress.util.webview.BaseWebChromeClient
import `in`.testpress.util.webview.BaseWebViewClient
import `in`.testpress.util.webview.WebView
import `in`.testpress.util.webview.WebViewEventListener
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.io.File

class VideoAIFragment : Fragment(), WebViewEventListener, VideoAIJsInterface.Host {

    companion object {
        const val ARG_ASSET_ID = "assetId"
        const val ARG_NOTES_URL = "notesUrl"
        private const val TEMPLATE_NAME = "video_ai_template.html"

        fun newInstance(assetId: String, notesUrl: String?): VideoAIFragment {
            return VideoAIFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_ASSET_ID, assetId)
                    putString(ARG_NOTES_URL, notesUrl ?: "")
                }
            }
        }
    }

    interface Host {
        fun onVideoAISeek(seconds: Double)
        fun onVideoAICloseRequested()
    }

    private var webView: WebView? = null
    private var progressBar: ProgressBar? = null
    private var container: FrameLayout? = null
    private lateinit var webChromeClient: BaseWebChromeClient
    private var assetId: String = ""
    private var notesUrl: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.learnlens_video_ai_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        parseArguments()
        
        if (assetId.isBlank()) {
            findHost()?.onVideoAICloseRequested()
            return
        }

        setupAndLoadWebView()
    }

    private fun initViews(view: View) {
        progressBar = view.findViewById(R.id.pb_loading)
        container = view.findViewById(R.id.webview_container)
        webChromeClient = BaseWebChromeClient(this)
    }

    private fun parseArguments() {
        arguments?.let {
            assetId = it.getString(ARG_ASSET_ID).orEmpty()
            notesUrl = it.getString(ARG_NOTES_URL).orEmpty()
        }
    }

    private fun setupAndLoadWebView() {
        val wv = WebView(requireContext())
        webView = wv
        container?.addView(wv)
        
        configureWebView()
        loadTemplate()
    }

    @SuppressLint("JavascriptInterface", "AddJavascriptInterface")
    private fun configureWebView() {
        webView?.let { wv ->
            wv.enableFileAccess()
            wv.webViewClient = BaseWebViewClient(this)
            wv.webChromeClient = webChromeClient
            wv.addJavascriptInterface(VideoAIJsInterface(requireActivity(), this), "AndroidVideoAI")
        }
    }

    private fun loadTemplate() {
        toggleLoadingState(true)
        val authToken = TestpressSdk.getTestpressSession(requireContext())?.token ?: ""
        val cacheDir = File(requireContext().filesDir, "web_assets")

        viewLifecycleOwner.lifecycleScope.launch {
            val userId = VideoAIUserIdProvider.getUserId(requireContext())
            val replacements = mapOf(
                "ASSET_ID" to assetId,
                "AUTH_TOKEN" to authToken,
                "USER_ID" to userId,
                "NOTES_URL" to notesUrl
            )
            webView?.loadTemplateAndCacheResources(TEMPLATE_NAME, replacements, "file://${cacheDir.absolutePath}/")
        }
    }

    private fun toggleLoadingState(isLoading: Boolean) {
        progressBar?.isVisible = isLoading
        container?.isVisible = !isLoading
    }

    private fun findHost(): Host? {
        return (parentFragment as? Host) ?: (activity as? Host)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        webChromeClient.cleanup()
        cleanupWebView()
    }

    private fun cleanupWebView() {
        webView?.let { wv ->
            wv.stopLoading()
            container?.removeView(wv)
            wv.destroy()
        }
        webView = null
        progressBar = null
        container = null
    }

    override fun onLoadingStarted() = toggleLoadingState(true)
    override fun onLoadingFinished() = toggleLoadingState(false)
    override fun onError(exception: TestpressException) = toggleLoadingState(false)
    override fun isViewActive(): Boolean = isAdded

    override fun onSeek(seconds: Double) {
        findHost()?.onVideoAISeek(seconds)
    }

    override fun onRequestClose() {
        findHost()?.onVideoAICloseRequested()
    }
}
