package `in`.testpress.core.database

import `in`.testpress.database.CoursesItem
import `in`.testpress.database.ProductsItem
import `in`.testpress.database.ProductsListEntity
import `in`.testpress.util.getOrAwaitValue
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProductsListDaoTest: DbTestMixin() {

    private fun createCoursesItem(): List<CoursesItem>? {
        return listOf(CoursesItem(
                id = 1, image = "https://media.testpress.in/i/189189f925444d48abf627bbe17f73d6.png",
                examsCount = 10, created = "Jan 12", description = "Course",title = "Soft Skills",
                chaptersCount = 12, deviceAccessControl = "Both", createdBy = 12,enableDiscussions = true,
                url = "https://media.testpress.in",contentsCount = 1,contentsUrl = "https://testpress.in",
                chaptersUrl = "https://testpress.in",modified = "yes",videosCount = 2, externalContentLink = ""
        ))
    }

    private fun createProductsItem(): List<ProductsItem>? {
        return listOf(ProductsItem(
                id = 1, endDate = "Jan 15", image = "https://media.testpress.in/i/189189f925444d48abf627bbe17f73d6.png",
                title = "Soft Skills",surl = "soft-skills", paymentLink = "",buyNowText = "buy",furl = "",
                descriptionHtml = "Course",currentPrice = "100",slug = "softskill",startDate = "Jan 4"
        ))
    }

    private fun createProductList(): ProductsListEntity {
        return ProductsListEntity(id = 1, courses = createCoursesItem(), products = createProductsItem())
    }

    @Test
    fun readWrite() {
        val productsList = createProductList()
        db.productsListDao().insert(productsList)

        val fetchedProductsList = db.productsListDao().findById(1).getOrAwaitValue()
        MatcherAssert.assertThat(fetchedProductsList, CoreMatchers.equalTo(productsList))
    }

    @Test
    fun getProductsList() {
        val productsList = createProductList()
        db.productsListDao().insert(productsList)
        val fetchedProducts = db.productsListDao().getAll().getOrAwaitValue()
        Assert.assertEquals(productsList,fetchedProducts)
    }

}