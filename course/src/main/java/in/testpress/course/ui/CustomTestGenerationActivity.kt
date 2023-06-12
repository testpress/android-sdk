package `in`.testpress.course.ui

import `in`.testpress.exam.ui.ReviewStatsActivity
import `in`.testpress.fragments.WebViewFragment
import `in`.testpress.models.greendao.Attempt
import `in`.testpress.ui.WebViewWithSSOActivity
import `in`.testpress.util.BaseJavaScriptInterface
import android.app.Activity
import android.webkit.JavascriptInterface
import android.widget.Toast
import org.json.JSONException
import org.json.JSONObject

class CustomTestGenerationActivity: WebViewWithSSOActivity(), WebViewFragment.Listener {

    override fun initializeWebViewFragmentListener() {
        webViewFragment.setListener(this)
    }

    override fun onWebViewInitializationSuccess() {
        webViewFragment.addJavascriptInterface(JavaScriptInterface(this),"AndroidInterface")
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