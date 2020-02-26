package `in`.testpress.course.db

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*

@Dao
interface ContentDao: BaseDao<Content> {
    @Query("SELECT * FROM content")
    fun getAll(): LiveData<List<Content>>

    @Query("SELECT * from content where id = :id LIMIT 1")
    fun findById(id: Long): Content
}