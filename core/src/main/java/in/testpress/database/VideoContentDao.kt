package `in`.testpress.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

@Dao
interface VideoContentDao: BaseDao<VideoContentEntity> {
    @Query("SELECT * FROM videocontententity")
    fun getAll(): LiveData<List<VideoContentEntity>>

    @Query("SELECT * from videocontententity where id = :id LIMIT 1")
    fun findById(id: Long): LiveData<VideoContentEntity>
}