package `in`.testpress.database.dao

import `in`.testpress.database.BaseDao
import `in`.testpress.database.entities.CommentEntity
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

@Dao
interface CommentDao: BaseDao<CommentEntity> {

    @Query("SELECT * FROM commententity")
    fun getAll(): LiveData<List<CommentEntity>>
}
