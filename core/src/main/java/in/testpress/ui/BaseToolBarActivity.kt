package `in`.testpress.ui

import `in`.testpress.R
import `in`.testpress.core.TestpressSdk
import `in`.testpress.core.TestpressSession
import `in`.testpress.network.RetrofitCall
import `in`.testpress.util.CommonUtils
import `in`.testpress.util.UIUtils
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import `in`.testpress.util.applySystemBarColors

/**
 * Base activity used to support the toolbar & handle backpress.
 * Activity that extends this activity must needs to include the #layout/testpress_toolbar
 * in its view.
 */

open class BaseToolBarActivity: AppCompatActivity() {

    companion object {
        const val ACTIONBAR_TITLE = "title"
    }

    private var session: TestpressSession? = null
    protected lateinit var logo: ImageView
    protected lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applySystemBarColors(window.decorView.rootView)
    }

    override fun setContentView(layoutResId: Int) {
        super.setContentView(layoutResId)
        preventScreenshot()
        setupActionBar()
    }

    override fun setContentView(view: View?) {
        super.setContentView(view)
        preventScreenshot()
        setupActionBar()
    }

    private fun preventScreenshot() {
        session = TestpressSdk.getTestpressSession(this)
        if (session != null && session!!.instituteSettings.isScreenshotDisabled) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        }
    }

    private fun setupActionBar() {
        toolbar = findViewById(R.id.toolbar)
        logo = findViewById(R.id.toolbar_logo)
        setSupportActionBar(toolbar)
        supportActionBar!!.setHomeButtonEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    open fun setActionBarTitle(@StringRes titleId: Int) {
        supportActionBar!!.setTitle(titleId)
    }

    open fun setActionBarTitle(title: String) {
        supportActionBar!!.title = title
    }

    open fun showLogoInToolbar() {
        supportActionBar!!.title = ""
        if (session == null || session!!.instituteSettings == null) {
            return
        }
        UIUtils.loadLogoInView(logo, this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            /**
             * Set result with home button pressed flag if activity is started by
             * [.startActivityForResult]
             *
             * Note: [.getCallingActivity] return null if activity is started using
             * [.startActivity] instead of [.startActivityForResult]
             */
            if (callingActivity == null) {
                onBackPressed()
            } else {
                setResult(RESULT_CANCELED, Intent().putExtras(getDataToSetResult()))
                super.onBackPressed()
            }
            return true
        }
        return false
    }

    protected open fun getDataToSetResult(): Bundle {
        return Bundle().apply { putBoolean(TestpressSdk.ACTION_PRESSED_HOME, true) }
    }

    open fun getRetrofitCalls(): Array<RetrofitCall<*>> {
        return arrayOf()
    }

    override fun onBackPressed() {
        /**
         * Set result with home button pressed flag false if activity is started by
         * [.startActivityForResult]
         */
        if (callingActivity != null) {
            setResult(RESULT_CANCELED, Intent().putExtra(TestpressSdk.ACTION_PRESSED_HOME, false))
        }
        try {
            super.onBackPressed()
        } catch (e: IllegalStateException) {
            supportFinishAfterTransition()
        }
    }

    override fun onResume() {
        super.onResume()
        preventScreenshot()
    }

    public override fun onDestroy() {
        CommonUtils.cancelAPIRequests(getRetrofitCalls())
        super.onDestroy()
    }

}