package `in`.testpress.course.network

import `in`.testpress.database.ContentAttemptEntity
import `in`.testpress.models.greendao.CourseAttempt

data class NetworkContentAttempt(
    val id: Long,
    val type: String? = null,
    val objectId: Int? = null,
    val objectUrl: String? = null,
    val trophies: String? = null,
    val chapterContentId: Long? = null,
    val assessmentId: Long? = null,
    val userVideoId: Long? = null,
    val assessment: NetworkAttempt? = null
)

fun createContentAttempt(contentAttempt: NetworkContentAttempt): CourseAttempt {
    return CourseAttempt(
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

fun createContentAttemptEntity(contentAttempt: NetworkContentAttempt): ContentAttemptEntity {
    return ContentAttemptEntity(
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

fun NetworkContentAttempt.asDatabaseModel(): ContentAttemptEntity {
    return createContentAttemptEntity(this)
}

fun List<NetworkContentAttempt>.asGreenDaoModel(): List<CourseAttempt> {
    return this.map {
        createContentAttempt(it)
    }
}
