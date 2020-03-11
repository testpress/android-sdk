package `in`.testpress.course.repository

import `in`.testpress.core.TestpressSDKDatabase
import `in`.testpress.course.network.NetworkContent
import `in`.testpress.course.network.asGreenDaoModel
import android.content.Context

class HtmlContentRepository(context: Context) : ContentRepository(context) {
    val htmlContentDao = TestpressSDKDatabase.getHtmlContentDao(context)

    override fun storeContentAndItsRelationsToDB(content: NetworkContent) {
        val greenDaoContent = content.asGreenDaoModel()
        content.htmlContent ?.let {
            val htmlContent = it.asGreenDaoModel()
            greenDaoContent.htmlId = htmlContent.id
            htmlContentDao.insertOrReplace(htmlContent)
        }
    }
}