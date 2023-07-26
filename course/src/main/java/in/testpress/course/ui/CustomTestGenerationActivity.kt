package `in`.testpress.course.ui

import `in`.testpress.BuildConfig
import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.course.R
import `in`.testpress.exam.api.TestpressExamApiClient
import `in`.testpress.exam.ui.TestFragment
import `in`.testpress.models.greendao.Attempt
import `in`.testpress.models.greendao.AttemptSection
import `in`.testpress.ui.AbstractWebViewActivity
import `in`.testpress.util.BaseJavaScriptInterface
import android.os.Bundle
import android.util.Log
import android.webkit.JavascriptInterface
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


class CustomTestGenerationActivity: AbstractWebViewActivity() {

    override fun onWebViewInitializationSuccess() {
        webViewFragment.addJavascriptInterface(JavaScriptInterface(this),"AndroidInterface")
    }

    fun getAttempt(attemptId: String) {
        val apiClient = TestpressExamApiClient(this)
        apiClient.startAttempt("api/v2.2/attempts/$attemptId/start/").enqueue(object : TestpressCallback<Attempt>(){
            override fun onSuccess(result: Attempt?) {
                startExam(result!!)
            }

            override fun onException(exception: TestpressException?) {
                Toast.makeText(this@CustomTestGenerationActivity,"Not able to start exam",Toast.LENGTH_SHORT).show()
                finish()
            }
        })
    }

    private fun startExam(attempt: Attempt) {
        val testFragment = TestFragment()
        val bundle  = Bundle()
        bundle.putParcelable("attempt", attempt)
        testFragment.arguments = bundle
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, testFragment).commitAllowingStateLoss()
    }

}

class JavaScriptInterface(val activity: CustomTestGenerationActivity):BaseJavaScriptInterface(activity) {

    @JavascriptInterface
    fun onExamEndCallBack(jsonData: String) {
        Toast.makeText(activity, jsonData, Toast.LENGTH_SHORT).show()
        activity.getAttempt("41546")
    }

}