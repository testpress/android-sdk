package `in`.testpress.course.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import `in`.testpress.course.R

class FermionLiveStreamFragment : Fragment() {

    private var initialLoadComplete = false
    private var streamUrl: String? = null
    private var webView: WebView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_fermion_live_stream, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        streamUrl = arguments?.getString(ARG_STREAM_URL)
        if (streamUrl == null) {
            view.findViewById<View>(R.id.error_message).visibility = View.VISIBLE
            return
        }
        setupWebView()
    }

    private fun setupWebView() {
        val container = requireView() as ViewGroup
        webView = WebView(requireContext()).apply {
            configureSettings()
            webChromeClient = buildChromeClient()
            webViewClient = buildWebViewClient()
            streamUrl?.let { loadUrl(it) }
        }
        container.addView(webView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    private fun WebView.configureSettings() {
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true
        settings.allowFileAccess = false
        settings.mediaPlaybackRequiresUserGesture = false
    }

    private fun buildChromeClient() = object : WebChromeClient() {
        override fun onPermissionRequest(request: PermissionRequest) {
            val expectedHost = streamUrl?.let { android.net.Uri.parse(it).host }
            val requestHost = request.origin.host

            if (expectedHost == null || requestHost != expectedHost) {
                request.deny()
                return
            }

            val allowedResources = arrayOf(
                PermissionRequest.RESOURCE_VIDEO_CAPTURE,
                PermissionRequest.RESOURCE_AUDIO_CAPTURE
            )
            val filteredResources = request.resources.filter { it in allowedResources }.toTypedArray()
            if (filteredResources.isNotEmpty()) {
                request.grant(filteredResources)
            } else {
                request.deny()
            }
        }
    }

    private fun buildWebViewClient() = object : WebViewClient() {
        override fun onPageFinished(view: WebView, url: String) {
            initialLoadComplete = true
            view.evaluateJavascript(VIEWPORT_FIT_SCRIPT, null)
        }

        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            return handleNavigation(request.url.toString())
        }

        @Suppress("DEPRECATION")
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            return handleNavigation(url)
        }
    }

    private fun handleNavigation(url: String): Boolean {
        val currentUri = streamUrl?.let { android.net.Uri.parse(it) }
        val newUri = android.net.Uri.parse(url)

        if (initialLoadComplete && currentUri != null && isAdded) {
            val isSamePage = currentUri.host == newUri.host && currentUri.path == newUri.path

            if (!isSamePage) {
                requireActivity().finish()
                return true
            }
        }
        return false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        webView?.destroy()
        webView = null
    }

    companion object {
        const val ARG_STREAM_URL = "ARG_STREAM_URL"
        const val ARG_TITLE = "ARG_TITLE"

        private val VIEWPORT_FIT_SCRIPT = """
            (function() {
                var meta = document.querySelector('meta[name="viewport"]');
                if (!meta) {
                    meta = document.createElement('meta');
                    meta.name = 'viewport';
                    document.head.appendChild(meta);
                }
                meta.content = 'width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no';
                document.documentElement.style.cssText += 'width:100%!important;height:100%!important;overflow:hidden!important;';
                document.body.style.cssText += 'width:100%!important;height:100%!important;overflow:hidden!important;margin:0!important;padding:0!important;';
            })();
        """.trimIndent()
    }
}
