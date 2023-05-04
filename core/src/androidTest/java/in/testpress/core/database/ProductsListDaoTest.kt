package `in`.testpress.core.database

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProductsListDaoTest: ProductListDaoTestMixin() {

    @Test
    fun readShouldReturnInsertedData() {
        insertProductIntoDb()
        insertCourseIntoDb()
        db.productDao().insert(productCourseFixture())

        val fetchedProductCourse = db.productDao().getAll()
        Assert.assertEquals(productWithCoursesFixture().size,fetchedProductCourse.size)
        Assert.assertEquals("Jan 12",fetchedProductCourse[0].courses[0].created)
    }
}
