package `in`.testpress.course.ui

import `in`.testpress.course.R
import `in`.testpress.course.fragments.DownloadsFragment
import `in`.testpress.ui.BaseToolBarActivity
import android.content.Context
import android.content.Intent
import android.os.Bundle

class DownloadsActivity: BaseToolBarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.testpress_container_layout)
        val fragment = DownloadsFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment).commitAllowingStateLoss()
    }

    companion object {
        @JvmStatic
        fun createIntent(context: Context): Intent {
            return Intent(context, DownloadsActivity::class.java)
        }
    }
}