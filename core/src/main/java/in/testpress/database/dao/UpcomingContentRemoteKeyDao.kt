package `in`.testpress.database.dao

import `in`.testpress.database.entities.UpcomingContentRemoteKeys
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UpcomingContentRemoteKeyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(remoteKey: List<UpcomingContentRemoteKeys>)

    @Query("SELECT * FROM upcomingcontentremotekeys WHERE contentId = :contentId")
    suspend fun remoteKeysContentId(contentId: Long): UpcomingContentRemoteKeys?

    @Query("DELETE FROM upcomingcontentremotekeys WHERE courseId =:courseId")
    suspend fun clearRemoteKeysByCourseIdAndClassName(courseId: Long)
}