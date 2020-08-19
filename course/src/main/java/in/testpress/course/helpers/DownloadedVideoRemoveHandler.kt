package `in`.testpress.course.helpers

import `in`.testpress.core.TestpressSDKDatabase
import `in`.testpress.course.domain.DomainOfflineVideo
import `in`.testpress.models.greendao.CourseDao
import android.content.Context

class DownloadedVideoRemoveHandler(val videos: List<DomainOfflineVideo>, val context: Context) {
    val courseDao: CourseDao = TestpressSDKDatabase.getCourseDao(context)
    private val videosByCourseId = hashMapOf<Long, List<DomainOfflineVideo>>()
    private val userCourseIds: List<Long>

    init {
        val courses = courseDao.queryBuilder()
            .where(CourseDao.Properties.IsMyCourse.eq(true)).list()
        userCourseIds = courses.map { it.id }
        mapVideosWithItsCourseIds()
    }

    private fun mapVideosWithItsCourseIds() {
        for (video in videos) {
            val videos = mutableListOf<DomainOfflineVideo>()
            if (videosByCourseId.containsKey(video.courseId)) {
                videos.addAll(videosByCourseId[video.courseId]!!)
            } else {
                videos.add(video)
            }
            videosByCourseId[video.courseId] = videos
        }
    }

    fun hasVideosToRemove(): Boolean {
        val videoCourseIds = videosByCourseId.keys
        return (videoCourseIds subtract userCourseIds).isNotEmpty()
    }

    fun remove() {
        val courseIdsToRemove = videosByCourseId.keys subtract userCourseIds
        val videosToRemove = getVideosToRemove(courseIdsToRemove)
        for (video in videosToRemove) {
            val downloadTask = DownloadTask(video.url!!, context)
            downloadTask.delete()
        }
    }

    private fun getVideosToRemove(courseIds: Set<Long>): MutableList<DomainOfflineVideo> {
        val videos = mutableListOf<DomainOfflineVideo>()
        for (courseId in courseIds) {
            videos.addAll(videosByCourseId[courseId]!!)
        }
        return videos
    }
}