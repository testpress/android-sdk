package `in`.testpress.course.ui

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.course.R
import `in`.testpress.exam.api.TestpressExamApiClient
import `in`.testpress.exam.ui.TestFragment
import `in`.testpress.exam.ui.TestFragment.DEFAULT_EXAM_TIME
import `in`.testpress.exam.ui.TestFragment.INFINITE_EXAM_TIME
import `in`.testpress.models.greendao.Attempt
import `in`.testpress.ui.AbstractWebViewActivity
import `in`.testpress.util.BaseJavaScriptInterface
import android.content.Intent
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.widget.Toast
import android.widget.Toolbar
import androidx.core.view.isVisible


class CustomTestGenerationActivity: AbstractWebViewActivity() {

    override fun onWebViewInitializationSuccess() {
        webViewFragment.addJavascriptInterface(JavaScriptInterface(this),"AndroidInterface")
    }

    fun getAttempt(attemptId: String) {
        val apiClient = TestpressExamApiClient(this)
        apiClient.startAttempt("api/v2.2/attempts/$attemptId/start/")
            .enqueue(object : TestpressCallback<Attempt>() {
                override fun onSuccess(result: Attempt?) {
                    // Check if the remaining time for the attempt is infinite we reset to default value of 24 hours.
                    // This is done because our app doesn't support exams with infinite timing.
                    result?.let {
                        if (it.remainingTime == INFINITE_EXAM_TIME) {
                            it.remainingTime = DEFAULT_EXAM_TIME
                        }
                        startExam(result)
                    }
                }

                override fun onException(exception: TestpressException) {
                    Toast.makeText(
                        this@CustomTestGenerationActivity,
                        "Something went wrong, Please try again later",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            })
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
            super.onBackPressed()
        }
    }

}

class JavaScriptInterface(val activity: CustomTestGenerationActivity):BaseJavaScriptInterface(activity) {

    @JavascriptInterface
    fun startCustomTest(attemptId: String) {
        activity.getAttempt(attemptId)
    }

    @JavascriptInterface
    fun startCustomTestInQuizMode(attemptId: String) {
        val intent = Intent(activity, QuizActivity::class.java).apply {
            putExtra("ATTEMPT_ID", attemptId.toLong())
        }
        activity.startActivity(intent)
        activity.finish()
    }

}