package `in`.testpress.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

@Entity
data class RunningContentEntity(
    @PrimaryKey val id: Long,
    var order: Int? = null,
    var chapter_id: Long? = null,
    var free_preview: Boolean? = null,
    var title: String? = null,
    var courseId: Long? = null,
    var examId: Long? = null,
    var contentId: Long? = null,
    var videoId: Long? = null,
    var attachmentId: Long? = null,
    var contentType: String? = null,
    var icon: String? = null,
    var start: String? = null,
    var end: String? = null,
    var treePath: String? = null
) {
    fun getFormattedStartDateAndEndDate(): String {
        var startAndEnd = ""
        if (getFormattedStartDate() != ""){
            startAndEnd += "Start: ${getFormattedStartDate()}"  //Result should be like [Start: 01/01/23 10:00 am]
        }
        if (getFormattedEndDate() != ""){
            startAndEnd += " - End: ${getFormattedEndDate()}" //Result should be like [Start: 01/01/23 10:00 am - End: 01/01/23 12:00 pm]
        }
        return startAndEnd
    }

    private fun getFormattedStartDate(): String {
        var startDateAndTime = ""
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
        simpleDateFormat.timeZone = TimeZone.getTimeZone("UTC")
        if (start != null && start != "") {
            startDateAndTime = try {
                val date = start?.let { simpleDateFormat.parse(it) }
                val dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                date?.let { dateFormat.format(it) }!!
            } catch (e: Exception) {
                ""
            }
        }
        return startDateAndTime
    }

    private fun getFormattedEndDate(): String {
        var endDateAndTime = ""
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
        simpleDateFormat.timeZone = TimeZone.getTimeZone("UTC")
        if (end != null && end != "") {
            endDateAndTime = try {
                val date = end?.let { simpleDateFormat.parse(it) }
                val dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                date?.let { dateFormat.format(it) }!!
            } catch (e: Exception) {
                ""
            }
        }
        return endDateAndTime
    }
}