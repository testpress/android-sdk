package `in`.testpress.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import `in`.testpress.database.entities.DomainProduct
import `in`.testpress.database.entities.PriceEntity
import `in`.testpress.database.entities.ProductEntity

@Dao
interface ProductEntityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(obj: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrices(obj: List<PriceEntity>)

    @Query("SELECT * FROM ProductEntity WHERE id=:productId")
    fun getProduct(productId: Int): DomainProduct?

}