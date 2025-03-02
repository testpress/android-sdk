package `in`.testpress.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ProductDao: BaseDao<ProductCourseEntity> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: CourseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrice(price: PriceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProductPrice(price: ProductPriceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllProducts(products: List<ProductEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCourses(courses: List<CourseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPrices(prices: List<PriceEntity>)

    @Transaction
    @Query("SELECT * FROM productentity")
    fun getAll(): LiveData<List<ProductWithCoursesAndPrices>>

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Delete
    suspend fun deleteCourse(course: CourseEntity)

    @Query("DELETE FROM productentity")
    suspend fun deleteAllProducts()

    @Query("DELETE FROM courseentity")
    suspend fun deleteAllCourses()

    @Query("DELETE FROM priceentity")
    suspend fun deleteAllPrices()
}
