package `in`.testpress.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class VideoWatchDataEntity (
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null,
    var lastWatchPosition: String? = null,
    var watchedTimeRanges: List<Array<String>> = arrayListOf(),
    var chapterContentId: Long,
    var videoContentId: Long
)