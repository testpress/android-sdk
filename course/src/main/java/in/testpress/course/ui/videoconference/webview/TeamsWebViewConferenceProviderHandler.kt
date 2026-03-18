package `in`.testpress.course.ui.videoconference.webview

import android.annotation.SuppressLint
import android.os.Build
import android.webkit.JavascriptInterface
import android.webkit.WebView

class TeamsWebViewConferenceProviderHandler(
    private val showLoader: () -> Unit,
    private val hideLoader: () -> Unit
) : WebViewConferenceProviderHandler {

    private var webView: WebView? = null
    private var hasCompletedAutoJoin = false

    override fun attach(webView: WebView) {
        this.webView = webView
        webView.addJavascriptInterface(TeamsAutoJoinBridge(), "TeamsAutoJoin")
    }

    override fun onPageStarted(url: String?) {
        showLoader()
    }

    override fun onPageFinished(url: String?) {
        if (!hasCompletedAutoJoin && isTeamsJoinGateUrl(url)) {
            showLoader()
            autoJoinTeamsMeetingWeb()
        } else {
            hideLoader()
        }
    }

    @SuppressLint("JavascriptInterface")
    private fun autoJoinTeamsMeetingWeb() {
        // Best-effort: depends on Teams DOM selectors/text and may break if Teams UI changes.
        val javascript = """
            (function() {
                var maxRetries = 20;
                var retries = 0;

                var selectors = [
                    '[data-tid="joinOnWeb"]',
                    '[data-tid="joinBrowserButton"]',
                    'button[data-tid*="web"]'
                ];

                var checkExist = setInterval(function() {
                    retries++;

                    for (var j = 0; j < selectors.length; j++) {
                        var btn = document.querySelector(selectors[j]);
                        if (btn && btn.offsetParent !== null) {
                            btn.click();
                            clearInterval(checkExist);
                            window.TeamsAutoJoin.onButtonClicked();
                            return;
                        }
                    }

                    var allButtons = document.querySelectorAll('button');
                    for (var i = 0; i < allButtons.length; i++) {
                        var text = (allButtons[i].textContent || allButtons[i].innerText || "").toLowerCase();
                        if (text.indexOf('continue on this browser') > -1 || text.indexOf('join on the web') > -1) {
                            allButtons[i].click();
                            clearInterval(checkExist);
                            window.TeamsAutoJoin.onButtonClicked();
                            return;
                        }
                    }

                    if (retries >= maxRetries) {
                        clearInterval(checkExist);
                        window.TeamsAutoJoin.onAutoJoinTimeout();
                    }
                }, 500);
            })();
        """.trimIndent()

        webView?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                it.evaluateJavascript(javascript, null)
            } else {
                it.loadUrl("javascript:$javascript")
            }
        }
    }

    private fun isTeamsJoinGateUrl(url: String?): Boolean {
        val normalizedUrl = url?.lowercase() ?: return false
        return normalizedUrl.contains("teams.microsoft.com") &&
            (normalizedUrl.contains("launcher") ||
                normalizedUrl.contains("join") ||
                normalizedUrl.contains("pre-join"))
    }

    inner class TeamsAutoJoinBridge {
        @JavascriptInterface
        fun onButtonClicked() {
            hasCompletedAutoJoin = true
        }

        @JavascriptInterface
        fun onAutoJoinTimeout() {
            hideLoader()
        }
    }
}
