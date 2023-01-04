package `in`.testpress.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class RunningContentEntity(
    @PrimaryKey val id: Long,
    var order: Int? = null,
    var chapter_id: Long? = null,
    var free_preview: Boolean? = null,
    var title: String? = null,
    var courseId: Long? = null,
    var examId: Long? = null,
    var contentId: Long? = null,
    var videoId: Long? = null,
    var attachmentId: Long? = null,
    var contentType: String? = null,
    var icon: String? = null,
    var start: String? = null,
    var end: String? = null,
    var treePath: String? = null
)