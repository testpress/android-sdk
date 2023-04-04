package `in`.testpress.database.dao

import `in`.testpress.database.entities.ContentEntityLiteRemoteKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ContentLiteRemoteKeyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(remoteKey: List<ContentEntityLiteRemoteKey>)

    @Query("SELECT * FROM contententityliteremotekey WHERE contentId = :contentId AND type = :type")
    suspend fun remoteKeyContentId(contentId: Long,type: Int): ContentEntityLiteRemoteKey?

    @Query("DELETE FROM contententityliteremotekey WHERE courseId =:courseId AND type = :type")
    suspend fun clearRemoteKeysByCourseIdAndType(courseId: Long,type: Int)
}