package `in`.testpress.database.dao

import `in`.testpress.database.BaseDao
import `in`.testpress.database.entities.RemoteKeys
import androidx.room.Dao
import androidx.room.Query

@Dao
interface RemoteKeysDao: BaseDao<RemoteKeys> {
    @Query("SELECT * FROM REMOTE_KEYS WHERE repoId = :id")
    suspend fun remoteKeysDoggoId(id: Long): RemoteKeys?
}