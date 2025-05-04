package `in`.testpress.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import `in`.testpress.R
import `in`.testpress.ui.fragments.GlobalSearchFragment

class GlobalSearchActivity : AppCompatActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.global_search_activity_layout)
        val fragment = GlobalSearchFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.global_search_fragment_container, fragment).commitAllowingStateLoss()
    }

}