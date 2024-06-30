package `in`.testpress.database.dao

import `in`.testpress.database.BaseDao
import `in`.testpress.database.entities.OfflineAttempt
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface OfflineAttemptDao: BaseDao<OfflineAttempt>{

    @Insert
    suspend fun insert(offlineAttempt: OfflineAttempt): Long

    @Query("SELECT * FROM OfflineAttempt WHERE id = :attemptId")
    suspend fun get(attemptId: Long): OfflineAttempt

    @Query("SELECT * FROM OfflineAttempt WHERE examId = :examId")
    fun getOfflineAttemptsById(examId: Long): LiveData<List<OfflineAttempt>>

    @Query("SELECT * FROM OfflineAttempt WHERE examId = :examId")
    suspend fun getOfflineAttemptListById(examId: Long): List<OfflineAttempt>

    @Query("UPDATE OfflineAttempt SET remainingTime = :remainingTime WHERE id = :attemptId")
    suspend fun updateRemainingTime(attemptId: Long, remainingTime: String)

}