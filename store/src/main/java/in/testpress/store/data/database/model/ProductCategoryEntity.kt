package `in`.testpress.store.data.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ProductCategoryEntity(
    @PrimaryKey val id: Int,
    val name: String? = null,
    val slug: String? = null
)
