package `in`.testpress.database.dao

import `in`.testpress.database.BaseDao
import `in`.testpress.database.entities.RunningContentEntity
import androidx.room.Dao
import androidx.room.Query

@Dao
interface RunningContentDao: BaseDao<RunningContentEntity> {
    @Query("SELECT * FROM runningcontententity where courseId = :courseId")
    fun getAll(courseId: Long): List<RunningContentEntity>

    @Query("delete from runningcontententity where courseId = :courseId")
    fun deleteAll(courseId: Long)
}