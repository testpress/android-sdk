package `in`.testpress.database.dao

import `in`.testpress.database.BaseDao
import `in`.testpress.database.entities.RunningContentEntity
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

@Dao
interface RunningContentDao: BaseDao<RunningContentEntity> {
    @Query("SELECT * FROM runningcontententity")
    fun getAll(): LiveData<List<RunningContentEntity>>
}