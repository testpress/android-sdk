package `in`.testpress.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query


@Dao
interface ContentDao: BaseDao<ContentEntity> {
    @Query("SELECT * FROM contententity")
    fun getAll(): LiveData<List<ContentWithRelations>>

    @Query("SELECT * from contententity where id = :id LIMIT 1")
    fun findById(id: Long): LiveData<ContentWithRelations>

    @Query("SELECT * from contententity where chapterId = :chapterId and active = 1")
    fun getChapterContents(chapterId: Long): LiveData<List<ContentWithRelations>>

    @Query("SELECT EXISTS(SELECT * from contententity where id = :id LIMIT 1)")
    fun isContentPresent(id: Long): Boolean
}