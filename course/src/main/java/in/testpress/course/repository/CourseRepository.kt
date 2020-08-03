package `in`.testpress.course.repository

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.core.TestpressSDKDatabase
import `in`.testpress.course.helpers.CourseLastSyncedDate
import `in`.testpress.course.network.CourseNetwork
import `in`.testpress.course.network.Resource
import `in`.testpress.models.TestpressApiResponse
import `in`.testpress.models.greendao.Course
import `in`.testpress.models.greendao.CourseDao
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class CourseRepository(val context: Context) {
    private val courseNetwork = CourseNetwork(context)
    val courseDao: CourseDao = TestpressSDKDatabase.getCourseDao(context)
    var page = 1

    var _resourceCourses: MutableLiveData<Resource<List<Course>?>> = MutableLiveData()
    val resourceCourses: LiveData<Resource<List<Course>?>>
        get() = _resourceCourses

    fun fetch(page: Int = 1) {
        val queryParams = hashMapOf<String, Any>("page" to page)
        courseNetwork.getCourses(queryParams)
            .enqueue(object : TestpressCallback<TestpressApiResponse<Course>>() {
                override fun onSuccess(response: TestpressApiResponse<Course>) {
                    if (page == 1) {
                        clearExistingCoursesForUser()
                    }
                    addNewCoursesForUser(response.results)
                    if (response.hasMore()) {
                        fetch(page + 1)
                    } else {
                        postToLiveData()
                        refreshLastSyncedDate()
                    }
                }

                override fun onException(exception: TestpressException?) {
                    _resourceCourses.postValue(Resource.error(exception!!, null))
                }
            })
    }

    private fun clearExistingCoursesForUser() {
        val coursesFromDB = courseDao.queryBuilder()
            .where(CourseDao.Properties.IsMyCourse.eq(true)).list()
        for (course in coursesFromDB) {
            course.isMyCourse = false
        }
        courseDao.insertOrReplaceInTx(coursesFromDB)
    }

    private fun addNewCoursesForUser(courses: List<Course>) {
        for (course in courses) {
            course.isMyCourse = true
        }
        courseDao.insertOrReplaceInTx(courses)
    }

    private fun postToLiveData() {
        _resourceCourses.postValue(Resource.success(getAll()))
    }

    private fun getAll(): MutableList<Course>? {
        return courseDao.queryBuilder()
            .where(CourseDao.Properties.IsMyCourse.eq(true)).list()
    }

    private fun refreshLastSyncedDate() {
        CourseLastSyncedDate(context).refresh()
    }
}