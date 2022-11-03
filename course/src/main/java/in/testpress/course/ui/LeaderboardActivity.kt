package `in`.testpress.course.ui

import `in`.testpress.ui.BaseToolBarActivity
import android.os.Bundle
import `in`.testpress.course.R


class LeaderboardActivity : BaseToolBarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.testpress_container_layout)
        LeaderboardFragment.show(this, R.id.fragment_container)
    }
}