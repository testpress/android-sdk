package `in`.testpress.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class OfflineAttemptItem(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 1,
    val question: Question,
    val selectedAnswers: List<Int> = arrayListOf(),
    val review: Boolean? = null,
    val savedAnswers: List<Int> = arrayListOf(),
    val order: Int,
    val shortText: String? = null,
    val currentShortText: String? = null,
    val attemptSection: OfflineAttemptSection? = null,
    val essayText: String? = null,
    val localEssayText: String? = null,
    val files: ArrayList<OfflineUserUploadedFile> = arrayListOf(),
    val unSyncedFiles: List<String> = arrayListOf()
)