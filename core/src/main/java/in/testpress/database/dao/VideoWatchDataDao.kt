package `in`.testpress.database.dao

import `in`.testpress.database.BaseDao
import `in`.testpress.database.entities.DiscussionPostEntity
import `in`.testpress.database.entities.VideoWatchDataEntity
import androidx.room.Dao
import androidx.room.Query

@Dao
interface VideoWatchDataDao: BaseDao<VideoWatchDataEntity> {
    @Query("SELECT * FROM VideoWatchDataEntity ")
    fun getAll(): List<VideoWatchDataEntity>

    @Query("SELECT * FROM VideoWatchDataEntity WHERE chapterContentId=:contentId")
    fun get(contentId: Long): VideoWatchDataEntity?

    @Query("DELETE FROM VideoWatchDataEntity")
    fun deleteAll()
}