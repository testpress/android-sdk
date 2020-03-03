package `in`.testpress.course.repository

import `in`.testpress.course.api.TestpressCourseApiClient.CONTENTS_PATH_v2_4
import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.domain.asDomainContent
import `in`.testpress.course.domain.asDomainContents
import `in`.testpress.course.network.CourseNetwork
import `in`.testpress.course.network.NetworkContent
import `in`.testpress.course.network.Resource
import `in`.testpress.course.network.asDatabaseModel
import `in`.testpress.course.network.asGreenDaoModel
import `in`.testpress.models.greendao.ContentDao
import `in`.testpress.network.RetrofitCall
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class ContentRepository(
    val roomContentDao: `in`.testpress.database.ContentDao,
    val contentDao: ContentDao,
    val courseNetwork: CourseNetwork
) {

    fun loadContent(
        contentId: Long,
        forceRefresh: Boolean = false
    ): LiveData<Resource<DomainContent>> {
        return object : NetworkBoundResource<DomainContent, NetworkContent>() {
            override fun saveNetworkResponseToDB(item: NetworkContent) {
                roomContentDao.insert(item.asDatabaseModel())
                contentDao.insertOrReplace(item.asGreenDaoModel())
            }

            override fun shouldFetch(data: DomainContent?): Boolean {
                return forceRefresh || loadFromDb().value == null
            }

            override fun loadFromDb(): LiveData<DomainContent> {
                val liveData = MutableLiveData<DomainContent>()
                val contents = contentDao.queryBuilder().where(ContentDao.Properties.Id.eq(contentId)).list()
                if (contents.isNotEmpty()) {
                    liveData.value = contents[0].asDomainContent()
                }
                return liveData
            }

            override fun createCall(): RetrofitCall<NetworkContent> {
                val contentUrl = "$CONTENTS_PATH_v2_4$contentId/"
                return courseNetwork.getNetworkContent(contentUrl)
            }
        }.asLiveData()
    }

    fun getContent(position: Int, chapterId: Long): DomainContent {
        val contents = contentDao.queryBuilder()
            .where(
                ContentDao.Properties.ChapterId.eq(chapterId),
                ContentDao.Properties.Active.eq(true)
            )
            .orderAsc(ContentDao.Properties.Order)
            .list()
        return contents[position].asDomainContent()
    }

    fun getContentsForChapterFromDB(chapterId: Long): LiveData<List<DomainContent>>? {
        val contentsLiveData = MutableLiveData<List<DomainContent>>()
        val contents = contentDao.queryBuilder()
            .where(
                ContentDao.Properties.ChapterId.eq(chapterId),
                ContentDao.Properties.Active.eq(true)
            ).list().asDomainContents()
        contentsLiveData.value = contents
        return contentsLiveData
    }
}