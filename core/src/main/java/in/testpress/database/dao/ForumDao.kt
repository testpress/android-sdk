package `in`.testpress.database.dao

import `in`.testpress.database.BaseDao
import `in`.testpress.database.entities.ForumEntity
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query

@Dao
interface ForumDao: BaseDao<ForumEntity> {
    @Query("SELECT * FROM ForumEntity ORDER BY id DESC")
    fun getAll(): List<ForumEntity>

    @Query("SELECT * FROM ForumEntity ORDER BY id DESC")
    fun getDiscussions(): PagingSource<Int, ForumEntity>

    @Query("DELETE FROM FORUMENTITY")
    suspend fun deleteAll()
}
