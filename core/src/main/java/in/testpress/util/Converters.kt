package `in`.testpress.util

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object Converters {

    @TypeConverter
    @JvmStatic
    fun stringToList(value: String?): List<Int?>? {
        val listType = object : TypeToken<ArrayList<Int?>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    @JvmStatic
    fun listToString(courses: List<Int?>?): String? {
        return Gson().toJson(courses)
    }
}
