package `in`.testpress.course.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import `in`.testpress.course.R
import `in`.testpress.course.TestpressCourse
import `in`.testpress.store.TestpressStore
import `in`.testpress.ui.BaseToolBarActivity

class CoursePreviewActivity: BaseToolBarActivity() {
    private var isPurchaseSuccessful = false
    private var continuePurchase = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.testpress_container_layout)
        val fragment = CoursePreviewFragment()
        fragment.arguments = intent.extras
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment)
            .commitAllowingStateLoss()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == TestpressStore.STORE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            isPurchaseSuccessful = true
            continuePurchase = data.getBooleanExtra(TestpressStore.CONTINUE_PURCHASE, false)
            if (!continuePurchase) {
                setResult(RESULT_OK, data)
                finish()
            }
        }
    }

    override fun finish() {
        if (isPurchaseSuccessful) {
            val intent = Intent().apply {
                putExtra(TestpressStore.PAYMENT_SUCCESS, true)
                putExtra(TestpressStore.CONTINUE_PURCHASE, continuePurchase)
            }
            setResult(RESULT_OK, intent)
        }
        super.finish()
    }

    companion object {

        @JvmStatic
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