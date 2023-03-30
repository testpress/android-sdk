package `in`.testpress.exam.models

import android.content.Context
import android.text.format.DateUtils
import android.text.format.DateUtils.FORMAT_ABBREV_TIME
import androidx.core.text.HtmlCompat
import `in`.testpress.models.User
import `in`.testpress.util.FormatDate

data class ReportQuestionResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val per_page: Int?,
    val results: List<ReportQuestion>?,
    val is_report_resolved: Boolean?
) {
    inner class ReportQuestion(
        val id: Int?,
        val user: User?,
        val description: String?,
        val type: Int?,
        val type_display: String?,
        val exam_id: Int?,
        val created: String?
    ) {
        fun getFormattedDescription():String{
            return HtmlCompat.fromHtml(description?:"",HtmlCompat.FROM_HTML_MODE_COMPACT).toString().trimEnd()
        }

        fun getFormattedDate(context: Context): String? {
            created?.let {
                val dateInMillis: Long = FormatDate.getDate(
                    created,
                    "yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX", "UTC"
                ).time
                return DateUtils.formatDateTime(context,dateInMillis,FORMAT_ABBREV_TIME).toString()
            }
            return null
        }
    }


}