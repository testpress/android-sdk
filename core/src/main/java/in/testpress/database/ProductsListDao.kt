package `in`.testpress.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ProductsListDao: BaseDao<ProductsListEntity> {

    @Query("SELECT * FROM productslistentity")
    fun getAll(): LiveData<ProductsListEntity?>
}
