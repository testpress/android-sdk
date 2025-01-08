package `in`.testpress.course.ui

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.course.R
import `in`.testpress.exam.api.TestpressExamApiClient
import `in`.testpress.exam.ui.ReviewStatsActivity
import `in`.testpress.exam.ui.TestFragment
import `in`.testpress.exam.ui.TestFragment.DEFAULT_EXAM_TIME
import `in`.testpress.exam.ui.TestFragment.INFINITE_EXAM_TIME
import `in`.testpress.models.greendao.Attempt
import `in`.testpress.ui.AbstractWebViewActivity
import `in`.testpress.util.BaseJavaScriptInterface
import `in`.testpress.util.extension.toast
import android.content.Intent
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.widget.Toolbar
import androidx.core.view.isVisible
import `in`.testpress.exam.network.NetworkAttempt
import `in`.testpress.exam.network.asGreenDaoModel


class CustomTestGenerationActivity: AbstractWebViewActivity() {

    override fun onWebViewInitializationSuccess() {
        webViewFragment.addJavascriptInterface(JavaScriptInterface(this),"AndroidInterface")
    }

    fun startAttempt(attemptId: String) {
        val apiClient = TestpressExamApiClient(this)
        apiClient.startAttempt("api/v2.2/attempts/$attemptId/start/")
            .enqueue(object : TestpressCallback<NetworkAttempt>() {
                override fun onSuccess(result: NetworkAttempt) {
                    // Check if the remaining time for the attempt is infinite we reset to default value of 24 hours.
                    // This is done because our app doesn't support exams with infinite timing.
                    val attempt = result.asGreenDaoModel()
                    attempt.let {
                        if (it.remainingTime == INFINITE_EXAM_TIME) {
                            it.remainingTime = DEFAULT_EXAM_TIME
                        }
                        startExam(it)
                    }
                }

                override fun onException(exception: TestpressException) {
                    showErrorToast(exception)
                }
            })
    }

    fun getAttempt(attemptId: String) {
        val apiClient = TestpressExamApiClient(this)
        apiClient.getAttempt("api/v2.2/attempts/$attemptId/")
            .enqueue(object : TestpressCallback<Attempt>() {
                override fun onSuccess(result: Attempt?) {
                    result?.let {
                        startActivity(
                            ReviewStatsActivity.createIntent(
                                this@CustomTestGenerationActivity,
                                result
                            )
                        )
                    }
                }

                override fun onException(exception: TestpressException) {
                    showErrorToast(exception)
                }
            })
    }

    fun showErrorToast(exception: TestpressException) {
        when {
            exception.isForbidden -> toast(R.string.custom_test_permission_error)
            exception.isNetworkError -> toast(R.string.custom_test_network_error)
            exception.isPageNotFound -> toast(R.string.custom_test_page_not_found_error)
            exception.isServerError -> toast(R.string.custom_test_server_error)
            else -> toast(R.string.custom_test_unknown_error)
        }
        finish()
    }

    private fun startExam(attempt: Attempt) {
        findViewById<Toolbar>(R.id.toolbar).isVisible = false
        val testFragment = TestFragment()
        val bundle  = Bundle()
        bundle.putParcelable("attempt", attempt)
        testFragment.arguments = bundle
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, testFragment).commitAllowingStateLoss()
    }

    override fun onBackPressed() {
        val testFragment: TestFragment? =
            (supportFragmentManager.findFragmentById(R.id.fragment_container) as? TestFragment?)
        if (testFragment != null) {
            testFragment.showEndExamAlert()
        } else {
            this.finish()
        }
    }

}

class JavaScriptInterface(val activity: CustomTestGenerationActivity):BaseJavaScriptInterface(activity) {

    @JavascriptInterface
    fun startCustomTest(attemptId: String) {
        activity.startAttempt(attemptId)
    }

    @JavascriptInterface
    fun startCustomTestInQuizMode(attemptId: String) {
        val intent = Intent(activity, QuizActivity::class.java).apply {
            putExtra("ATTEMPT_ID", attemptId.toLong())
        }
        activity.startActivity(intent)
        activity.finish()
    }

    @JavascriptInterface
    fun showReview(attemptId: String) {
        activity.getAttempt(attemptId)
    }

}