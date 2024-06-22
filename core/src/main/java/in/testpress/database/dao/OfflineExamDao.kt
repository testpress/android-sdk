package `in`.testpress.database.dao

import `in`.testpress.database.BaseDao
import `in`.testpress.database.entities.OfflineExam
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

@Dao
interface OfflineExamDao: BaseDao<OfflineExam> {

    @Query("SELECT * FROM OfflineExam")
    fun getAll() : LiveData<List<OfflineExam>>

    @Query("DELETE FROM OfflineExam WHERE id = :examId")
    suspend fun deleteById(examId: Long)

    @Query("UPDATE OfflineExam SET isSyncRequired = :isSyncRequired WHERE contentId = :contentId")
    suspend fun updateSyncRequired(contentId: Long, isSyncRequired: Boolean)

    @Query("SELECT * FROM OfflineExam WHERE contentId = :contentId")
    suspend fun getById(contentId: Long): OfflineExam?
}