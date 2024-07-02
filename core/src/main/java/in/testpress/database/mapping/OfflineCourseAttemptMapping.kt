package `in`.testpress.database.mapping

import `in`.testpress.database.entities.OfflineCourseAttempt
import `in`.testpress.models.greendao.Attempt
import `in`.testpress.models.greendao.CourseAttempt

fun OfflineCourseAttempt.createGreenDoaModel(attempt: Attempt): CourseAttempt {
    val courseAttempt = CourseAttempt()
    courseAttempt.id = this.id
    courseAttempt.type = null
    courseAttempt.objectId = null
    courseAttempt.objectUrl = null
    courseAttempt.trophies = null
    courseAttempt.chapterContentId = null
    courseAttempt.assessmentId = this.assessmentId
    courseAttempt.userVideoId = null
    courseAttempt.assessment = attempt
    courseAttempt.chapterContent = null
    courseAttempt.video = null
    return courseAttempt
}