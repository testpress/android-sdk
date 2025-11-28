package `in`.testpress.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "BookmarkEntity",
    indices = [Index(value = ["contentId", "bookmarkType"])]
)
data class BookmarkEntity(
    @PrimaryKey
    val id: Long,
    val contentId: Long,
    val bookmarkType: String,
    val pageNumber: Int?,
    val previewText: String?,
    val lastUpdated: Long = System.currentTimeMillis()
)

