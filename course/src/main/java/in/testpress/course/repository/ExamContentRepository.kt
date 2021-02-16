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
import `in`.testpress.network.Resource
import `in`.testpress.course.network.asDomainContentAttempt
import `in`.testpress.course.network.asGreenDaoModel
import `in`.testpress.exam.network.ExamNetwork
import `in`.testpress.exam.network.NetworkLanguage
import `in`.testpress.exam.network.asGreenDaoModel
import `in`.testpress.exam.network.asGreenDaoModels
import `in`.testpress.models.TestpressApiResponse
import `in`.testpress.models.greendao.CourseAttemptDao
import `in`.testpress.models.greendao.LanguageDao
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
    private var contentAttempts = arrayListOf<DomainContentAttempt>()
    private var networkContentAttempts = arrayListOf<NetworkContentAttempt>()
    var resourceContentAttempt: MutableLiveData<Resource<ArrayList<DomainContentAttempt>>> =
        MutableLiveData()
    private var isLanguagesBeingFetched = false
    private var isAttemptsBeingFetched = false
    private var languages = arrayListOf<NetworkLanguage>()
    var resourceLanguages: MutableLiveData<Resource<List<DomainLanguage>>> = MutableLiveData()

    private fun fetchAttemptFromNetwork(url: String, contentId: Long) {
        courseNetwork.getContentAttempts(url)
            .enqueue(object : TestpressCallback<TestpressApiResponse<NetworkContentAttempt>>() {
                override fun onSuccess(response: TestpressApiResponse<NetworkContentAttempt>?) {
                    handleAttemptsFetchSuccess(response, contentId)
                }

                override fun onException(exception: TestpressException) {
                    isAttemptsBeingFetched = false
                    resourceContentAttempt.value = Resource.error(exception, null)
                }
            })
    }

    private fun handleAttemptsFetchSuccess(response:TestpressApiResponse<NetworkContentAttempt>?, contentId: Long) {
        contentAttempts.addAll(response?.results?.asDomainContentAttempt() ?: listOf())
        networkContentAttempts.addAll(response?.results ?: listOf())

        if (response?.next != null) {
            fetchAttemptFromNetwork(response.next, contentId)
        } else {
            isAttemptsBeingFetched = false
            clearContentAttemptsInDB(contentId)
            saveCourseAttemptInDB(networkContentAttempts, contentId)
            resourceContentAttempt.value = Resource.success(contentAttempts)
        }
    }

    fun loadAttempts(url: String?, contentId: Long): LiveData<Resource<ArrayList<DomainContentAttempt>>> {
        if (url == null) {
            val contentAttempts = contentAttemptDao.queryBuilder().where(CourseAttemptDao.Properties.ChapterContentId.eq(contentId)).list()
            val domainContentAttempts = ArrayList(contentAttempts.asDomainContentAttempts())
            resourceContentAttempt.postValue(Resource.success(domainContentAttempts))
        } else if (!isAttemptsBeingFetched) {
            isAttemptsBeingFetched = true
            fetchAttemptFromNetwork(url, contentId)
        }
        return resourceContentAttempt
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
            val attemptSections = networkContentAttempt.assessment.sections
            val attemptSectionDao = TestpressSDKDatabase.getAttemptSectionDao(context)
            attemptSectionDao.insertOrReplaceInTx(attemptSections.asGreenDaoModel())
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