package `in`.testpress.store.data.model

import `in`.testpress.models.greendao.Course
import `in`.testpress.util.StringList

data class NetworkCourses(
    val id: Long?,
    val url: String?,
    val title: String?,
    val expiryDate: String?,
    val description: String?,
    val image: String?,
    val createdBy: Int?,
    val created: String?,
    val modified: String?,
    val contentsUrl: String?,
    val chaptersUrl: String?,
    val slug: String?,
    val chaptersCount: Int?,
    val contentsCount: Int?,
    val examsCount: Int?,
    val videosCount: Int?,
    val attachmentsCount: Int?,
    val htmlContentsCount: Int?,
    val order: Int,
    val externalContentLink: String?,
    val externalLinkLabel: String?,
    val enableDiscussions: Boolean,
    val deviceAccessControl: String,
    val layout: String,
    val tags: List<String>,
    val enableProgressiveLock: Boolean,
    val maxAllowedViewsPerVideo: Int?,
    val maxAllowedWatchMinutes: Int?,
    val allowCustomTestGeneration: Boolean,
    val enableCertificate: Boolean?
)

fun NetworkCourses.asDomain(): Course {
    val course = Course()
    course.id = id
    course.url = url
    course.title = title
    course.description = description
    course.image = image
    course.modified = modified
    course.contentsUrl = contentsUrl
    course.chaptersUrl = chaptersUrl
    course.slug = slug
    course.chaptersCount = chaptersCount
    course.contentsCount = contentsCount
    course.order = order
    course.isMyCourse = false
    course.isProduct = true
    course.external_content_link = externalContentLink
    course.external_link_label = externalLinkLabel
    course.examsCount = examsCount
    course.videosCount = videosCount
    course.htmlContentsCount = htmlContentsCount
    course.attachmentsCount = attachmentsCount
    course.expiryDate = expiryDate
    course.tags = StringList(tags)
    course.allowCustomTestGeneration = allowCustomTestGeneration
    course.maxAllowedViewsPerVideo = maxAllowedViewsPerVideo
    return course
}

fun List<NetworkCourses>.asDomain(): List<Course> = this.map { it.asDomain() }
