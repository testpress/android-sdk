package `in`.testpress.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

@Dao
interface ContentAttemptDao: BaseDao<ContentAttemptEntity> {
    @Query("SELECT * FROM contentattemptentity")
    fun getAll(): LiveData<List<ContentAttemptEntity>>

    @Query("SELECT * from contentattemptentity where id = :id LIMIT 1")
    fun findById(id: Long): LiveData<ContentAttemptEntity>
}