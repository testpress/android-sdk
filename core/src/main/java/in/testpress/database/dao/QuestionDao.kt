package `in`.testpress.database.dao

import `in`.testpress.database.BaseDao
import `in`.testpress.database.entities.Question
import androidx.room.Dao

@Dao
interface QuestionDao: BaseDao<Question>