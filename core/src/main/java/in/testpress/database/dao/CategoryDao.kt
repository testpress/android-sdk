package `in`.testpress.database.dao

import `in`.testpress.database.BaseDao
import `in`.testpress.database.entities.CategoryEntity
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

@Dao
interface CategoryDao: BaseDao<CategoryEntity> {

    @Query("SELECT * FROM CATEGORYENTITY ")
    fun getAll(): LiveData<List<CategoryEntity>>

    @Query("DELETE FROM CATEGORYENTITY")
    suspend fun deleteAll()
}