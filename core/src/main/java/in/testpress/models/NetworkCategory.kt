package `in`.testpress.models

import `in`.testpress.database.entities.CategoryEntity

data class NetworkCategory (
    val id: Long? = null,
    val name: String? = null,
    val color: String? = null,
    val slug: String? = null
)


fun NetworkCategory.asDatabaseModel(): CategoryEntity {
    return CategoryEntity(id, name, color, slug)
}

fun List<NetworkCategory>.asDatabaseModels(): List<CategoryEntity> {
    return this.map {
        it.asDatabaseModel()
    }
}