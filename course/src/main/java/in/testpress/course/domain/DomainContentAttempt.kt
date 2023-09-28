package `in`.testpress.course.domain

import `in`.testpress.core.TestpressSDKDatabase
import `in`.testpress.database.TestpressDatabase
import `in`.testpress.exam.domain.DomainAttempt
import `in`.testpress.exam.domain.asDomainModel
import `in`.testpress.models.greendao.CourseAttempt
import `in`.testpress.models.greendao.CourseAttemptDao
import android.content.Context

data class DomainContentAttempt(
    val id: Long,
    val type: String? = null,
    val objectId: Int? = null,
    val objectUrl: String? = null,
    val trophies: String? = null,
    val chapterContentId: Long? = null,
    val assessmentId: Long? = null,
    val userVideoId: Long? = null,
    val assessment: DomainAttempt? = null
)

fun DomainContentAttempt.getEndAttemptUrl(context: Context):String? {
    return this.getGreenDaoContentAttempt(context)?.endAttemptUrl
}

fun createDomainContentAttempt(contentAttempt: CourseAttempt): DomainContentAttempt {
    return DomainContentAttempt(
        id = contentAttempt.id,
        type = contentAttempt.type,
        objectId = contentAttempt.objectId,
        objectUrl = contentAttempt.objectUrl,
        trophies = contentAttempt.trophies,
        chapterContentId = contentAttempt.chapterContentId,
        assessmentId = contentAttempt.assessmentId,
        userVideoId = contentAttempt.userVideoId,
        assessment = contentAttempt.assessment?.asDomainModel()
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

fun DomainContentAttempt.getGreenDaoContentAttempt(context: Context): CourseAttempt? {
    val courseAttemptDao = TestpressSDKDatabase.getCourseAttemptDao(context)
    val courseAttempts =  courseAttemptDao.queryBuilder().where(CourseAttemptDao.Properties.Id.eq(this.id)).list()
    if (courseAttempts.isNotEmpty()) {
        return courseAttempts[0]
    }

    return null
}