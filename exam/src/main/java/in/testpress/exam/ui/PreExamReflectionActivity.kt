package `in`.testpress.exam.ui

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.core.TestpressSdk
import `in`.testpress.models.SSOUrl
import `in`.testpress.network.TestpressApiClient
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.JavascriptInterface
import android.widget.Toast
import `in`.testpress.ui.AbstractWebViewActivity
import `in`.testpress.util.BaseJavaScriptInterface

class PreExamReflectionActivity : AbstractWebViewActivity() {

    private var isMandatory = false
    private var isSsoUrlFetched = false
    private var isWebViewFragmentInitialized = false
    private var allowValidationErrors = false

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        isMandatory = intent.getBooleanExtra(IS_MANDATORY, false)
        allowValidationErrors = intent.getBooleanExtra(ALLOW_VALIDATION_ERRORS, false)
        fetchSsoLink()
    }

    override fun initializeWebViewFragment() {
        if (isSsoUrlFetched) {
            isWebViewFragmentInitialized = true
            super.initializeWebViewFragment()
        }
    }

    private fun fetchSsoLink() {
        val session = TestpressSdk.getTestpressSession(this)
        if (session == null) {
            isSsoUrlFetched = true
            initializeWebViewFragment()
            return
        }

        TestpressApiClient(this, session).ssourl.enqueue(object : TestpressCallback<SSOUrl>() {
            override fun onSuccess(result: SSOUrl?) {
                val ssoUrl = result?.ssoUrl
                if (ssoUrl.isNullOrBlank()) {
                    isSsoUrlFetched = true
                    initializeWebViewFragment()
                    return
                }
                val nextUrl = Uri.encode(urlPath)
                urlPath = "${session.instituteSettings.baseUrl}$ssoUrl&next=$nextUrl"
                isSsoUrlFetched = true
                initializeWebViewFragment()
            }

            override fun onException(exception: TestpressException?) {
                isSsoUrlFetched = true
                initializeWebViewFragment()
            }
        })
    }

    override fun onWebViewInitializationSuccess() {
        webViewFragment.addJavascriptInterface(
            ReflectionJsInterface(this), "AndroidInterface"
        )
    }

    fun onReflectionSubmitted() {
        runOnUiThread {
            setResult(
                Activity.RESULT_OK,
                Intent().putExtra(RESULT_ACTION, ACTION_SUBMITTED)
            )
            finish()
        }
    }

    fun onReflectionSkipped() {
        runOnUiThread {
            if (isMandatory) {
                Toast.makeText(
                    this,
                    "Please complete the reflection before starting the exam.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                setResult(
                    Activity.RESULT_OK,
                    Intent().putExtra(RESULT_ACTION, ACTION_SKIPPED)
                )
                finish()
            }
        }
    }

    override fun onBackPressed() {
        if (isWebViewFragmentInitialized && webViewFragment.canGoBack()) {
            webViewFragment.goBack()
            return
        }
        if (isMandatory) {
            Toast.makeText(
                this,
                "Please complete the reflection before starting the exam.",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    companion object {
        const val IS_MANDATORY = "IS_MANDATORY"
        const val ALLOW_VALIDATION_ERRORS = "allow_validation_errors"
        const val REQUEST_CODE = 1001
        const val RESULT_ACTION = "RESULT_ACTION"
        const val ACTION_SUBMITTED = "ACTION_SUBMITTED"
        const val ACTION_SKIPPED = "ACTION_SKIPPED"

        @JvmStatic
        fun createIntent(
            context: Context,
            baseUrl: String,
            examId: Long,
            formId: Long,
            isMandatory: Boolean
        ): Intent {
            val cleanBaseUrl = baseUrl.trimEnd('/')
            val url = "$cleanBaseUrl/exam-mindset/$examId/reflection/$formId/"
            return AbstractWebViewActivity.createIntent(
                context,
                title = "",
                urlPath = url,
                isAuthenticationRequired = true,
                activityToOpen = PreExamReflectionActivity::class.java
            ).apply {
                putExtra(IS_MANDATORY, isMandatory)
                putExtra(ALLOW_VALIDATION_ERRORS, true)
            }
        }
    }
}

class ReflectionJsInterface(
    private val activity: PreExamReflectionActivity
) : BaseJavaScriptInterface(activity) {

    @JavascriptInterface
    fun onReflectionSubmitted() = activity.onReflectionSubmitted()

    @JavascriptInterface
    fun onReflectionSkipped() = activity.onReflectionSkipped()
}
