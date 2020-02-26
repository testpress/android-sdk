package `in`.testpress.course.domain_models


data class DomainContent (
        val id: Long,
        val title: String = "",
        val description: String = "",
        val image: String = "",
        val order: Int,
        val url: String = "",
        val chapterId: Long? = null,
        val chapterSlug: String = "",
        val chapterUrl: String = "",
        val courseId: Long? = null,
        val freePreview: Boolean = false,
        val modified: String? = null,
        val contentType: String,
        val examUrl: String = "",
        val videoUrl: String = "",
        val attachmentUrl: String = "",
        val htmlUrl: String = "",
        val isLocked: Boolean,
        val isScheduled: Boolean,
        val attemptsCount: Int = 0,
        val bookmarkId: Long? = null,
        val videoWatchedPercentage: Int? = null,
        val active: Boolean,
        val examId: Long? = null,
        val attachmentId: Long? = null,
        val videoId: Long? = null,
        val htmlId: Long? = null,
        val start: String? = null
)