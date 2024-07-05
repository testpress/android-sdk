package `in`.testpress.util

import `in`.testpress.database.entities.Answer
import `in`.testpress.database.entities.OfflineAttemptSection
import `in`.testpress.database.entities.OfflineUserUploadedFile
import `in`.testpress.database.entities.Question
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

    @TypeConverter
    @JvmStatic
    fun fromStringList(list: List<String>?): String? {
        return list?.joinToString(",")
    }

    @TypeConverter
    @JvmStatic
    fun toStringList(value: String?): List<String>? {
        return value?.split(",")
    }

    @TypeConverter
    @JvmStatic
    fun fromIntList(list: List<Int>?): String? {
        return list?.joinToString(",")
    }

    @TypeConverter
    @JvmStatic
    fun toIntList(value: String?): List<Int> {
        if (value == null) return listOf()
        if (value.isEmpty()) return listOf()
        return value.split(",").map { it.toInt() }
    }

    @TypeConverter
    @JvmStatic
    fun fromQuestionList(value: ArrayList<Question>?): String? {
        val gson = Gson()
        return gson.toJson(value)
    }

    @TypeConverter
    @JvmStatic
    fun toQuestionList(value: String?): ArrayList<Question>? {
        val listType = object : TypeToken<ArrayList<Question>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    @JvmStatic
    fun fromAnswerList(value: ArrayList<Answer>?): String? {
        val gson = Gson()
        return gson.toJson(value)
    }

    @TypeConverter
    @JvmStatic
    fun toAnswerList(value: String?): ArrayList<Answer>? {
        val listType = object : TypeToken<ArrayList<Answer>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    @JvmStatic
    fun fromOfflineUserUploadedFileList(value: ArrayList<OfflineUserUploadedFile>?): String? {
        val gson = Gson()
        return gson.toJson(value)
    }

    @TypeConverter
    @JvmStatic
    fun toOfflineUserUploadedFileList(value: String?): ArrayList<OfflineUserUploadedFile>? {
        val listType = object : TypeToken<ArrayList<OfflineUserUploadedFile>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    @JvmStatic
    fun fromQuestion(value: Question?): String? {
        val gson = Gson()
        return gson.toJson(value)
    }

    @TypeConverter
    @JvmStatic
    fun toQuestion(value: String?): Question? {
        val questionType = object : TypeToken<Question>() {}.type
        return Gson().fromJson(value, questionType)
    }

    @TypeConverter
    @JvmStatic
    fun fromOfflineAttemptSection(value: OfflineAttemptSection?): String? {
        val gson = Gson()
        return gson.toJson(value)
    }

    @TypeConverter
    @JvmStatic
    fun toOfflineAttemptSection(value: String?): OfflineAttemptSection? {
        val offlineAttemptSectionType = object : TypeToken<OfflineAttemptSection>() {}.type
        return Gson().fromJson(value, offlineAttemptSectionType)
    }
}
