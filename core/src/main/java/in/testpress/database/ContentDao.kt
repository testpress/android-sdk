package `in`.testpress.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface ContentDao: BaseDao<ContentEntity> {
    @Query("SELECT * FROM contententity")
    fun getAll(): LiveData<List<ContentEntity>>

    @Query("SELECT * from contententity where id = :id LIMIT 1")
    fun findById(id: Long): LiveData<ContentEntity>

    @Query("SELECT * from contententity where chapterId = :chapterId and active = 1 ORDER BY position IS NULL, position ASC, `order` ASC, id ASC")
    fun getChapterContents(chapterId: Long): LiveData<List<ContentEntity>>

    @Query("SELECT position FROM contententity WHERE id = :id LIMIT 1")
    fun getPosition(id: Long): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(contents: List<ContentEntity>)
}
