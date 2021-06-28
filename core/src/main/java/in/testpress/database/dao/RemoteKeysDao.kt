package `in`.testpress.database.dao

import `in`.testpress.database.BaseDao
import `in`.testpress.database.entities.RemoteKeys
import androidx.room.Dao
import androidx.room.Query

@Dao
interface RemoteKeysDao: BaseDao<RemoteKeys> {
    @Query("SELECT * FROM REMOTE_KEYS WHERE resourceType = :name")
    suspend fun findPageDataForResource(name: String): RemoteKeys?

    @Query("DELETE FROM REMOTE_KEYS")
    suspend fun deleteAll()
}