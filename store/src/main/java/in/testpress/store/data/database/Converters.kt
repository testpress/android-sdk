package `in`.testpress.store.data.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import `in`.testpress.store.data.database.model.Image

object Converters {

    @TypeConverter
    @JvmStatic
    fun fromImageList(value: List<Image>?): String? {
        val gson = Gson()
        return gson.toJson(value)
    }

    @TypeConverter
    @JvmStatic
    fun toImageList(value: String?): List<Image>? {
        val listType = object : TypeToken<List<Image>>() {}.type
        return Gson().fromJson(value, listType)
    }
}
