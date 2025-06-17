package `in`.testpress.course.domain

import `in`.testpress.models.greendao.VideoConference
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

data class DomainVideoConferenceContent(
    val id: Long? = null,
    val conferenceId: String? = null,
    val duration: Int? = null,
    val joinUrl: String? = null,
    val provider: String? = null,
    val start: String? = null,
    val title: String? = null,
    val accessToken: String? = null,
    val password: String? = null,
    val showRecordedVideo: Boolean?,
    val state: String? = null
) {
    private fun formattedDate(inputString: String): String {
        var date: Date? = null
        val simpleDateFormat =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        simpleDateFormat.timeZone = TimeZone.getTimeZone("UTC")
        try {
            if (inputString.isNotEmpty()) {
                date = simpleDateFormat.parse(inputString)
                val dateformat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                return dateformat.format(date)
            }
        } catch (e: ParseException) {
        }
        return "forever"
    }

    fun formattedStartDate() = formattedDate(start ?: "")

    fun isEnded() = state?.equals("ended", ignoreCase = true) == true
}

fun createDomainVideoConferenceContent(video: VideoConference): DomainVideoConferenceContent {
    return DomainVideoConferenceContent(
        id = video.id,
        title = video.title,
        conferenceId = video.conferenceId,
        joinUrl = video.joinUrl,
        duration = video.duration,
        provider = video.provider,
        start = video.start,
        password = video.password,
        accessToken = video.accessToken,
        showRecordedVideo = video.showRecordedVideo,
        state = video.state
    )
}

fun VideoConference.asDomainContent(): DomainVideoConferenceContent {
    return createDomainVideoConferenceContent(this)
}