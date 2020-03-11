package `in`.testpress.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

@Dao
interface HtmlContentDao: BaseDao<HtmlContentEntity> {
    @Query("SELECT * FROM htmlcontententity")
    fun getAll(): LiveData<List<HtmlContentEntity>>

    @Query("SELECT * from htmlcontententity where id = :id LIMIT 1")
    fun findById(id: Long): LiveData<HtmlContentEntity>
}