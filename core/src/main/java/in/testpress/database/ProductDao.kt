package `in`.testpress.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ProductDao: BaseDao<ProductCourseEntity> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertProduct(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCourse(course: CourseEntity)

    @Transaction
    @Query("SELECT * FROM productentity")
    fun getAll(): LiveData<List<ProductWithCourses>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(join: ProductWithCourses)

    @Delete
    fun deleteProduct(product: ProductEntity)

    @Delete
    fun deleteCourse(course: CourseEntity)
}
