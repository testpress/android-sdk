package `in`.testpress.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

@Dao
interface OfflineVideoDao: BaseDao<OfflineVideo> {
    @Query("SELECT * FROM offlinevideo")
    fun getAll(): LiveData<List<OfflineVideo>>

    @Query("SELECT url FROM offlinevideo")
    fun getUrls(): LiveData<List<String>>

    @Query("SELECT * FROM offlinevideo WHERE url=:url")
    fun getByUrl(url: String): OfflineVideo?

    @Query("SELECT * FROM offlinevideo WHERE url=:url")
    fun get(url: String): LiveData<OfflineVideo?>

    @Query("SELECT * FROM offlinevideo")
    fun getAllSync(): List<OfflineVideo>

    @Query("DELETE FROM offlinevideo WHERE url=:url")
    fun deleteByUrl(url: String)
}