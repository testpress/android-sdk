package `in`.testpress.course.domain

import `in`.testpress.models.greendao.CourseAttempt

data class DomainContentAttempt(
    val id: Long,
    val type: String? = null,
    val objectId: Int? = null,
    val objectUrl: String? = null,
    val trophies: String? = null,
    val chapterContentId: Long? = null,
    val assessmentId: Long? = null,
    val userVideoId: Long? = null
)

fun createDomainContentAttempt(contentAttempt: CourseAttempt): DomainContentAttempt {
    return DomainContentAttempt(
        id = contentAttempt.id,
        type = contentAttempt.type,
        objectId = contentAttempt.objectId,
        objectUrl = contentAttempt.objectUrl,
        trophies = contentAttempt.trophies,
        chapterContentId = contentAttempt.chapterContentId,
        assessmentId = contentAttempt.assessmentId,
        userVideoId = contentAttempt.userVideoId
    )
}

fun CourseAttempt.asDomainContentAttempt(): DomainContentAttempt {
    return createDomainContentAttempt(this)
}

fun List<CourseAttempt>.asDomainContentAttempts(): List<DomainContentAttempt> {
    return this.map {
        createDomainContentAttempt(it)
    }
}