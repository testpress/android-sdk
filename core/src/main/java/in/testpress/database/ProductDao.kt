package `in`.testpress.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ProductDao: BaseDao<ProductCourseEntity> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertProduct(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCourse(course: CourseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPrice(price: PriceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertProductPrice(price: ProductPriceEntity)

    @Transaction
    @Query("SELECT * FROM productentity")
    fun getAll(): List<ProductWithCoursesAndPrices>

    @Delete
    fun deleteProduct(product: ProductEntity)

    @Delete
    fun deleteCourse(course: CourseEntity)
}
