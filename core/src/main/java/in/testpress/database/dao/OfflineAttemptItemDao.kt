package `in`.testpress.database.dao

import `in`.testpress.database.BaseDao
import `in`.testpress.database.entities.OfflineAttemptItem
import `in`.testpress.models.greendao.Attempt
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update

@Dao
interface OfflineAttemptItemDao : BaseDao<OfflineAttemptItem> {

    @Query("SELECT * FROM OfflineAttemptItem WHERE attemptId = :attemptId")
    suspend fun getOfflineAttemptItemByAttemptId(attemptId: Long): List<OfflineAttemptItem>

    @Update
    suspend fun update(offlineAttemptItem: OfflineAttemptItem)

    @Query("SELECT * FROM OfflineAttemptItem WHERE id = :id")
    suspend fun getAttemptItemById(id: Long): OfflineAttemptItem?
}