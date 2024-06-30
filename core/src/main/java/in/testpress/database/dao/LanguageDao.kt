package `in`.testpress.database.dao

import `in`.testpress.database.BaseDao
import `in`.testpress.database.entities.Language
import androidx.room.Dao
import androidx.room.Query

@Dao
interface LanguageDao: BaseDao<Language> {
    @Query("SELECT * FROM Language WHERE examId = :examId")
    suspend fun getLanguagesByExamId(examId: Long): List<Language>
}