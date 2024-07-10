package `in`.testpress.database.dao

import `in`.testpress.database.BaseDao
import `in`.testpress.database.entities.OfflineCourseAttempt
import androidx.room.Dao
import androidx.room.Query

@Dao
interface OfflineCourseAttemptDao: BaseDao<OfflineCourseAttempt> {
    @Query("SELECT * FROM OfflineCourseAttempt WHERE assessmentId = :attemptId")
    suspend fun getById(attemptId: Long): OfflineCourseAttempt?
}