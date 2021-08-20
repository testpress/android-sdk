package `in`.testpress.util

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object Converters {
    @TypeConverter
    @JvmStatic
    fun stringToList(value: String?): List<Array<String>>? {
        val listType = object : TypeToken<ArrayList<ArrayList<String>?>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    @JvmStatic
    fun listToString(stringArray: List<Array<String>>?): String? {
        return Gson().toJson(stringArray)
    }
}
