package `in`.testpress.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity (tableName = "RunningContentEntity")
data class ContentEntityLite(@PrimaryKey val id: Long, var type: Int = CourseContentType.RUNNING_CONTENT.ordinal) : BaseContentEntity()

enum class CourseContentType {
    RUNNING_CONTENT,
    UPCOMING_CONTENT
}