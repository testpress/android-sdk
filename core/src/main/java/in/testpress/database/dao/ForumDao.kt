package `in`.testpress.database.dao

import `in`.testpress.database.BaseDao
import `in`.testpress.database.entities.ForumEntity
import androidx.room.Dao
import androidx.room.Query

@Dao
interface ForumDao: BaseDao<ForumEntity> {
    @Query("SELECT * FROM ForumEntity")
    fun getAll(): List<ForumEntity>
}
