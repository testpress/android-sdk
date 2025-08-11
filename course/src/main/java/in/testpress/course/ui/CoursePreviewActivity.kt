package `in`.testpress.course.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import `in`.testpress.course.R
import `in`.testpress.course.TestpressCourse
import `in`.testpress.ui.BaseToolBarActivity

class CoursePreviewActivity: BaseToolBarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.testpress_container_layout)
        val fragment = CoursePreviewFragment()
        fragment.arguments = intent.extras
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment)
            .commitAllowingStateLoss()
    }

    companion object {
        fun createIntent(
            courseIds: ArrayList<Int>,
            context: Context,
            productSlug: String?
        ): Intent {
            return Intent(context, CoursePreviewActivity::class.java).apply {
                putExtra(TestpressCourse.COURSE_IDS, courseIds)
                putExtra(TestpressCourse.PRODUCT_SLUG, productSlug)
            }
        }
    }
}