package `in`.testpress.database

import `in`.testpress.util.Converters
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters


@Entity
data class ProductsListEntity(
        @PrimaryKey(autoGenerate = true)
        var id: Long? = null,
        @ColumnInfo(name = "courses")
        var courses: List<CoursesItem>? = null,
        @ColumnInfo(name = "products")
        var products: List<ProductsItem>? = null
)

data class CoursesItem(
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

data class ProductsItem(
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
