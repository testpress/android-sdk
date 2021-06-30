package `in`.testpress.database.dao

import `in`.testpress.database.BaseDao
import `in`.testpress.database.entities.LastLoadedPageData
import androidx.room.Dao
import androidx.room.Query

@Dao
interface LastLoadedPageDataDao: BaseDao<LastLoadedPageData> {
    @Query("SELECT * FROM LastLoadedPageData WHERE resourceType = :name")
    suspend fun findPageDataForResource(name: String): LastLoadedPageData?

    @Query("DELETE FROM LastLoadedPageData WHERE resourceType = :name")
    suspend fun deleteForResource(name: String)
}