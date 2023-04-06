package `in`.testpress.database.dao

import `in`.testpress.database.BaseDao
import `in`.testpress.database.entities.ContentEntityLite
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query

@Dao
interface ContentLiteDao: BaseDao<ContentEntityLite> {

    @Query("SELECT * FROM runningcontententity WHERE courseId = :courseId ORDER BY start DESC")
    fun getAll(courseId: Long): PagingSource<Int, ContentEntityLite>

    @Query("delete from runningcontententity where courseId = :courseId")
    fun deleteAll(courseId: Long)
}