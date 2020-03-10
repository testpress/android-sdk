package `in`.testpress.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity
data class ExamContentEntity(
    @PrimaryKey var id: Long,
    var title: String? = "",
    var duration: String? = null,
    var description: String = "",
    var url: String? = null,
    var startDate: String? = "",
    var endDate: String? = null,
    var numberOfQuestions: Int? = 0,
    var negativeMarks: String? = null,
    var markPerQuestion: String? = null,
    var templateType: Int? = null,
    var allowRetake: Boolean? = null,
    var maxRetakes: Int? = null,
    var enableRanks: Boolean? = false,
    var rankPublishingDate: String? = null,
    var attemptsUrl: String? = null,
    var attemptsCount: Int? = null,
    var pausedAttemptsCount: Int? = 0,
    var allowPdf: Boolean? = null,
    var allowQuestionsPdf: Boolean? = null,
    var created: String? = "",
    var slug: String? = "",
    var variableMarkPerQuestion: Boolean? = false,
    var showAnswers: Boolean? = null,
    var commentsCount: Int? = 0,
    var allowPreemptiveSectionEnding: Boolean? = false,
    var immediateFeedback: Boolean? = false,
    var deviceAccessControl: String? = "",
    var instructions: String? = null,
    var passPercentage: Int? = 50,
    var showPercentile: Boolean? = true,
    var showScore: Boolean? = true,
    var studentsAttemptedCount: Int? = 0,
    var customRedirectUrl: String? = "",
    var overallStudentsAttemptedCount: Int? = 0,
    var restrictRetakeDuration: Int? = null,
    var languages: List<LanguageEntity>? = arrayListOf()
)

class ExamContentEntityConverter {
    private var gson = Gson()

    @TypeConverter
    fun fromString(data: String?): ExamContentEntity? =
        gson.fromJson(data, object : TypeToken<ExamContentEntity>() {}.type)

    @TypeConverter
    fun fromModel(objects: ExamContentEntity?): String = gson.toJson(objects)
}