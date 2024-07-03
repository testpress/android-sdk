package `in`.testpress.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class OfflineAttemptItem(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var question: Question,
    var selectedAnswers: List<Int> = arrayListOf(),
    var review: Boolean? = null,
    var savedAnswers: List<Int> = arrayListOf(),
    var order: Int,
    var shortText: String? = null,
    var currentShortText: String? = null,
    var attemptSection: OfflineAttemptSection? = null,
    var essayText: String? = null,
    var localEssayText: String? = null,
    var files: ArrayList<OfflineUserUploadedFile> = arrayListOf(),
    var unSyncedFiles: List<String> = arrayListOf()
)