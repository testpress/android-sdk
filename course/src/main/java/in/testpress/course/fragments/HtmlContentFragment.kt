package `in`.testpress.course.fragments

import `in`.testpress.core.TestpressException
import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.R
import `in`.testpress.util.EventsTrackerFacade
import `in`.testpress.util.ViewUtils
import `in`.testpress.util.WebViewUtils
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.Observer
import java.io.IOException
import java.util.*
import kotlin.concurrent.schedule

class HtmlContentFragment : BaseContentDetailFragment() {
    private lateinit var webView: WebView
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    lateinit var webViewUtils: WebViewUtils
    private lateinit var titleView: TextView
    private lateinit var titleLayout: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.html_content_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        webView = view.findViewById(R.id.web_view)
        titleView = view.findViewById(R.id.title)
        titleLayout = view.findViewById(R.id.title_layout)
        webViewUtils = HtmlViewUtils(webView)
        webView.scrollBarStyle = WebView.SCROLLBARS_OUTSIDE_OVERLAY
        ViewUtils.setTypeface(arrayOf(titleView), TestpressSdk.getRubikMediumFont(activity!!))
    }

    override fun display() {
        titleView.text = content.title
        titleLayout.visibility = View.VISIBLE

        val htmlContent = content.htmlContent
        val html = """
            <div style='padding-left: 20px; padding-right: 20px;'>
                ${htmlContent?.textHtml}
            </div>
        """.trimIndent()
        webViewUtils.initWebView(html, activity)
    }

    inner class HtmlViewUtils(webView: WebView) : WebViewUtils(webView) {
        override fun onPageStarted() {
            super.onPageStarted()
            swipeRefresh.isRefreshing = true
        }

        override fun onLoadFinished() {
            super.onLoadFinished()
            swipeRefresh.isRefreshing = false
            webView.visibility = View.VISIBLE
            viewModel.createContentAttempt(contentId).observe(viewLifecycleOwner, Observer {
                checkAndUnlockNextContent()
            })

            Timer().schedule(5000) {
                activity?.applicationContext?.let {
                    val params = hashMapOf<String, Any>("name" to content.title!!, "id" to contentId)
                    EventsTrackerFacade(it).logEvent(EventsTrackerFacade.VIEWED_NOTES_EVENT, params)
                }
            }
        }

        override fun onNetworkError() {
            super.onNetworkError()
            emptyViewFragment.displayError(TestpressException.networkError(IOException()))
        }
    }
}