package `in`.testpress.course.ui

import `in`.testpress.course.R
import `in`.testpress.course.fragments.ContentLoadingFragment
import `in`.testpress.course.ui.AvailableCourseListFragment
import `in`.testpress.store.TestpressStore
import `in`.testpress.ui.BaseToolBarActivity
import android.app.Activity
import android.content.Intent
import android.os.Bundle


class AvailableCoursesListActivity : BaseToolBarActivity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.testpress_container_layout)
        val fragment = AvailableCourseListFragment()
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment).commitAllowingStateLoss()
        supportActionBar!!.title = "Store"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == TestpressStore.STORE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null && !data.getBooleanExtra(TestpressStore.CONTINUE_PURCHASE, false)) {
            setResult(Activity.RESULT_OK, data)
            finish()
        }
    }
}