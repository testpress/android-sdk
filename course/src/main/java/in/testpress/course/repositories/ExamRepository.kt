package `in`.testpress.course.repositories

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.core.TestpressSDKDatabase
import `in`.testpress.course.enums.Status
import `in`.testpress.course.models.Resource
import `in`.testpress.course.util.plusAssign
import `in`.testpress.exam.network.TestpressExamApiClient
import `in`.testpress.models.TestpressApiResponse
import `in`.testpress.models.greendao.Content
import `in`.testpress.models.greendao.CourseAttempt
import `in`.testpress.models.greendao.CourseAttemptDao
import `in`.testpress.models.greendao.Language
import android.app.Application
import android.arch.lifecycle.MutableLiveData

class ExamRepository(val application: Application, val content: MutableLiveData<Content>) {
    private val courseAttempts: MutableLiveData<ArrayList<CourseAttempt>> = MutableLiveData()
    var resourceCourseAttempt: MutableLiveData<Resource<ArrayList<CourseAttempt>>> = MutableLiveData()
    var resourceLanguages: MutableLiveData<Resource<ArrayList<Language>>> = MutableLiveData()

    private val apiClient = TestpressExamApiClient(application)
    private val attemptDao = TestpressSDKDatabase.getAttemptDao(application)
    private var courseAttemptDao = TestpressSDKDatabase.getCourseAttemptDao(application)

    fun loadAttempts(attemptsUrl: String): MutableLiveData<Resource<ArrayList<CourseAttempt>>> {
        apiClient.getContentAttempts(attemptsUrl)
                .enqueue(object : TestpressCallback<TestpressApiResponse<CourseAttempt>>() {
                    override fun onSuccess(response: TestpressApiResponse<CourseAttempt>?) {
                        courseAttempts += response?.results ?: arrayListOf()

                        if (response?.next != null) {
                            loadAttempts(response.next)
                        } else {
                            clearContentAttemptsInDB()
                            saveCourseAttemptInDB(courseAttempts.value!!)
                            resourceCourseAttempt.value = Resource(Status.SUCCESS, courseAttempts.value, null)
                        }
                    }

                    override fun onException(exception: TestpressException?) {
                        resourceCourseAttempt.value = Resource(Status.ERROR, null, exception)
                    }

                })
        return resourceCourseAttempt
    }

    fun fetchLanguages(): MutableLiveData<Resource<ArrayList<Language>>> {
        apiClient.getLanguages(content.value!!.rawExam.slug)
                .enqueue(object : TestpressCallback<TestpressApiResponse<Language>>() {
                    override fun onSuccess(response: TestpressApiResponse<Language>?) {
                        storeLanguagesInDB(response?.results ?: arrayListOf())
                        val exam = content.value!!.rawExam
                        if (response?.next != null) {
                            fetchLanguages()
                        } else {
                            exam.saveLanguages(application)
                            resourceLanguages.value = Resource(Status.SUCCESS, ArrayList(exam.languages), null)
                        }
                    }

                    override fun onException(exception: TestpressException?) {
                        resourceLanguages.value = Resource(Status.ERROR, null, exception)
                    }

                })
        return resourceLanguages
    }

    fun storeLanguagesInDB(languagesFromApi: List<Language>) {
        val exam = content.value!!.rawExam
        val languages = exam.rawLanguages
        languages.addAll(languagesFromApi)
        val uniqueLanguages = HashMap<String, Language>()
        for (language in languages) {
            uniqueLanguages[language.code] = language
        }
        content.value!!.rawExam.languages = ArrayList(uniqueLanguages.values)
    }

    fun getContentAttemptsFromDB(): List<CourseAttempt> {
        return courseAttemptDao.queryBuilder()
                .where(CourseAttemptDao.Properties.ChapterContentId.eq(content.value!!.id))
                .list()
    }

    fun clearContentAttemptsInDB() {
        courseAttemptDao.queryBuilder()
                .where(CourseAttemptDao.Properties.ChapterContentId.eq(content.value!!.id))
                .buildDelete()
                .executeDeleteWithoutDetachingEntities()
    }

    fun saveCourseAttemptInDB(courseAttemptList: ArrayList<CourseAttempt>) {
        for (courseAttempt in courseAttemptList) {
            val attempt = courseAttempt.rawAssessment
            attemptDao.insertOrReplace(attempt)
            courseAttempt.assessmentId = attempt.id
            courseAttempt.chapterContentId = content.value!!.id
            courseAttemptDao.insertOrReplace(courseAttempt)
        }
        courseAttempts.value = ArrayList(courseAttemptDao.queryBuilder().where(CourseAttemptDao.Properties.ChapterContentId.eq(content.value!!.id)).list())
    }
}
