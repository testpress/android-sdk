package `in`.testpress.database.dao

import androidx.room.*
import `in`.testpress.database.entities.HighlightEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HighlightDao {
    @Query("SELECT * FROM HighlightEntity WHERE contentId = :contentId ORDER BY pageNumber ASC")
    suspend fun getHighlightsByContent(contentId: Long): List<HighlightEntity>

    @Query("SELECT * FROM HighlightEntity WHERE contentId = :contentId ORDER BY pageNumber ASC")
    fun getHighlightsByContentFlow(contentId: Long): Flow<List<HighlightEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(highlight: HighlightEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(highlights: List<HighlightEntity>)

    @Query("DELETE FROM HighlightEntity WHERE contentId = :contentId")
    suspend fun deleteByContent(contentId: Long)

    @Query("DELETE FROM HighlightEntity WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM HighlightEntity WHERE id = :id")
    suspend fun getById(id: Long): HighlightEntity?
}



