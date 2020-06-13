package `in`.testpress.course.network

import `in`.testpress.course.domain.DomainContentAttempt
import `in`.testpress.exam.network.NetworkAttempt
import `in`.testpress.models.greendao.CourseAttempt

data class NetworkContentAttempt(
    val id: Long,
    val type: String? = null,
    val objectId: Int? = null,
    val objectUrl: String? = null,
    val trophies: String? = null,
    var chapterContentId: Long? = null,
    var assessmentId: Long? = null,
    val userVideoId: Long? = null,
    val assessment: NetworkAttempt? = null,
    val video: NetworkVideoAttempt? = null,
    val chapterContent: NetworkContent? = null
)

fun createContentAttempt(contentAttempt: NetworkContentAttempt): CourseAttempt {
    val courseAttempt = CourseAttempt(
        contentAttempt.id,
        contentAttempt.type,
        contentAttempt.objectId,
        contentAttempt.objectUrl,
        contentAttempt.trophies,
        contentAttempt.chapterContentId,
        contentAttempt.assessmentId,
        contentAttempt.userVideoId
    )
    courseAttempt.chapterContent = contentAttempt.chapterContent?.asGreenDaoModel()
    return courseAttempt
}

fun createDomainContentAttempt(contentAttempt: NetworkContentAttempt): DomainContentAttempt {
    return DomainContentAttempt(
        contentAttempt.id,
        contentAttempt.type,
        contentAttempt.objectId,
        contentAttempt.objectUrl,
        contentAttempt.trophies,
        contentAttempt.chapterContentId,
        contentAttempt.assessmentId,
        contentAttempt.userVideoId
    )
}

fun NetworkContentAttempt.asGreenDaoModel(): CourseAttempt {
    return createContentAttempt(this)
}

fun List<NetworkContentAttempt>.asGreenDaoModel(): List<CourseAttempt> {
    return this.map {
        createContentAttempt(it)
    }
}

fun NetworkContentAttempt.asDomainContentAttempt(): DomainContentAttempt {
    return createDomainContentAttempt(this)
}

fun List<NetworkContentAttempt>.asDomainContentAttempt(): List<DomainContentAttempt> {
    return this.map {
        createDomainContentAttempt(it)
    }
}
