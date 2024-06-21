package `in`.testpress.database.dao

import `in`.testpress.database.BaseDao
import `in`.testpress.database.entities.CategoryEntity
import `in`.testpress.database.entities.Language
import `in`.testpress.database.entities.OfflineExam
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

@Dao
interface LanguageDao: BaseDao<Language>