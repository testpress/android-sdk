package `in`.testpress.ui

import android.content.Context
import android.content.Intent

class WebViewWithSSOActivity:BaseSSOActivity() {

    override fun onWebViewInitializationSuccess() {}

    companion object {
        @JvmStatic
        fun createIntent(
            context: Context,
            title: String,
            urlPath: String,
            isSSORequired: Boolean,
        ): Intent {
            return createUrlIntent(context, title, urlPath, isSSORequired, WebViewWithSSOActivity::class.java)
        }
    }
}