package `in`.testpress.database.dao

import `in`.testpress.database.BaseDao
import `in`.testpress.database.entities.CategoryEntity
import `in`.testpress.database.entities.OfflineExam
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query

@Dao
interface OfflineExamDao: BaseDao<OfflineExam> {

    @Query("SELECT * FROM OfflineExam ")
    fun getAll(): LiveData<List<OfflineExam>>

    @Query("SELECT * FROM OfflineExam WHERE id = :examId")
    fun get(examId: Long): OfflineExam?

    @Query("DELETE FROM OfflineExam WHERE id = :examId")
    fun deleteById(examId: Long)

}