package `in`.testpress.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CategoryEntity (
    @PrimaryKey
    var id: Long? = null,
    val name: String? = null,
    val color: String? = null,
    val slug: String? = null
)