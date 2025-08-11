package `in`.testpress.course.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import `in`.testpress.course.R
import `in`.testpress.store.TestpressStore
import `in`.testpress.ui.BaseToolBarActivity

class CourseListActivity : BaseToolBarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.testpress_container_layout)
        val fragment = CourseListFragment()
        fragment.arguments = intent.extras
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment)
            .commitAllowingStateLoss()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == TestpressStore.STORE_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            data?.let {  intentData ->
                if (!intentData.getBooleanExtra(TestpressStore.CONTINUE_PURCHASE, false)) {
                    supportFragmentManager.fragments.forEach { fragment ->
                        fragment.onActivityResult(requestCode, resultCode, intentData)
                    }
                }
            }
        }
    }
}