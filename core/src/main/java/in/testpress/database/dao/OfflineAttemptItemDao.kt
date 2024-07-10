package `in`.testpress.database.dao

import `in`.testpress.database.BaseDao
import `in`.testpress.database.entities.OfflineAttemptItem
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update

@Dao
interface OfflineAttemptItemDao : BaseDao<OfflineAttemptItem> {
    @Query("SELECT * FROM OfflineAttemptItem WHERE id = :id")
    suspend fun getAttemptItemById(id: Long): OfflineAttemptItem?

    @Update
    suspend fun update(offlineAttemptItem: OfflineAttemptItem)

    @Query("SELECT * FROM OfflineAttemptItem WHERE attemptId = :attemptId")
    suspend fun getOfflineAttemptItemByAttemptId(attemptId: Long): List<OfflineAttemptItem>

    @Query("SELECT COUNT(*) FROM OfflineAttemptItem WHERE attemptId = :attemptId")
    suspend fun getOfflineAttemptItemCountByAttemptId(attemptId: Long): Int

    @Query("DELETE FROM OfflineAttemptItem WHERE attemptId = :attemptId")
    suspend fun deleteByAttemptId(attemptId: Long)
}