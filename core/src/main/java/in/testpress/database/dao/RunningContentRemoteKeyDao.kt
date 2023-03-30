package `in`.testpress.database.dao

import `in`.testpress.database.entities.RunningContentRemoteKeys
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RunningContentRemoteKeysDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(remoteKey: List<RunningContentRemoteKeys>)

    @Query("SELECT * FROM runningcontentremotekeys WHERE contentId = :contentId")
    suspend fun remoteKeysContentId(contentId: Long): RunningContentRemoteKeys?

    @Query("DELETE FROM runningcontentremotekeys WHERE courseId =:courseId")
    suspend fun clearRemoteKeysByCourseIdAndClassName(courseId: Long)
}