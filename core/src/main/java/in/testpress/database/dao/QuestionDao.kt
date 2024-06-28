package `in`.testpress.database.dao

import `in`.testpress.database.BaseDao
import `in`.testpress.database.entities.Question
import androidx.room.Dao
import androidx.room.Query

@Dao
interface QuestionDao : BaseDao<Question> {

    @Query("SELECT * FROM Question WHERE id = :id")
    suspend fun getQuestionById(id: Long): Question?
}