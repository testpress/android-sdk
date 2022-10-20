package `in`.testpress.course.ui

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.R
import `in`.testpress.exam.ui.WebViewActivity
import `in`.testpress.fragments.EmptyViewFragment
import `in`.testpress.fragments.EmptyViewListener
import `in`.testpress.models.InstituteSettings
import `in`.testpress.models.SSOUrl
import `in`.testpress.network.TestpressApiClient
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.content_detail_activity.*
import java.io.IOException

class WebViewWithSSO: AppCompatActivity(), EmptyViewListener {
    lateinit var emptyViewFragment: EmptyViewFragment
    val instituteSettings: InstituteSettings? = TestpressSdk.getTestpressSession(this)?.instituteSettings;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.testpress_container_layout)
        initializeEmptyViewFragment()
        showLoading()
        fetchSsoLink()
    }

    private fun initializeEmptyViewFragment() {
        emptyViewFragment = EmptyViewFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, emptyViewFragment)
        transaction.commit()
    }

    fun fetchSsoLink() {
        TestpressApiClient(this, TestpressSdk.getTestpressSession(this)).ssourl
            .enqueue(object: TestpressCallback<SSOUrl>() {
                override fun onSuccess(result: SSOUrl?) {
                    openWebview(result)
                }

                override fun onException(exception: TestpressException?) {
                    hideLoading()
                    showErrorView(exception)
                }

            })
    }

    private fun openWebview(ssoLink: SSOUrl?) {
        val urlToOpen = intent.getStringExtra(URL_TO_OPEN) ?: ""
        val pageTitle = intent.getStringExtra(TITLE) ?: ""
        val intent = Intent(this, WebViewActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP;
        intent.putExtra(WebViewActivity.ACTIVITY_TITLE, pageTitle)
        intent.putExtra(
            WebViewActivity.URL_TO_OPEN,
            instituteSettings?.baseUrl + ssoLink?.ssoUrl + "&next=$urlToOpen"
        )
        startActivity(intent)
        finish()
    }

    private fun showLoading() {
        pb_loading.visibility = View.VISIBLE
        fragment_container.visibility = View.GONE
    }

    private fun hideLoading() {
        pb_loading.visibility = View.GONE
        fragment_container.visibility = View.VISIBLE
    }

    private fun showErrorView(exception: java.lang.Exception?) {
        if (exception?.cause is IOException) {
            val testpressException = TestpressException.networkError(exception.cause as IOException)
            emptyViewFragment.displayError(testpressException)
        } else {
            val testpressException = TestpressException.unexpectedError(exception)
            emptyViewFragment.displayError(testpressException)
        }
    }


    override fun onRetryClick() {
        fetchSsoLink()
    }

    companion object {
        const val URL_TO_OPEN = "URL"
        const val TITLE = "TITLE"

        @JvmStatic
        fun createIntent(context: Context, url: String, title: String): Intent {
            return Intent(context, WebViewWithSSO::class.java).apply {
                putExtra(URL_TO_OPEN, url)
                putExtra(TITLE, title)
            }
        }
    }
}