package `in`.testpress.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity
data class AttemptEntity(
    @PrimaryKey var id: Long,
    var url: String? = null,
    var date: String? = null,
    var totalQuestions: Int? = null,
    var score: String? = null,
    var rank: String? = null,
    var maxRank: String? = null,
    var reviewUrl: String? = null,
    var questionsUrl: String? = null,
    var correctCount: Int? = null,
    var incorrectCount: Int? = null,
    var lastStartedTime: String? = null,
    var remainingTime: String? = null,
    var timeTaken: String? = null,
    var state: String? = null,
    var percentile: String? = null,
    var speed: Int? = null,
    var accuracy: Int? = null,
    var percentage: String? = null
)


class AttemptEntityTypeConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromString(data: String?): AttemptEntity? =
        gson.fromJson(data, object : TypeToken<AttemptEntity>() {}.type)

    @TypeConverter
    fun fromModel(objects: AttemptEntity?): String = gson.toJson(objects)
}