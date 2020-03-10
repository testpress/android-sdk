package `in`.testpress.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = ExamContentEntity::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("examId"),
            onDelete = ForeignKey.NO_ACTION
        )
    ]
)data class LanguageEntity(
    @PrimaryKey var id: Long,
    var title: String? = null,
    var code: String? = null,
    var examId: Long? = null
)

class LanguageEntityTypeConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromString(data: String?): List<LanguageEntity>? =
        gson.fromJson(data, object : TypeToken<List<LanguageEntity>>() {}.type)

    @TypeConverter
    fun fromModel(objects: List<LanguageEntity>?): String = gson.toJson(objects)
}