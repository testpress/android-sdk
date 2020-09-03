package `in`.testpress.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

@Dao
interface ProductDetailDao: BaseDao<ProductDetailEntity> {
    @Query("SELECT * FROM productdetailentity")
    fun getAll(): LiveData<ProductDetailEntity?>

    @Query("SELECT * from productdetailentity where id = :id LIMIT 1")
    fun findById(id: Long): LiveData<ProductDetailEntity?>
}