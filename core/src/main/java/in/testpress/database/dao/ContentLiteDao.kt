package `in`.testpress.database.dao

import `in`.testpress.database.BaseDao
import `in`.testpress.database.entities.ContentEntityLite
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query

@Dao
interface ContentLiteDao: BaseDao<ContentEntityLite> {

    @Query("SELECT * FROM contententitylite WHERE courseId = :courseId AND type = 1 ORDER BY start DESC")
    fun getRunningContents(courseId: Long): PagingSource<Int, ContentEntityLite>

    @Query("delete from contententitylite where courseId = :courseId AND type = :type")
    fun delete(courseId: Long,type: Int)
}