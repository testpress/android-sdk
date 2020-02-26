package `in`.testpress.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query


@Dao
interface ContentDao: BaseDao<ContentEntity> {
    @Query("SELECT * FROM contententity")
    fun getAll(): LiveData<List<ContentEntity>>

    @Query("SELECT * from contententity where id = :id LIMIT 1")
    fun findById(id: Long): LiveData<ContentEntity>
}