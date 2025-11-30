package `in`.testpress.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "HighlightEntity",
    indices = [Index(value = ["contentId"])]
)
data class HighlightEntity(
    @PrimaryKey
    val id: Long,
    val contentId: Long,
    val pageNumber: Int?,
    val selectedText: String?,
    val notes: String?,
    val color: String?,
    val position: List<Double>?,
    val created: String?,
    val modified: String?,
    val lastUpdated: Long = System.currentTimeMillis()
)

