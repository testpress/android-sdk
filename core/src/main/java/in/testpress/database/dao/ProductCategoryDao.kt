package `in`.testpress.database.dao

import `in`.testpress.database.BaseDao
import `in`.testpress.database.entities.ProductCategoryEntity
import androidx.room.Dao
import androidx.room.Query

@Dao
interface ProductCategoryDao: BaseDao<ProductCategoryEntity>{

    @Query("DELETE FROM productcategoryentity")
    suspend fun deleteAll()

    @Query("SELECT * FROM productcategoryentity")
    suspend fun getAll() : MutableList<ProductCategoryEntity>
}