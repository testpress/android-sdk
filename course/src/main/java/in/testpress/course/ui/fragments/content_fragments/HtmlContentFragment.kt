package `in`.testpress.course.ui.fragments.content_fragments

import `in`.testpress.core.TestpressSDKDatabase
import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.R
import `in`.testpress.course.network.TestpressCourseApiClient.EMBED_DOMAIN_RESTRICTED_VIDEO_PATH
import `in`.testpress.models.greendao.Content
import `in`.testpress.models.greendao.HtmlContentDao
import `in`.testpress.util.ViewUtils
import `in`.testpress.util.WebViewUtils
import android.app.Activity
import android.os.Bundle
import android.support.annotation.VisibleForTesting
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.LinearLayout
import android.widget.TextView


class HtmlContentFragment : BaseContentDetailFragment() {
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    lateinit var htmlContentDao: HtmlContentDao
    private lateinit var webView: WebView
    private lateinit var webViewUtils: WebViewUtils
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    lateinit var titleView: TextView
    private lateinit var titleLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        htmlContentDao = TestpressSDKDatabase.getHtmlContentDao(activity)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.html_content_detail, container, false);
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

    override fun loadContent() {
        titleView.text = content.title
        titleLayout.visibility = View.VISIBLE


        if (content.rawHtmlContent == null) {
            updateContent()
            return
        }

        val html = """
            <div style='padding-left: 20px; padding-right: 20px;'>
                ${content.rawHtmlContent.textHtml}
            </div>
        """.trimIndent()
        webViewUtils.initWebView(html, activity)
    }

    override fun onUpdateContent(content: Content) {
        htmlContentDao.insertOrReplaceInTx(content.rawHtmlContent)
        contentDao.insertOrReplaceInTx(content)
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
                return false;
            }

            return super.shouldOverrideUrlLoading(activity, url)
        }
    }
}