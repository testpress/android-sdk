package `in`.testpress.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class VideoQuestion(
    @PrimaryKey
    val videoContentId: Long,
    val questionsJson: String // JSON string of List<NetworkVideoQuestion>
)

