// package `in`.testpress.core.database
//
// import `in`.testpress.util.getOrAwaitValue
// import androidx.test.ext.junit.runners.AndroidJUnit4
// import org.junit.Assert
// import org.junit.Test
// import org.junit.runner.RunWith
//
// @RunWith(AndroidJUnit4::class)
// class ProductsListDaoTest: ProductListDaoTestMixin() {
//
//     @Test
//     fun readShouldReturnInsertedData() {
//         insertProductIntoDb()
//         insertCourseIntoDb()
//         db.productDao().insert(productCourseFixture())
//
//         val fetchedProductCourse = db.productDao().getAll().getOrAwaitValue()
//         Assert.assertEquals(productWithCoursesFixture(),fetchedProductCourse)
//     }
// }
