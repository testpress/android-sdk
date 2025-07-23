package `in`.testpress.exam.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import `in`.testpress.exam.R

class SearchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.testpress_container_layout_without_toolbar)
        val searchFragment = SearchFragment().apply {
            arguments = intent.extras
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, searchFragment).commitAllowingStateLoss()
    }
}