package `in`.testpress.database.dao

import `in`.testpress.database.BaseDao
import `in`.testpress.database.entities.DiscussionPostEntity
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query

@Dao
interface DiscussionPostDao: BaseDao<DiscussionPostEntity> {
    @Query("SELECT * FROM DiscussionPostEntity ")
    fun getAll(): List<DiscussionPostEntity>

    @Query("SELECT * FROM DiscussionPostEntity ORDER BY upvotes DESC")
    fun getDiscussionsOrderedByUpvotes(): PagingSource<Int, DiscussionPostEntity>

    @Query("SELECT * FROM DiscussionPostEntity ORDER BY created")
    fun getOldestDiscussions(): PagingSource<Int, DiscussionPostEntity>

    @Query("SELECT * FROM DiscussionPostEntity ORDER BY viewsCount DESC")
    fun getDiscussionsOrderedByViews(): PagingSource<Int, DiscussionPostEntity>

    @Query("SELECT * FROM DiscussionPostEntity ORDER BY created DESC")
    fun getDiscussionsOrderedByLatest(): PagingSource<Int, DiscussionPostEntity>


    @Query("DELETE FROM DiscussionPostEntity")
    suspend fun deleteAll()
}
