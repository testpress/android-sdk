package `in`.testpress.database

import androidx.lifecycle.LiveData
import androidx.room.*


@Dao
interface ProductsListDao: BaseDao<ProductsListEntity> {
    @Transaction
    @Query("SELECT * FROM productslistentity")
    fun getAll(): LiveData<ProductsListEntity?>

    @Query("SELECT * from productslistentity where id = :id LIMIT 1")
    fun findById(id: Long): LiveData<ProductsListEntity?>
}