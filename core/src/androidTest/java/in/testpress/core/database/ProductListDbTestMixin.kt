package `in`.testpress.core.database

import `in`.testpress.database.*

abstract class ProductListDaoTestMixin: DbTestMixin() {

    fun insertProductIntoDb() {
        db.productDao().insertProduct(productFixture())
    }

    fun insertCourseIntoDb() {
        db.productDao().insertCourse(courseFixture())
    }

    private fun productFixture(): ProductEntity {
        return ProductEntity(id = 1, endDate = "Jan 15", image = "https://media.testpress.in/i/189189f925444d48abf627bbe17f73d6.png",
                title = "Soft Skills",surl = "soft-skills", paymentLink = "",buyNowText = "buy",furl = "",
                descriptionHtml = "Course",currentPrice = "100",slug = "softskill",startDate = "Jan 4")
    }

    private fun courseFixture(): CourseEntity {
        return CourseEntity(id = 1, image = "https://media.testpress.in/i/189189f925444d48abf627bbe17f73d6.png",
                examsCount = 10, created = "Jan 12", description = "Course",title = "Soft Skills",
                chaptersCount = 12, deviceAccessControl = "Both", createdBy = 12,enableDiscussions = true,
                url = "https://media.testpress.in",contentsCount = 1,contentsUrl = "https://testpress.in",
                chaptersUrl = "https://testpress.in",modified = "yes",videosCount = 2, externalContentLink = "")
    }

    fun productCourseFixture(): ProductCourseEntity {
        return ProductCourseEntity(productId = 1,courseId = 1)
    }

    fun productWithCoursesFixture(): List<ProductWithCoursesAndPrices> {
        return listOf(ProductWithCoursesAndPrices(productFixture(),coursesListFixture(),pricesListFixture()))
    }

    private fun coursesListFixture(): List<CourseEntity> {
        return listOf(courseFixture())
    }

    private fun pricesListFixture(): List<PriceEntity> {
        return listOf(PriceEntity(1,price = "10000"))
    }
}
