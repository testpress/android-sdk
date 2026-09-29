package `in`.testpress.database.dao

import `in`.testpress.database.BaseDao
import `in`.testpress.database.entities.ContentEntityLite
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query

@Dao
interface ContentLiteDao: BaseDao<ContentEntityLite> {

    @Query("SELECT * FROM runningcontententity WHERE courseId = :courseId AND type = :type")
    fun getRunningContents(courseId: Long, type: Int): PagingSource<Int, ContentEntityLite>

    @Query("SELECT * FROM runningcontententity WHERE courseId = :courseId AND type = :type ORDER BY contentOrder DESC LIMIT 1")
    suspend fun getLastContent(courseId: Long, type: Int): ContentEntityLite?

    @Query("SELECT * FROM runningcontententity WHERE courseId = :courseId AND type = :type")
    fun getUpcomingContents(courseId: Long, type: Int): PagingSource<Int, ContentEntityLite>

    @Query("delete from runningcontententity where courseId = :courseId AND type = :type")
    fun delete(courseId: Long,type: Int)
}