package `in`.testpress.database.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity
data class ProductEntity(
    @PrimaryKey
    val id: Int,
    val url: String?,
    val title: String?,
    val slug: String?,
    val images: List<Image>?,
    val startDate: String?,
    val endDate: String?,
    val description: String?,
    val paymentLink: String?,
    val descriptionHtml: String?,
    val shortDescription: String?,
    val contentsCount: Int,
    val chaptersCount: Int,
    val videosCount: Int,
    val attachmentsCount: Int,
    val examsCount: Int,
    val quizCount: Int,
    val htmlCount: Int,
    val videoConferenceCount: Int,
    val livestreamCount: Int,
    val price: String?,
    val strikeThroughPrice: String?,
    val institute: String?,
    val requiresShipping: Boolean?,
    val buyNowText: String?
)

@Entity
data class PriceEntity(
    @PrimaryKey
    val id: Int,
    val productId: Int,
    val name: String?,
    val price: String,
    val validity: String?,
    val startDate: String?,
    val endDate: String?
)

data class ProductWithPrices(
    @Embedded val product: ProductEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "productId"
    )
    val prices: List<PriceEntity>
)