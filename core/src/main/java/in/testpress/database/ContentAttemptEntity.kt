package `in`.testpress.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity
data class ContentAttemptEntity(
    @PrimaryKey var id: Long,
    var type: String? = null,
    var objectId: Int? = null,
    var objectUrl: String? = null,
    var trophies: String? = null,
    var chapterContentId: Long? = null,
    var assessmentId: Long? = null,
    var userVideoId: Long? = null
)

class ContentAttemptEntityTypeConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromString(data: String?): ContentAttemptEntity? =
        gson.fromJson(data, object : TypeToken<ContentAttemptEntity>() {}.type)

    @TypeConverter
    fun fromModel(objects: ContentAttemptEntity?): String = gson.toJson(objects)
}