package `in`.testpress.database.dao

import `in`.testpress.database.BaseDao
import `in`.testpress.database.entities.Subject
import androidx.room.Dao
import androidx.room.Query

@Dao
interface SubjectDao : BaseDao<Subject> {

    @Query("SELECT * FROM Subject WHERE id = :subjectId")
    suspend fun getSubjectById(subjectId: Long): Subject?
}