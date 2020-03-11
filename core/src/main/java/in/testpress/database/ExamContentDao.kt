package `in`.testpress.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

@Dao
interface ExamContentDao: BaseDao<ExamContentEntity> {
    @Query("SELECT * FROM examcontententity")
    fun getAll(): LiveData<List<ExamContentEntity>>

    @Query("SELECT * from examcontententity where id = :id LIMIT 1")
    fun findById(id: Long): LiveData<ExamContentEntity>
}