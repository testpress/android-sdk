package `in`.testpress.core.database

import `in`.testpress.database.CoursesItem
import `in`.testpress.database.ProductsItem
import `in`.testpress.database.ProductsListEntity

abstract class ProductListDaoTestMixin: DbTestMixin() {

    fun productListFixture(): ProductsListEntity {
        return ProductsListEntity(id = 1, courses = coursesItemFixture(), products = productsItemFixture())
    }

    private fun coursesItemFixture(): List<CoursesItem>? {
        return listOf(CoursesItem(
                id = 1, image = "https://media.testpress.in/i/189189f925444d48abf627bbe17f73d6.png",
                examsCount = 10, created = "Jan 12", description = "Course",title = "Soft Skills",
                chaptersCount = 12, deviceAccessControl = "Both", createdBy = 12,enableDiscussions = true,
                url = "https://media.testpress.in",contentsCount = 1,contentsUrl = "https://testpress.in",
                chaptersUrl = "https://testpress.in",modified = "yes",videosCount = 2, externalContentLink = ""
        ))
    }

    private fun productsItemFixture(): List<ProductsItem>? {
        return listOf(ProductsItem(
                id = 1, endDate = "Jan 15", image = "https://media.testpress.in/i/189189f925444d48abf627bbe17f73d6.png",
                title = "Soft Skills",surl = "soft-skills", paymentLink = "",buyNowText = "buy",furl = "",
                descriptionHtml = "Course",currentPrice = "100",slug = "softskill",startDate = "Jan 4"
        ))
    }
}