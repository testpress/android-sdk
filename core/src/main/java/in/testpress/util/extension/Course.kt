package `in`.testpress.util.extension

import android.util.Log
import `in`.testpress.models.greendao.Course
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

fun Course.getFormattedExpiryDate(): String {
    if (expiryDate.isNullOrEmpty()) return ""

    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val date = inputFormat.parse(expiryDate)
        "Valid till " + outputFormat.format(date)
    } catch (e: ParseException) {
        // Handle parsing errors and return empty string in case of failure
        Log.e("CourseUtil", "Error parsing date: $expiryDate", e)
        ""
    }
}