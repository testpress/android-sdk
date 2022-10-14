package `in`.testpress.course.ui

import `in`.testpress.course.R
import `in`.testpress.models.greendao.Language
import `in`.testpress.ui.BaseToolBarActivity
import android.os.Bundle

class BookmarkDetailActivity : BaseToolBarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookmark_detail)

        val bookmarkID = intent.getLongExtra("BOOKMARKID",0L)
        val language = Language()

        val bookmarksFragment = BookmarksFragment.getInstance(bookmarkID,language)

        supportFragmentManager.beginTransaction()
            .replace(R.id.bookmark_detail_container,bookmarksFragment)
            .commitAllowingStateLoss()

    }
}