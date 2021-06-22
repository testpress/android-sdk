package `in`.testpress.ui

import `in`.testpress.R
import `in`.testpress.ui.fragments.DiscussionFragment
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log

class DiscussionActivity: BaseToolBarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.testpress_container_layout)
        Log.d("TAG", "onCreate: DiscussionActivity")
        val fragment = DiscussionFragment()
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment).commitAllowingStateLoss()
    }

    companion object {
        @JvmStatic
        fun createIntent(context: Context): Intent {
            return Intent(context, DiscussionActivity::class.java)
        }
    }
}