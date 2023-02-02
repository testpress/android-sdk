package `in`.testpress.database.dao

import `in`.testpress.database.BaseDao
import `in`.testpress.database.entities.UpcomingContentEntity
import androidx.room.Dao
import androidx.room.Query

@Dao
interface UpcomingContentDao : BaseDao<UpcomingContentEntity> {
    @Query("SELECT * FROM upcomingcontententity where courseId = :courseId")
    fun getAll(courseId: Long): List<UpcomingContentEntity>

    @Query("delete from upcomingcontententity where courseId = :courseId")
    fun deleteAll(courseId: Long)
}