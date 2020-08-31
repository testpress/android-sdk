package `in`.testpress.util

import `in`.testpress.database.CoursesItem
import `in`.testpress.database.ProductsItem
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object Converters {

    @TypeConverter
    @JvmStatic
    fun stringToCoursesItem(value: String?): List<CoursesItem?>? {
        val listType = object : TypeToken<ArrayList<CoursesItem?>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    @JvmStatic
    fun coursesItemToString(courses: List<CoursesItem?>?): String? {
        val gson = Gson()
        return gson.toJson(courses)
    }

    @TypeConverter
    @JvmStatic
    fun stringToProductsItem(value: String?): List<ProductsItem?>? {
        val listType = object : TypeToken<ArrayList<ProductsItem?>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    @JvmStatic
    fun productsItemToString(products: List<ProductsItem?>?): String? {
        val gson = Gson()
        return gson.toJson(products)
    }
}
