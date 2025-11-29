package `in`.testpress.database.dao

import androidx.room.*
import `in`.testpress.database.entities.BookmarkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM BookmarkEntity WHERE contentId = :contentId AND bookmarkType = :bookmarkType ORDER BY pageNumber ASC")
    suspend fun getBookmarksByContent(contentId: Long, bookmarkType: String): List<BookmarkEntity>

    @Query("SELECT * FROM BookmarkEntity WHERE contentId = :contentId AND bookmarkType = :bookmarkType ORDER BY pageNumber ASC")
    fun getBookmarksByContentFlow(contentId: Long, bookmarkType: String): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bookmark: BookmarkEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(bookmarks: List<BookmarkEntity>)

    @Query("DELETE FROM BookmarkEntity WHERE contentId = :contentId AND bookmarkType = :bookmarkType")
    suspend fun deleteByContent(contentId: Long, bookmarkType: String)

    @Query("DELETE FROM BookmarkEntity WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM BookmarkEntity WHERE id = :id")
    suspend fun getById(id: Long): BookmarkEntity?
}

