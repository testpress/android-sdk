package `in`.testpress.database.dao

import `in`.testpress.database.BaseDao
import `in`.testpress.database.entities.RemoteKeys
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RemoteKeysDao: BaseDao<RemoteKeys> {
    @Query("SELECT * FROM REMOTE_KEYS WHERE repoId = :id ORDER BY repoId DESC")
    suspend fun findKeyForDiscussion(id: Long): RemoteKeys?

    @Query("DELETE FROM REMOTE_KEYS")
    suspend fun deleteAll()
}