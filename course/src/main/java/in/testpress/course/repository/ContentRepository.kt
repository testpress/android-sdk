package `in`.testpress.course.repository

import `in`.testpress.course.models.Resource
import `in`.testpress.course.network.TestpressCourseApiClient
import `in`.testpress.course.network.TestpressCourseApiClient.CONTENTS_PATH_v2_4
import `in`.testpress.models.greendao.Content
import `in`.testpress.models.greendao.ContentDao
import `in`.testpress.network.RetrofitCall
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData


class ContentRepository(val contentDao: ContentDao, val courseApiClient: TestpressCourseApiClient) {
    fun getContentFromDB(id: Int): Content? {
        val contents = contentDao.queryBuilder().where(ContentDao.Properties.Id.eq(id)).list()

        if (contents.isNotEmpty()) {
            return contents[0]
        }
        return null
    }

    fun getContent(position: Int, chapterId: Long): Content {
        val contents = getChapterContentsFromDB(chapterId)
        return contents[position]
    }

    fun loadContent(contentId: Int, forceRefresh:Boolean=false): LiveData<Resource<Content>> {
        return object : NetworkBoundResource<Content, Content>() {
            override fun saveCallResult(item: Content) {
                contentDao.insertOrReplace(item)
            }

            override fun shouldFetch(data: Content?): Boolean {
                return forceRefresh
            }

            override fun loadFromDb(): LiveData<Content> {
                val content: MutableLiveData<Content> =   MutableLiveData()
                content.value = getContentFromDB(contentId)
                return content
            }

            override fun createCall(): RetrofitCall<Content> {
                val contentUrl = "$CONTENTS_PATH_v2_4$contentId/"
                return courseApiClient.getContent(contentUrl)
            }

        }.asLiveData()
    }

    fun getChapterContentsFromDB(chapterId: Long): List<Content> {
        return contentDao.queryBuilder()
                .where(
                        ContentDao.Properties.ChapterId.eq(chapterId),
                        ContentDao.Properties.Active.eq(true)
                )
                .orderAsc(ContentDao.Properties.Order)
                .list()
    }
}
