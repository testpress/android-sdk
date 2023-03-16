package `in`.testpress.database.entities

open class BaseContentEntity {
    var order: Int? = null
    var chapterId: Long? = null
    var freePreview: Boolean? = null
    var title: String? = null
    var courseId: Long? = null
    var examId: Long? = null
    var contentId: Long? = null
    var videoId: Long? = null
    var attachmentId: Long? = null
    var contentType: String? = null
    var icon: String? = null
    var start: String? = null
    var end: String? = null
    var treePath: String? = null
}