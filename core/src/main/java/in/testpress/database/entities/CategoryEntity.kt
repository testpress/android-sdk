package `in`.testpress.database.entities

import `in`.testpress.models.DomainCategory
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


fun CategoryEntity.asDomainModel(): DomainCategory {
    return DomainCategory(id, name, color, slug)
}

fun List<CategoryEntity>.asDomainModels(): List<DomainCategory> {
    return this.map {
        it.asDomainModel()
    }
}