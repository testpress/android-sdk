package `in`.testpress.database.dao

import `in`.testpress.database.BaseDao
import `in`.testpress.database.entities.VideoAnswer
import androidx.room.Dao
import androidx.room.Query

@Dao
interface VideoAnswerDao : BaseDao<VideoAnswer> {

    @Query("SELECT * FROM VideoAnswer WHERE videoContentId = :videoContentId AND videoQuestionId = :videoQuestionId ORDER BY id ASC")
    suspend fun getByVideoQuestionId(videoContentId: Long, videoQuestionId: Long): List<VideoAnswer>

    @Query("DELETE FROM VideoAnswer WHERE videoContentId = :videoContentId AND videoQuestionId = :videoQuestionId")
    suspend fun deleteByVideoQuestionId(videoContentId: Long, videoQuestionId: Long)

    @Query("DELETE FROM VideoAnswer WHERE videoContentId = :videoContentId")
    suspend fun deleteByVideoContentId(videoContentId: Long)
}

