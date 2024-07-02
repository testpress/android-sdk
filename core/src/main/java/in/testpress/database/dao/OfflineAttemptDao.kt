package `in`.testpress.database.dao

import `in`.testpress.database.BaseDao
import `in`.testpress.database.entities.OfflineAttempt
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface OfflineAttemptDao: BaseDao<OfflineAttempt>{

    @Insert
    suspend fun insert(offlineAttempt: OfflineAttempt): Long

    @Query("SELECT * FROM OfflineAttempt WHERE id = :attemptId")
    suspend fun getById(attemptId: Long): OfflineAttempt
}