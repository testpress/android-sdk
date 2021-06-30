package `in`.testpress.database.dao

import `in`.testpress.database.BaseDao
import `in`.testpress.database.entities.DiscussionPostEntity
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query

@Dao
interface DiscussionPostDao: BaseDao<DiscussionPostEntity> {
    @Query("SELECT * FROM DiscussionPostEntity ORDER BY id DESC")
    fun getAll(): List<DiscussionPostEntity>

    @Query("SELECT * FROM DiscussionPostEntity ORDER BY id DESC")
    fun getDiscussions(): PagingSource<Int, DiscussionPostEntity>

    @Query("DELETE FROM DiscussionPostEntity")
    suspend fun deleteAll()
}
