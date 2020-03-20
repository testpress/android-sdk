package `in`.testpress.course.repository

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.core.TestpressSDKDatabase
import `in`.testpress.course.domain.DomainContentAttempt
import `in`.testpress.course.domain.DomainLanguage
import `in`.testpress.course.domain.asDomainContentAttempts
import `in`.testpress.course.domain.toDomainLanguages
import `in`.testpress.course.network.NetworkContent
import `in`.testpress.course.network.NetworkContentAttempt
import `in`.testpress.course.network.Resource
import `in`.testpress.course.network.asGreenDaoModel
import `in`.testpress.exam.network.ExamNetwork
import `in`.testpress.exam.network.NetworkLanguage
import `in`.testpress.exam.network.asGreenDaoModels
import `in`.testpress.models.TestpressApiResponse
import `in`.testpress.models.greendao.CourseAttempt
import `in`.testpress.models.greendao.CourseAttemptDao
import `in`.testpress.models.greendao.LanguageDao
import `in`.testpress.network.RetrofitCall
import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class ExamContentRepository(
    context: Context
) : ContentRepository(context) {

    private val examContentDao = TestpressSDKDatabase.getExamDao(context)
    private val languageDao = TestpressSDKDatabase.getLanguageDao(context)
    private val attemptDao = TestpressSDKDatabase.getAttemptDao(context)
    private val contentAttemptDao = TestpressSDKDatabase.getCourseAttemptDao(context)
    private val examNetwork = ExamNetwork(context)
    private var isLanguagesBeingFetched = false
    private var languages = arrayListOf<NetworkLanguage>()
    var resourceLanguages: MutableLiveData<Resource<List<DomainLanguage>>> = MutableLiveData()

    fun loadAttempts(attemptsUrl: String, contentId: Long, forceRefresh: Boolean = false): LiveData<Resource<ArrayList<DomainContentAttempt>>> {
        return object : MultiPageNetworkBoundResource<ArrayList<DomainContentAttempt>, NetworkContentAttempt>() {
            override fun saveNetworkResponseToDB(item: List<NetworkContentAttempt>) {
                saveCourseAttemptInDB(ArrayList(item), contentId)
            }

            override fun shouldFetch(data: ArrayList<DomainContentAttempt>?): Boolean {
                return forceRefresh || getContentAttemptsFromDB(contentId).isEmpty()
            }

            override fun loadFromDb(): LiveData<ArrayList<DomainContentAttempt>> {
                val liveData = MutableLiveData<ArrayList<DomainContentAttempt>>()
                val contentAttempts = contentAttemptDao.queryBuilder()
                    .where(CourseAttemptDao.Properties.ChapterContentId.eq(contentId))
                    .list()
                liveData.postValue(ArrayList(contentAttempts.asDomainContentAttempts()))
                return liveData
            }

            override fun shouldClearDB()  = true

            override fun clearFromDB() {
                clearContentAttemptsInDB(contentId)
            }

            override fun createCall(
                url: String?
            ): RetrofitCall<TestpressApiResponse<NetworkContentAttempt>> {
                if (url != null) return courseNetwork.getContentAttempts(url)
                return courseNetwork.getContentAttempts(attemptsUrl)
            }
        }.asLiveData()
    }

    fun getContentAttemptsFromDB(contentId: Long): MutableList<CourseAttempt> {
        return contentAttemptDao.queryBuilder()
            .where(CourseAttemptDao.Properties.ChapterContentId.eq(contentId))
            .list()
    }

    fun clearContentAttemptsInDB(contentId: Long) {
        contentAttemptDao.queryBuilder()
            .where(CourseAttemptDao.Properties.ChapterContentId.eq(contentId))
            .buildDelete()
            .executeDeleteWithoutDetachingEntities()
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun saveCourseAttemptInDB(
        courseAttemptList: ArrayList<NetworkContentAttempt>,
        contentId: Long
    ) {
        for (networkContentAttempt in courseAttemptList) {
            val contentAttempt = networkContentAttempt.asGreenDaoModel()
            val attempt = networkContentAttempt.assessment!!
            attemptDao.insertOrReplace(attempt.asGreenDaoModel())
            contentAttempt.assessmentId = attempt.id
            contentAttempt.chapterContentId = contentId
            contentAttemptDao.insertOrReplace(contentAttempt)
        }
    }

    private fun fetchLanguagesNetwork(examSlug: String, examId: Long) {
        examNetwork.getLanguages(examSlug)
            .enqueue(object : TestpressCallback<TestpressApiResponse<NetworkLanguage>>() {
                override fun onSuccess(response: TestpressApiResponse<NetworkLanguage>) {
                    handleLanguagesFetchSuccess(response, examId)
                }

                override fun onException(exception: TestpressException) {
                    isLanguagesBeingFetched = false
                    resourceLanguages.value = Resource.error(exception, null)
                }
            })
    }

    private fun handleLanguagesFetchSuccess(response: TestpressApiResponse<NetworkLanguage>, examId: Long) {
        val fetchedLanguages = response.results
        for (language in fetchedLanguages) {
            language.examId = examId
        }
        languages.addAll(fetchedLanguages)
        isLanguagesBeingFetched = false
        storeLanguagesInDB(languages, examId)
        resourceLanguages.value = Resource.success(languages.toDomainLanguages())
    }

    fun loadLanguages(examSlug: String, examId: Long): LiveData<Resource<List<DomainLanguage>>> {
        if (!isLanguagesBeingFetched) {
            isLanguagesBeingFetched = true
            fetchLanguagesNetwork(examSlug, examId)
        }
        return resourceLanguages
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
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
    }

    override fun storeContentAndItsRelationsToDB(content: NetworkContent) {
        val greenDaoContent = content.asGreenDaoModel()
        content.exam?.let {
            val exam = it.asGreenDaoModel()
            greenDaoContent.examId = exam.id
            examContentDao.insertOrReplace(exam)
        }
        contentDao.insertOrReplace(greenDaoContent)
    }
}