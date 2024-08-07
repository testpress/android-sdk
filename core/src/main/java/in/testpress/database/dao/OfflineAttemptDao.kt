package `in`.testpress.database.dao

import `in`.testpress.database.BaseDao
import `in`.testpress.database.entities.OfflineAttempt
import `in`.testpress.models.greendao.Attempt
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface OfflineAttemptDao: BaseDao<OfflineAttempt>{

    @Insert
    suspend fun insert(offlineAttempt: OfflineAttempt): Long

    @Query("SELECT * FROM OfflineAttempt WHERE id = :attemptId")
    suspend fun getById(attemptId: Long): OfflineAttempt

    @Query("UPDATE OfflineAttempt SET state = :state WHERE id = :attemptId")
    suspend fun updateAttemptState(attemptId: Long, state: String)

    @Query("UPDATE OfflineAttempt SET remainingTime = :remainingTime, lastStartedTime = :lastStartedTime WHERE id = :attemptId")
    suspend fun updateRemainingTimeAndLastStartedTime(attemptId: Long, remainingTime: String, lastStartedTime: String)

    @Query("SELECT * FROM OfflineAttempt WHERE examId = :examId AND state = :state")
    suspend fun getOfflineAttemptsByExamIdAndState(examId: Long, state: String): List<OfflineAttempt>

    @Query("SELECT * FROM OfflineAttempt WHERE state = :state")
    suspend fun getOfflineAttemptsByState(state: String): List<OfflineAttempt>

    @Query("DELETE FROM OfflineAttempt WHERE id = :attemptId")
    suspend fun deleteByAttemptId(attemptId: Long)

    @Query("SELECT id FROM OfflineAttempt WHERE examId = :examId")
    suspend fun getAttemptIdsByExamId(examId: Long): List<Long>

    @Query("SELECT * FROM OfflineAttempt WHERE state = :state")
    fun getOfflineAttemptsByCompleteState(state: String = Attempt.COMPLETED): LiveData<List<OfflineAttempt>>
}