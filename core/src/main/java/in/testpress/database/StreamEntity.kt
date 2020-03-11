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
            entity = VideoContentEntity::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("videoId"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)data class StreamEntity(
    @PrimaryKey var id: Long,
    var url: String? = null,
    var format: String = "",
    var videoId: Long
)

class StreamEntityTypeConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromString(data: String?): List<StreamEntity>? =
        gson.fromJson(data, object : TypeToken<List<StreamEntity>>() {}.type)

    @TypeConverter
    fun fromModel(objects: List<StreamEntity>?): String = gson.toJson(objects)
}
