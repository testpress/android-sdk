package `in`.testpress.course.repository

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.course.domain.DomainContentAttempt
import `in`.testpress.course.domain.DomainLanguage
import `in`.testpress.course.domain.asDomainContentAttempts
import `in`.testpress.course.domain.toDomainLanguages
import `in`.testpress.course.network.CourseNetwork
import `in`.testpress.course.network.NetworkContentAttempt
import `in`.testpress.course.network.Resource
import `in`.testpress.course.network.asDatabaseModel
import `in`.testpress.course.network.asGreenDaoModel
import `in`.testpress.database.ContentAttemptDao
import `in`.testpress.exam.network.ExamNetwork
import `in`.testpress.exam.network.NetworkLanguage
import `in`.testpress.exam.network.asDatabaseModels
import `in`.testpress.exam.network.asGreenDaoModels
import `in`.testpress.models.TestpressApiResponse
import `in`.testpress.models.greendao.AttemptDao
import `in`.testpress.models.greendao.CourseAttempt
import `in`.testpress.models.greendao.CourseAttemptDao
import `in`.testpress.models.greendao.LanguageDao
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExamContentRepository(
    val courseNetwork: CourseNetwork,
    val examNetwork: ExamNetwork,
    val contentAttemptDao: CourseAttemptDao,
    val attemptDao: AttemptDao,
    val languageDao: LanguageDao,
    val roomContentAttemptDao: ContentAttemptDao,
    val roomAttemptDao: `in`.testpress.database.AttemptDao,
    val roomLanguageDao: `in`.testpress.database.LanguageDao
) {

    private var courseAttempts = arrayListOf<NetworkContentAttempt>()
    var resourceCourseAttempt: MutableLiveData<Resource<ArrayList<NetworkContentAttempt>>> =
        MutableLiveData()
    private var isLanguagesBeingFetched = false
    private var isAttemptsBeingFetched = false
    private var languages = arrayListOf<NetworkLanguage>()
    var resourceLanguages: MutableLiveData<Resource<List<DomainLanguage>>> = MutableLiveData()

    private fun _loadAttempts(
        url: String,
        contentId: Long
    ) {
        courseNetwork.getContentAttempts(url)
            .enqueue(object : TestpressCallback<TestpressApiResponse<NetworkContentAttempt>>() {
                override fun onSuccess(response: TestpressApiResponse<NetworkContentAttempt>?) {
                    courseAttempts.addAll(response?.results!!)

                    if (response.next != null) {
                        _loadAttempts(response.next, contentId)
                    } else {
                        isAttemptsBeingFetched = false
                        clearContentAttemptsInDB(contentId)
                        saveCourseAttemptInDB(courseAttempts, contentId)
                        resourceCourseAttempt.value = Resource.success(courseAttempts)
                    }
                }

                override fun onException(exception: TestpressException) {
                    isAttemptsBeingFetched = false
                    resourceCourseAttempt.value = Resource.error(exception, null)
                }
            })
    }

    fun loadAttempts(
        url: String,
        contentId: Long
    ): LiveData<Resource<ArrayList<NetworkContentAttempt>>> {
        if (!isAttemptsBeingFetched) {
            isAttemptsBeingFetched = true
            _loadAttempts(url, contentId)
        }
        return resourceCourseAttempt
    }

    fun clearContentAttemptsInDB(contentId: Long) {
        contentAttemptDao.queryBuilder()
            .where(CourseAttemptDao.Properties.ChapterContentId.eq(contentId))
            .buildDelete()
            .executeDeleteWithoutDetachingEntities()
    }

    fun saveCourseAttemptInDB(
        courseAttemptList: ArrayList<NetworkContentAttempt>,
        contentId: Long
    ) {
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                for (networkContentAttempt in courseAttemptList) {
                    val contentAttempt = networkContentAttempt.asGreenDaoModel()
                    val attempt = networkContentAttempt.assessment!!
                    attemptDao.insertOrReplace(attempt.asGreenDaoModel())
                    contentAttempt.assessmentId = attempt.id
                    contentAttempt.chapterContentId = contentId
                    contentAttemptDao.insertOrReplace(contentAttempt)

                    val roomContentAttempt = networkContentAttempt.asDatabaseModel()
                    val roomAttempt = networkContentAttempt.assessment.asDatabaseModel()
                    roomAttemptDao.insert(roomAttempt)
                    roomContentAttempt.assessmentId = roomAttempt.id
                    roomContentAttempt.chapterContentId = contentId
                    roomContentAttemptDao.insert(roomContentAttempt)
                }
            }
        }
    }

    fun getContentAttempts(contentId: Long): LiveData<List<DomainContentAttempt>> {
        return Transformations.map(roomContentAttemptDao.getForContentId(contentId)) {
            it?.asDomainContentAttempts()
        }
    }

    fun getContentAttemptsFromDB(contentId: Long): List<CourseAttempt> {
        return contentAttemptDao.queryBuilder()
            .where(CourseAttemptDao.Properties.ChapterContentId.eq(contentId))
            .list()
    }

    private fun _fetchLanguages(examSlug: String, examId: Long) {
        examNetwork.getLanguages(examSlug)
            .enqueue(object : TestpressCallback<TestpressApiResponse<NetworkLanguage>>() {
                override fun onSuccess(response: TestpressApiResponse<NetworkLanguage>) {
                    val fetchedLanguages = response.results
                    for (language in fetchedLanguages) {
                        language.examId = examId
                    }
                    languages.addAll(fetchedLanguages)
                    isLanguagesBeingFetched = false
                    storeLanguagesInDB(languages, examId)
                    resourceLanguages.value = Resource.success(languages.toDomainLanguages())
                }

                override fun onException(exception: TestpressException) {
                    isLanguagesBeingFetched = false
                    resourceLanguages.value = Resource.error(exception, null)
                }
            })
    }

    fun fetchLanguages(examSlug: String, examId: Long): LiveData<Resource<List<DomainLanguage>>> {
        if (!isLanguagesBeingFetched) {
            isLanguagesBeingFetched = true
            _fetchLanguages(examSlug, examId)
        }
        return resourceLanguages
    }

    fun storeLanguagesInDB(networkLanguages: ArrayList<NetworkLanguage>, examId: Long) {
        languageDao.queryBuilder()
            .where(LanguageDao.Properties.ExamId.eq(examId))
            .buildDelete()
            .executeDeleteWithoutDetachingEntities()
        val languages = networkLanguages.asGreenDaoModels()
        for (language in languages) {
            language.examId = examId
        }
        languageDao.insertOrReplaceInTx(languages)

        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                roomLanguageDao.deleteForExam(examId)
                val roomlanguages = networkLanguages.asDatabaseModels()
                for (language in roomlanguages) {
                    language.examId = examId
                }
                roomLanguageDao.insertAll(roomlanguages)
            }
        }
    }
}