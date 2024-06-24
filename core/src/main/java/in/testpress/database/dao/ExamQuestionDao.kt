package `in`.testpress.database.dao

import `in`.testpress.database.BaseDao
import `in`.testpress.database.entities.ExamQuestion
import androidx.room.Dao
import androidx.room.Query

@Dao
interface ExamQuestionDao: BaseDao<ExamQuestion> {

    @Query("DELETE FROM ExamQuestion WHERE examId = :examId")
    suspend fun deleteByExamId(examId: Long)
}