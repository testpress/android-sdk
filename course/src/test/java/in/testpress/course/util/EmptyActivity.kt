package `in`.testpress.course.util

import `in`.testpress.course.R
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class EmptyActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.TestpressTheme)
        super.onCreate(savedInstanceState)
    }
}
