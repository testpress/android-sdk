package `in`.testpress.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

@Dao
interface AttemptDao: BaseDao<AttemptEntity> {
    @Query("SELECT * FROM attemptentity")
    fun getAll(): LiveData<List<AttemptEntity>>

    @Query("SELECT * from attemptentity where id = :id LIMIT 1")
    fun findById(id: Long): LiveData<AttemptEntity>
}