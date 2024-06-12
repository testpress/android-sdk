package `in`.testpress.database.entities

import androidx.room.Entity

@Entity
data class OfflineUserUploadedFile(
    val id: Long? = null,
    val url: String? = null,
    val path: String? = null
)
