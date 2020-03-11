package `in`.testpress.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

@Dao
interface AttachmentContentDao: BaseDao<AttachmentEntity> {
    @Query("SELECT * FROM attachmententity")
    fun getAll(): LiveData<List<AttachmentEntity>>

    @Query("SELECT * from attachmententity where id = :id LIMIT 1")
    fun findById(id: Long): LiveData<AttachmentEntity>
}