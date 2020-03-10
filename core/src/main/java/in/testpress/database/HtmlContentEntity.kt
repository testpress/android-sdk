package `in`.testpress.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity
data class HtmlContentEntity(
    @PrimaryKey val id: Long,
    var title: String? = null,
    var textHtml: String? = null,
    var sourceUrl: String? = null,
    var readTime: String? = null
)

class HtmlContentEntityConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromString(data: String?): HtmlContentEntity? =
        gson.fromJson(data, object : TypeToken<HtmlContentEntity>() {}.type)

    @TypeConverter
    fun fromModel(objects: HtmlContentEntity?): String = gson.toJson(objects)
}