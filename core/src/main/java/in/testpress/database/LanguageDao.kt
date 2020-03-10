package `in`.testpress.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

@Dao
interface LanguageDao: BaseDao<LanguageEntity> {
    @Query("SELECT * FROM languageentity")
    fun getAll(): LiveData<List<LanguageEntity>>

    @Query("SELECT * from languageentity where id = :id LIMIT 1")
    fun findById(id: Long): LiveData<LanguageEntity>

    @Query("DELETE FROM languageentity WHERE examId = :examId")
    fun deleteForExam(examId: Long)
}