package `in`.testpress.database.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Relation

@Entity(primaryKeys = ["videoContentId", "id"])
data class VideoQuestion(
    val videoContentId: Long,
    val id: Long, // NetworkVideoQuestion.id
    val position: Int,
    val order: Int,
    val questionId: Long, // question.id
    val questionType: String,
    val questionHtml: String
)

data class VideoQuestionWithAnswers(
    @Embedded val videoQuestion: VideoQuestion,
    @Relation(
        parentColumn = "id",
        entityColumn = "videoQuestionId",
        entity = VideoAnswer::class
    )
    val answers: List<VideoAnswer>
)

