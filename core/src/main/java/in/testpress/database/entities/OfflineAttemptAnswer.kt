package `in`.testpress.database.entities

data class OfflineAttemptDetail(
    val chapterContentId: Long,
    val startedOn: String?,
    val completedOn: String?
)

data class OfflineAnswer(
    val examQuestionId: Long?,
    val duration: String?,
    val selectedAnswers: List<Int>?,
    val essayText: String?,
    val shortText: String?,
    val files: List<File>?,
    val gapFillResponses: List<GapFillResponse>?
)

data class File(
    val fileContent: String?,
    val fileExtension: String?
)

data class GapFillResponse(
    val answer: String?,
    val order: String?
)