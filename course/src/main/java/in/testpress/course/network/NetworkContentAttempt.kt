package `in`.testpress.course.network

import `in`.testpress.models.greendao.CourseAttempt

data class NetworkContentAttempt(
    val id: Long,
    val type: String? = null,
    val objectId: Int? = null,
    val objectUrl: String? = null,
    val trophies: String? = null,
    val chapterContentId: Long? = null,
    val assessmentId: Long? = null,
    val userVideoId: Long? = null
)

fun NetworkContentAttempt.asGreenDaoModel(): CourseAttempt {
    return CourseAttempt(
        this.id,
        this.type,
        this.objectId,
        this.objectUrl,
        this.trophies,
        this.chapterContentId,
        this.assessmentId,
        this.userVideoId
    )
}