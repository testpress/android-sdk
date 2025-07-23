package `in`.testpress.exam.ui

import android.os.Bundle
import `in`.testpress.exam.R
import `in`.testpress.ui.BaseToolBarActivity

class AccessCodeActivity : BaseToolBarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.testpress_container_layout)
        if (savedInstanceState == null) {
            AccessCodeFragment.show(this, R.id.fragment_container)
        }
    }
}
