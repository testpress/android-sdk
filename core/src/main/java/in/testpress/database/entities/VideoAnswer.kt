package `in`.testpress.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    primaryKeys = ["videoContentId", "videoQuestionId", "id"],
    foreignKeys = [
        ForeignKey(
            entity = VideoQuestion::class,
            parentColumns = ["videoContentId", "id"],
            childColumns = ["videoContentId", "videoQuestionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["videoContentId", "videoQuestionId"])]
)
data class VideoAnswer(
    val videoContentId: Long,
    val videoQuestionId: Long,
    val id: Long, // NetworkAnswer.id
    val isCorrect: Boolean,
    val textHtml: String
)

