package `in`.testpress.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import `in`.testpress.database.entities.ProductLiteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductLiteEntityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(obj: List<ProductLiteEntity>)

    @Query("SELECT * FROM productliteentity ORDER BY `order` ASC")
    suspend fun getAll(): List<ProductLiteEntity>

    @Query("SELECT * FROM productliteentity ORDER BY `order` ASC")
    fun getAllV2(): Flow<List<ProductLiteEntity>>

    @Query("DELETE FROM productliteentity")
    suspend fun deleteAll()
}