package `in`.testpress.database.dao

import `in`.testpress.database.BaseDao
import `in`.testpress.database.entities.VideoQuestion
import androidx.room.Dao
import androidx.room.Query

@Dao
interface VideoQuestionDao : BaseDao<VideoQuestion> {

    @Query("SELECT * FROM VideoQuestion WHERE videoContentId = :videoContentId")
    suspend fun getByVideoContentId(videoContentId: Long): VideoQuestion?

    @Query("DELETE FROM VideoQuestion WHERE videoContentId = :videoContentId")
    suspend fun deleteByVideoContentId(videoContentId: Long)
}

