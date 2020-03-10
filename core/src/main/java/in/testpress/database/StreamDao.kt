package `in`.testpress.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

@Dao
interface StreamDao: BaseDao<StreamEntity> {
    @Query("SELECT * FROM streamentity")
    fun getAll(): LiveData<List<StreamEntity>>

    @Query("SELECT * from streamentity where id = :id LIMIT 1")
    fun findById(id: Long): LiveData<StreamEntity>
}