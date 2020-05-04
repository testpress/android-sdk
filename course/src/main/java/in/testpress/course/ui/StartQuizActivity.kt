package `in`.testpress.course.ui

import `in`.testpress.course.R
import `in`.testpress.course.fragments.StartQuizFragment
import `in`.testpress.ui.BaseToolBarActivity
import android.app.Activity
import android.os.Bundle

class StartQuizActivity : BaseToolBarActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.testpress_container_layout)
        val fragment = StartQuizFragment().apply {
            arguments = intent.extras
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment).commitAllowingStateLoss()
    }
}