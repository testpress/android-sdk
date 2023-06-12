package `in`.testpress.course.ui

import `in`.testpress.R
import `in`.testpress.databinding.BaseTestpressWebviewContainerLayoutBinding
import `in`.testpress.exam.ui.ReviewStatsActivity
import `in`.testpress.fragments.WebViewFragment
import `in`.testpress.models.greendao.Attempt
import `in`.testpress.ui.BaseToolBarActivity
import `in`.testpress.util.BaseJavaScriptInterface
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.widget.Toast
import org.json.JSONException
import org.json.JSONObject

class CustomTestGenerationActivity: BaseToolBarActivity(), WebViewFragment.Listener {

    private var _layout: BaseTestpressWebviewContainerLayoutBinding? = null
    private val layout: BaseTestpressWebviewContainerLayoutBinding get() = _layout!!
    private lateinit var webViewFragment: WebViewFragment
    private var courseSlug: String? = null
    private val title: String = "Custom Module"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _layout = BaseTestpressWebviewContainerLayoutBinding.inflate(layoutInflater)
        setContentView(layout.root)
        parseArguments()
        setActionBarTitle(title)
        initializeWebViewFragment()
    }

    override fun onBackPressed() {
        if (webViewFragment.canGoBack()) {
            webViewFragment.goBack()
        } else {
            super.onBackPressed()
        }
    }

    private fun parseArguments() {
        courseSlug = intent.getStringExtra(COURSE_SLUG)
    }

    private fun initializeWebViewFragment() {
        if (courseSlug.isNullOrEmpty()) {
            Toast.makeText(this,"Not able to take exam",Toast.LENGTH_SHORT).show()
            this.finish()
            return
        }
        webViewFragment = WebViewFragment(
            url = "/courses/$courseSlug/custom_test_generation/",
            webViewFragmentSettings = WebViewFragment.Settings()
        )
        webViewFragment.setListener(this)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, webViewFragment)
            .commit()
    }

    override fun onWebViewInitializationSuccess() {
        webViewFragment.addJavascriptInterface(JavaScriptInterface(this),"AndroidInterface")
    }

    companion object {
        const val COURSE_SLUG = "COURSE_SLUG"

        fun createIntent(
            currentContext: Context,
            courseSlug: String
        ): Intent {
            return Intent(currentContext, CustomTestGenerationActivity::class.java).apply {
                putExtra(COURSE_SLUG, courseSlug)
            }
        }
    }
}

class JavaScriptInterface(val activity: Activity):BaseJavaScriptInterface(activity) {

    @JavascriptInterface
    fun onExamEndCallBack(jsonData: String) {
        try {
            val attempt = parseJsonToAttempt(jsonData)
            activity.finish()
            activity.startActivity(ReviewStatsActivity.createIntent(activity, attempt))
        } catch (e: JSONException) {
            activity.finish()
            Toast.makeText(activity, "Review Not available for this exam", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun parseJsonToAttempt(json: String): Attempt {
        try {
            val jsonObject = JSONObject(json)
            val attempt = Attempt()
            attempt.url = jsonObject.optString("url")
            attempt.id = jsonObject.optLong("id")
            attempt.date = jsonObject.optString("date")
            attempt.totalQuestions = jsonObject.optInt("total_questions")
            attempt.score = jsonObject.optString("score")
            attempt.reviewUrl = jsonObject.optString("review_url")
            attempt.questionsUrl = jsonObject.optString("questions_url")
            attempt.correctCount = jsonObject.optInt("correct_count")
            attempt.incorrectCount = jsonObject.optInt("incorrect_count")
            attempt.lastStartedTime = jsonObject.optString("last_started_time")
            attempt.remainingTime = jsonObject.optString("remaining_time")
            attempt.timeTaken = jsonObject.optString("time_taken")
            attempt.state = jsonObject.optString("state")
            attempt.speed = jsonObject.optInt("speed")
            attempt.accuracy = jsonObject.optInt("accuracy")
            attempt.percentage = jsonObject.optString("percentage")
            attempt.lastViewedQuestionId = jsonObject.optInt("last_viewed_question_id")
            attempt.reviewPdf = jsonObject.optString("review_pdf")
            return attempt
        } catch (e: JSONException) {
            throw e
        }
    }
}