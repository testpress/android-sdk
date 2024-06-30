package `in`.testpress.database.dao

import `in`.testpress.database.BaseDao
import `in`.testpress.database.entities.OfflineExam
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update

@Dao
interface OfflineExamDao: BaseDao<OfflineExam> {

    @Query("SELECT * FROM OfflineExam")
    fun getAll() : LiveData<List<OfflineExam>>

    @Query("DELETE FROM OfflineExam WHERE id = :examId")
    suspend fun deleteById(examId: Long)

    @Query("UPDATE OfflineExam SET isSyncRequired = :isSyncRequired WHERE id = :examId")
    suspend fun updateSyncRequired(examId: Long, isSyncRequired: Boolean)

    @Query("SELECT * FROM OfflineExam WHERE id = :examId")
    suspend fun getById(examId: Long): OfflineExam?

    @Query("SELECT id FROM OfflineExam")
    suspend fun getAllIds(): List<Long>

    @Query("SELECT * FROM OfflineExam WHERE id = :examId")
    fun getOfflineExamById(examId: Long): LiveData<OfflineExam?>

    @Query("UPDATE OfflineExam SET downloadedQuestionCount = downloadedQuestionCount + :count WHERE id = :examId")
    suspend fun updateDownloadedQuestion(examId: Long, count: Long)
}