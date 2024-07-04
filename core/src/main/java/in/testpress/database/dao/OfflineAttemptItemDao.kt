package `in`.testpress.database.dao

import `in`.testpress.database.BaseDao
import `in`.testpress.database.entities.OfflineAttemptItem
import androidx.room.Dao
import androidx.room.Query

@Dao
interface OfflineAttemptItemDao : BaseDao<OfflineAttemptItem> {
    @Query("SELECT * FROM OfflineAttemptItem WHERE id = :id")
    suspend fun getAttemptItemById(id: Long): OfflineAttemptItem?
}