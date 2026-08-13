package `in`.testpress.course.fragments

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import androidx.fragment.app.Fragment
import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.R
import `in`.testpress.fragments.WebViewFragment
import `in`.testpress.models.SSOUrl
import `in`.testpress.network.TestpressApiClient
import `in`.testpress.util.BaseJavaScriptInterface

class FermionLiveStreamFragment : Fragment() {

    /** Implement in the parent to handle navigation when the user leaves the Fermion meeting. */
    interface Listener {
        fun onMeetingLeft()
    }

    var listener: Listener? = null
    private var streamUrl: String? = null
    private var fermionHost: String? = null
    private var fermionPath: String? = null
    private var fermionPageLoaded = false

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
        fetchSsoUrlAndLoad()
    }

    private fun fetchSsoUrlAndLoad() {
        val session = TestpressSdk.getTestpressSession(requireContext())
        if (session != null) {
            TestpressApiClient(requireContext(), session).ssourl.enqueue(object : TestpressCallback<SSOUrl>() {
                override fun onSuccess(result: SSOUrl?) {
                    val ssoUrl = result?.ssoUrl
                    if (!ssoUrl.isNullOrBlank()) {
                        val loginUrl = buildSsoLoginUrl(ssoUrl, session.instituteSettings.baseUrl)
                        loadInWebViewFragment(loginUrl)
                    } else {
                        loadInWebViewFragment(streamUrl!!)
                    }
                }

                override fun onException(exception: TestpressException?) {
                    loadInWebViewFragment(streamUrl!!)
                }
            })
        } else {
            loadInWebViewFragment(streamUrl!!)
        }
    }

    private fun buildSsoLoginUrl(ssoUrl: String, baseUrl: String): String {
        val nextUrl = Uri.encode(streamUrl)
        val separator = if (ssoUrl.contains("?")) "&" else "?"
        val cleanBaseUrl = baseUrl.trimEnd('/')
        val cleanSsoUrl = if (ssoUrl.startsWith("/")) ssoUrl else "/$ssoUrl"
        return "$cleanBaseUrl$cleanSsoUrl${separator}next=$nextUrl"
    }

    @SuppressLint("AddJavascriptInterface")
    private fun loadInWebViewFragment(urlToLoad: String) {
        if (!isAdded) return
        fermionHost = streamUrl?.let { Uri.parse(it).host }
        fermionPath = streamUrl?.let { Uri.parse(it).path?.trimEnd('/') }
        fermionPageLoaded = false

        val webViewFragment = createWebViewFragment(urlToLoad)
        childFragmentManager.beginTransaction()
            .replace(R.id.fermion_webview_container, webViewFragment)
            .commitAllowingStateLoss()
    }

    private fun createWebViewFragment(urlToLoad: String): WebViewFragment {
        return WebViewFragment().apply {
            arguments = Bundle().apply {
                putString(WebViewFragment.URL_TO_OPEN, urlToLoad)
                putBoolean(WebViewFragment.IS_AUTHENTICATION_REQUIRED, true)
                putBoolean(WebViewFragment.ALLOW_NON_INSTITUTE_URL_IN_WEB_VIEW, true)
                putInt(WebViewFragment.CACHE_MODE, WebSettings.LOAD_NO_CACHE)
            }
            listener = createWebViewListener(this)
        }
    }

    private fun createWebViewListener(webViewFragment: WebViewFragment): WebViewFragment.Listener {
        return object : WebViewFragment.Listener {
            override fun onWebViewInitializationSuccess() {
                webViewFragment.addJavascriptInterface(
                    FermionBridge { notifyMeetingLeft() },
                    "FermionAndroid"
                )
            }

            override fun onPageStarted(url: String?) {
                if (fermionPageLoaded && !isStillOnFermionPage(url)) {
                    webViewFragment.webView.stopLoading()
                    notifyMeetingLeft()
                }
            }

            override fun onPageFinished(url: String?) {
                val urlPath = url?.let { Uri.parse(it).path?.trimEnd('/') }
                when {
                    urlPath == fermionPath -> {
                        fermionPageLoaded = true
                        webViewFragment.webView.evaluateJavascript(
                            buildPostMessageListenerScript(fermionHost), null
                        )
                    }
                    fermionPageLoaded && !isStillOnFermionPage(url) -> {
                        notifyMeetingLeft()
                    }
                }
            }

            override fun shouldOverrideUrlLoading(url: String?): Boolean {
                if (fermionPageLoaded && !isStillOnFermionPage(url)) {
                    notifyMeetingLeft()
                    return true
                }
                return false
            }
        }
    }

    private fun isStillOnFermionPage(url: String?): Boolean {
        if (url == null) return true
        return Uri.parse(url).path?.trimEnd('/') == fermionPath
    }

    private fun buildPostMessageListenerScript(fermionHost: String?): String {
        val originCheck = if (fermionHost != null) {
            "if (!event.origin.includes('$fermionHost')) return;"
        } else {
            ""
        }
        return """
            (function() {
                if (window._fermionAndroidListenerAttached) return;
                window._fermionAndroidListenerAttached = true;
                window.addEventListener('message', function(event) {
                    $originCheck
                    var payload = event.data;
                    if (!payload || typeof payload !== 'object') return;
                    var type = payload.type;
                    if (type === 'webrtc:left-stage' || type === 'webrtc:livestream-ended') {
                        if (window.FermionAndroid) {
                            window.FermionAndroid.onLeave();
                        }
                    }
                });
            })();
        """.trimIndent()
    }

    /** Notifies the parent that the user has left the meeting. */
    private fun notifyMeetingLeft() {
        activity?.runOnUiThread {
            listener?.onMeetingLeft() ?: activity?.finish()
        }
    }

    /** JavaScript interface that receives leave events from the Fermion embed. */
    inner class FermionBridge(private val onLeave: () -> Unit) : BaseJavaScriptInterface(requireActivity()) {
        @JavascriptInterface
        fun onLeave() {
            onLeave()
        }
    }

    companion object {
        const val ARG_STREAM_URL = "ARG_STREAM_URL"
        const val ARG_TITLE = "ARG_TITLE"
    }
}
