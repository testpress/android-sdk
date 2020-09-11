package `in`.testpress.database

import androidx.room.*

@Entity
data class ProductEntity(
    @PrimaryKey
    var id: Int? = null,
    var endDate: String? = null,
    var image: String? = null,
    var surl: String? = null,
    var title: String? = null,
    var paymentLink: String? = null,
    var buyNowText: String? = null,
    var furl: String? = null,
    var descriptionHtml: String? = null,
    var currentPrice: String? = null,
    var slug: String? = null,
    var startDate: String? = null
)

@Entity
data class CourseEntity(
    @PrimaryKey
    var id: Int? = null,
    var image: String? = null,
    var examsCount: Int? = null,
    var created: String? = null,
    var description: String? = null,
    var title: String? = null,
    var chaptersCount: Int? = null,
    var deviceAccessControl: String? = null,
    var createdBy: Int? = null,
    var enableDiscussions: Boolean? = null,
    var url: String? = null,
    var contentsCount: Int? = null,
    var contentsUrl: String? = null,
    var chaptersUrl: String? = null,
    var modified: String? = null,
    var videosCount: Int? = null,
    var externalContentLink: String? = null
)

@Entity(
        primaryKeys = ["productId", "courseId"]
)
data class ProductCourseEntity(
    var courseId: Long,
    var productId: Long
)

data class ProductWithCourses(
    @Embedded
    var product: ProductEntity,
    @Relation(
            parentColumn = "id",
            entity = CourseEntity::class,
            entityColumn = "id",
            associateBy = Junction(
                    value = ProductCourseEntity::class,
                    parentColumn = "productId",
                    entityColumn = "courseId"
            )
    )
    var courses: List<CourseEntity>
)
