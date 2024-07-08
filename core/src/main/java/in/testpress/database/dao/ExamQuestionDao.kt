package `in`.testpress.database.dao

import `in`.testpress.database.BaseDao
import `in`.testpress.database.entities.ExamQuestion
import androidx.room.Dao
import androidx.room.Query

@Dao
interface ExamQuestionDao: BaseDao<ExamQuestion> {

    @Query("DELETE FROM ExamQuestion WHERE examId = :examId")
    suspend fun deleteByExamId(examId: Long)

    @Query("SELECT DISTINCT sectionId FROM ExamQuestion WHERE examId = :examId")
    suspend fun getUniqueSectionIdsByExamId(examId: Long): List<Long>

    @Query("SELECT * FROM ExamQuestion WHERE examId = :examId ORDER BY `order`")
    suspend fun getExamQuestionsByExamId(examId: Long): List<ExamQuestion>

    @Query("SELECT * FROM ExamQuestion WHERE examId = :examId AND sectionId = :sectionId ORDER BY `order`")
    suspend fun getExamQuestionsByExamIdAndSectionId(examId: Long, sectionId: Long): List<ExamQuestion>
}