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
import android.content.res.Configuration
import `in`.testpress.util.webview.CustomWebChromeClient
import androidx.appcompat.app.AppCompatActivity

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
    private var hasNotifiedLeave = false
    private var allowedHosts = setOf<String>()
    private var webViewFragment: WebViewFragment? = null

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
        updateLayoutForOrientation()
    }

    private fun fetchSsoUrlAndLoad() {
        val currentStreamUrl = streamUrl ?: return
        val session = TestpressSdk.getTestpressSession(requireContext())
        if (session != null) {
            TestpressApiClient(requireContext(), session).ssourl.enqueue(object : TestpressCallback<SSOUrl>() {
                override fun onSuccess(result: SSOUrl?) {
                    val ssoUrl = result?.ssoUrl
                    if (!ssoUrl.isNullOrBlank()) {
                        val loginUrl = buildSsoLoginUrl(ssoUrl, session.instituteSettings.baseUrl, currentStreamUrl)
                        loadInWebViewFragment(loginUrl)
                    } else {
                        loadInWebViewFragment(currentStreamUrl)
                    }
                }

                override fun onException(exception: TestpressException?) {
                    loadInWebViewFragment(currentStreamUrl)
                }
            })
        } else {
            loadInWebViewFragment(currentStreamUrl)
        }
    }

    private fun buildSsoLoginUrl(ssoUrl: String, baseUrl: String, targetUrl: String): String {
        val nextUrl = Uri.encode(targetUrl)
        val separator = if (ssoUrl.contains("?")) "&" else "?"
        val cleanBaseUrl = baseUrl.trimEnd('/')
        val cleanSsoUrl = if (ssoUrl.startsWith("/")) ssoUrl else "/$ssoUrl"
        return "$cleanBaseUrl$cleanSsoUrl${separator}next=$nextUrl"
    }

    @SuppressLint("AddJavascriptInterface")
    private fun loadInWebViewFragment(urlToLoad: String) {
        if (!isAdded) return

        val hosts = mutableSetOf<String>()
        streamUrl?.let { Uri.parse(it).host }?.let { hosts.add(it.lowercase()) }

        val session = TestpressSdk.getTestpressSession(requireContext())
        if (session != null) {
            runCatching { Uri.parse(session.instituteSettings.baseUrl)?.host }
                .getOrNull()?.let { hosts.add(it.lowercase()) }

            val whiteLabeledHostUrl = session.instituteSettings.whiteLabeledHostUrl
            if (!whiteLabeledHostUrl.isNullOrBlank()) {
                val whiteLabeledHost = runCatching {
                    if (whiteLabeledHostUrl.startsWith("http://") || whiteLabeledHostUrl.startsWith("https://")) {
                        Uri.parse(whiteLabeledHostUrl)?.host
                    } else {
                        Uri.parse("https://$whiteLabeledHostUrl")?.host
                    }
                }.getOrNull()
                whiteLabeledHost?.let { hosts.add(it.lowercase()) }
            }
        }
        allowedHosts = hosts

        fermionHost = streamUrl?.let { Uri.parse(it).host }
        fermionPath = streamUrl?.let { Uri.parse(it).path?.trimEnd('/') }
        fermionPageLoaded = false
        hasNotifiedLeave = false

        val fragment = createWebViewFragment(urlToLoad)
        webViewFragment = fragment
        childFragmentManager.beginTransaction()
            .replace(R.id.fermion_webview_container, fragment)
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
                    notifyMeetingLeft()
                }
            }

            override fun onPageFinished(url: String?) {
                val uri = url?.let { Uri.parse(it) }
                val urlHost = uri?.host?.lowercase()
                val urlPath = uri?.path?.trimEnd('/')

                val isMatchingHost = urlHost?.let { allowedHosts.contains(it) } ?: false

                when {
                    isMatchingHost && urlPath == fermionPath -> {
                        fermionPageLoaded = true
                        webViewFragment.webView.evaluateJavascript(
                            buildPostMessageListenerScript(), null
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
        val uri = Uri.parse(url)
        val path = uri.path ?: return false
        val host = uri.host?.lowercase() ?: return false
        return allowedHosts.contains(host) &&
               (path.trimEnd('/') == fermionPath || path.startsWith("$fermionPath/"))
    }

    private fun buildPostMessageListenerScript(): String {
        val hostsArrayJson = allowedHosts.joinToString(separator = ",", prefix = "[", postfix = "]") { "'$it'" }
        return """
            (function() {
                if (window._fermionAndroidListenerAttached) return;
                window._fermionAndroidListenerAttached = true;
                var allowedHosts = $hostsArrayJson;
                window.addEventListener('message', function(event) {
                    try {
                        var originHost = new URL(event.origin).hostname;
                        if (!allowedHosts.includes(originHost)) return;
                    } catch(e) { return; }
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
        if (hasNotifiedLeave) return
        hasNotifiedLeave = true
        activity?.runOnUiThread {
            if (listener != null) {
                listener?.onMeetingLeft()
            } else {
                activity?.finish()
            }
        }
    }

    /** JavaScript interface that receives leave events from the Fermion embed. */
    inner class FermionBridge(private val onLeave: () -> Unit) : BaseJavaScriptInterface(requireActivity()) {
        @JavascriptInterface
        fun onLeave() {
            onLeave()
        }
    }

    private fun hideSystemUI() {
        activity?.window?.decorView?.systemUiVisibility =
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_FULLSCREEN
    }

    private fun showSystemUI() {
        activity?.window?.decorView?.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    }

    private fun updateLayoutForOrientation() {
        val appCompatActivity = activity as? AppCompatActivity ?: return
        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        if (isLandscape) {
            appCompatActivity.supportActionBar?.hide()
            hideSystemUI()
            appCompatActivity.findViewById<View>(R.id.chat_view_fragment)?.visibility = View.GONE
        } else {
            appCompatActivity.supportActionBar?.show()
            showSystemUI()
            appCompatActivity.findViewById<View>(R.id.chat_view_fragment)?.visibility = View.VISIBLE
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateLayoutForOrientation()
    }

    override fun onDestroyView() {
        val appCompatActivity = activity as? AppCompatActivity
        appCompatActivity?.supportActionBar?.show()
        showSystemUI()
        super.onDestroyView()
    }

    companion object {
        const val ARG_STREAM_URL = "ARG_STREAM_URL"
        const val ARG_TITLE = "ARG_TITLE"
    }
}
