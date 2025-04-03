package `in`.testpress.store.data.model

import `in`.testpress.database.entities.Image


data class NetworkImage(
    val original: String?,
    val medium: String?,
    val small: String?
)

fun NetworkImage.asDomain(): Image {
    return Image(this.original, this.medium, this.small)
}

fun List<NetworkImage>.asDomain(): List<Image> = this.map { it.asDomain() }