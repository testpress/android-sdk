package `in`.testpress.course.repository

import `in`.testpress.course.db.asDomainModel
import `in`.testpress.course.domain_models.DomainContent
import `in`.testpress.course.models.Resource
import `in`.testpress.course.network.TestpressCourseApiClient
import `in`.testpress.course.network.TestpressCourseApiClient.CONTENTS_PATH_v2_4
import `in`.testpress.course.network_models.NetworkContent
import `in`.testpress.course.network_models.asDatabaseModel
import `in`.testpress.course.network_models.asGreenDaoModel
import `in`.testpress.models.greendao.Content
import `in`.testpress.models.greendao.ContentDao
import `in`.testpress.network.RetrofitCall
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData


class ContentRepository(val roomContentDao: `in`.testpress.course.db.ContentDao, val contentDao: ContentDao, val courseApiClient: TestpressCourseApiClient) {
    fun getContentFromDB(id: Int): `in`.testpress.course.db.Content {
        return roomContentDao.findById(id.toLong())
    }

    fun getContent(position: Int, chapterId: Long): Content {
        val contents = getChapterContentsFromDB(chapterId)
        return contents[position]
    }

    fun loadContent(contentId: Int, forceRefresh: Boolean = false): LiveData<Resource<DomainContent>> {
        return object : NetworkBoundResource<DomainContent, NetworkContent>() {
            override fun saveCallResult(item: NetworkContent) {
                roomContentDao.insert(item.asDatabaseModel())
                contentDao.insertOrReplace(item.asGreenDaoModel())
            }

            override fun shouldFetch(data: DomainContent?): Boolean {
                return forceRefresh
            }

            override fun loadFromDb(): LiveData<DomainContent> {
                val content: MutableLiveData<DomainContent> = MutableLiveData()
                content.value = roomContentDao.findById(contentId.toLong()).asDomainModel()
                return content
            }

            override fun createCall(): RetrofitCall<NetworkContent> {
                val contentUrl = "$CONTENTS_PATH_v2_4$contentId/"
                return courseApiClient.getNetworkContent(contentUrl)
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
