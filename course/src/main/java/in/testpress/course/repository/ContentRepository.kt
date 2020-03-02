package `in`.testpress.course.repository

import `in`.testpress.course.api.TestpressCourseApiClient.CONTENTS_PATH_v2_4
import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.domain.asDomainContent
import `in`.testpress.course.network.CourseNetwork
import `in`.testpress.course.network.NetworkContent
import `in`.testpress.course.network.Resource
import `in`.testpress.course.network.asDatabaseModel
import `in`.testpress.course.network.asGreenDaoModel
import `in`.testpress.models.greendao.ContentDao
import `in`.testpress.network.RetrofitCall
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations

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
                return forceRefresh
            }

            override fun loadFromDb(): LiveData<DomainContent> {
                return Transformations.map(roomContentDao.findById(contentId)) {
                    it.asDomainContent()
                }
            }

            override fun createCall(): RetrofitCall<NetworkContent> {
                val contentUrl = "$CONTENTS_PATH_v2_4$contentId/"
                return courseNetwork.getNetworkContent(contentUrl)
            }
        }.asLiveData()
    }

    fun getChapterContentsFromDB(chapterId: Long): LiveData<List<DomainContent>>? {
        return Transformations.map(roomContentDao.getChapterContents(chapterId)) {
            it.asDomainContent()
        }
    }
}