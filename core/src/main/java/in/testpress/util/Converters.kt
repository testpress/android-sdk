package `in`.testpress.util

import `in`.testpress.database.*
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

    @TypeConverter
    @JvmStatic
    fun courseToString(course: List<Course?>?): String? {
        val gson = Gson()
        return gson.toJson(course)
    }

    @TypeConverter
    @JvmStatic
    fun stringToCourse(value: String?): List<Course?>? {
        val listType = object : TypeToken<ArrayList<Course?>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    @JvmStatic
    fun pricesItemToString(prices: List<PricesItem?>?): String? {
        val gson = Gson()
        return gson.toJson(prices)
    }

    @TypeConverter
    @JvmStatic
    fun stringToPricesItem(value: String?): List<PricesItem?>? {
        val listType = object : TypeToken<ArrayList<PricesItem?>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    @JvmStatic
    fun orderItemsToString(ordersItem: List<OrderItems?>?): String? {
        val gson = Gson()
        return gson.toJson(ordersItem)
    }

    @TypeConverter
    @JvmStatic
    fun stringToOrderItems(value: String?): List<OrderItems?>? {
        val listType = object : TypeToken<ArrayList<OrderItems?>?>() {}.type
        return Gson().fromJson(value, listType)
    }
}