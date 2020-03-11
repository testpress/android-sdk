package `in`.testpress.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity
data class VideoContentEntity(
    @PrimaryKey var id: Long,
    var title: String? = null,
    var url: String? = null,
    var embedCode: String? = null,
    var duration: String? = null,
    var requiredWatchDuration: String? = null,
    var isDomainRestricted: Boolean? = null,
    var description: String? = null,
    var streams: List<StreamEntity>? = listOf()
)

class VideoContentEntityConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromString(data: String?): VideoContentEntity? =
        gson.fromJson(data, object : TypeToken<VideoContentEntity>() {}.type)

    @TypeConverter
    fun fromModel(objects: VideoContentEntity?): String = gson.toJson(objects)
}