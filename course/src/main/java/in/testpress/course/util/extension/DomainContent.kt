package `in`.testpress.course.util.extension

import `in`.testpress.course.R
import `in`.testpress.course.domain.DomainContent

fun DomainContent.getContentImage(): Int {
    return when (this.contentType) {
        "Attachment" -> R.drawable.testpress_file_icon
        "Video" -> R.drawable.testpress_video_icon
        "Notes" -> R.drawable.testpress_notes_icon
        "VideoConference" -> R.drawable.testpress_live_conference_icon
        "Exam" -> R.drawable.testpress_exam_icon
        else -> R.drawable.testpress_exam_icon
    }
}