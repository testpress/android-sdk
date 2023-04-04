package `in`.testpress.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ContentEntityLiteRemoteKey(
    @PrimaryKey val contentId: Long,
    val prevKey: Int?,
    val nextKey: Int?,
    val courseId: Long,
    val type: Int
)