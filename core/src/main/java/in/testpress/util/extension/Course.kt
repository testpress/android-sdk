package `in`.testpress.util.extension

import `in`.testpress.models.greendao.Course
import java.text.ParseException
import java.text.SimpleDateFormat

fun Course.getFormattedExpiryDate(): String {
    return if (expiryDate == null || expiryDate.isEmpty()) {
        ""
    } else try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd")
        val outputFormat = SimpleDateFormat("MMM dd, yyyy")
        val date = inputFormat.parse(expiryDate)
        "Valid till " + outputFormat.format(date)
    } catch (e: ParseException) {
        // Handle parsing errors and return empty string in case of failure
        println("Error parsing date: " + e.message)
        ""
    }
}