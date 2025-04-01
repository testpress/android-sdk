package `in`.testpress.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ProductLiteEntity(
    @PrimaryKey
    val id: Int,
    val title: String,
    val slug: String,
    val images: List<Image>? = null,
    val categoryId: Int?,
    val contentsCount: Int = 0,
    val chaptersCount: Int = 0,
    val order: Int,
    val price: String
)