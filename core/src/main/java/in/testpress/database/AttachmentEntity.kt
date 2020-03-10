package `in`.testpress.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity
data class AttachmentEntity(
    @PrimaryKey var id: Long,
    var title: String? = null,
    var description: String? = null,
    var attachmentUrl: String? = null
)

class AttachementTypeConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromString(data: String?): AttachmentEntity? =
        gson.fromJson(data, object : TypeToken<AttachmentEntity>() {}.type)

    @TypeConverter
    fun fromModel(objects: AttachmentEntity?): String = gson.toJson(objects)
}