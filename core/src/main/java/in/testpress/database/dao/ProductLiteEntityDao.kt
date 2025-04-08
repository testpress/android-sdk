package `in`.testpress.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import `in`.testpress.database.entities.ProductLiteEntity

@Dao
interface ProductLiteEntityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(obj: List<ProductLiteEntity>)

    @Query("SELECT * FROM productliteentity ORDER BY `order` ASC")
    suspend fun getAll(): List<ProductLiteEntity>

    @Query("SELECT * FROM productliteentity WHERE categoryId=:categoryId ORDER BY `order` ASC")
    suspend fun getByCategoryId(categoryId: Int): List<ProductLiteEntity>

    @Query("DELETE FROM productliteentity")
    suspend fun deleteAll()

    @Query("DELETE FROM productliteentity WHERE categoryId=:categoryId")
    suspend fun deleteByCategoryId(categoryId: Int)
}