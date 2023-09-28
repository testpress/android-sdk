package `in`.testpress.course.repository

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.core.TestpressSDKDatabase
import `in`.testpress.course.domain.*
import `in`.testpress.course.network.CourseNetwork
import `in`.testpress.course.network.NetworkContentAttempt
import `in`.testpress.network.Resource
import `in`.testpress.course.network.asGreenDaoModel
import `in`.testpress.exam.api.TestpressExamApiClient
import `in`.testpress.exam.domain.DomainAttempt
import `in`.testpress.exam.domain.asDomainModel
import `in`.testpress.exam.network.NetworkAttempt
import `in`.testpress.exam.network.asGreenDaoModel
import `in`.testpress.models.greendao.Attempt
import `in`.testpress.models.greendao.AttemptDao
import `in`.testpress.models.greendao.CourseAttempt
import `in`.testpress.models.greendao.CourseAttemptDao
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlin.random.Random

open class QuizExamRepository(val context: Context) {
    private val courseNetwork = CourseNetwork(context)
    val courseAttemptDao = TestpressSDKDatabase.getCourseAttemptDao(context)
    val attemptDao = TestpressSDKDatabase.getAttemptDao(context)

    private var _resourceAttempt: MutableLiveData<Resource<DomainAttempt>> = MutableLiveData()
    val resourceAttempt: LiveData<Resource<DomainAttempt>>
        get() = _resourceAttempt

    var _resourceContentAttempt: MutableLiveData<Resource<DomainContentAttempt>> = MutableLiveData()
    val resourceContentAttempt: LiveData<Resource<DomainContentAttempt>>
        get() = _resourceContentAttempt

    private var _endAttemptState: MutableLiveData<Resource<DomainAttempt>> = MutableLiveData()
    val endAttemptState: LiveData<Resource<DomainAttempt>>
        get() = _endAttemptState

    private var _endContentAttemptState: MutableLiveData<Resource<DomainContentAttempt>> = MutableLiveData()
    val endContentAttemptState: LiveData<Resource<DomainContentAttempt>>
        get() = _endContentAttemptState

    fun createAttempt(attemptId: Long): LiveData<Resource<DomainAttempt>> {
        val apiClient = TestpressExamApiClient(context)
        apiClient.startAttempt("api/v2.2/attempts/$attemptId/start/")
            .enqueue(object: TestpressCallback<Attempt>() {
                override fun onSuccess(result: Attempt?) {
                    attemptDao.insertOrReplaceInTx(result)
                    loadAttempt(result!!.id)
                }

                override fun onException(exception: TestpressException?) {
                    if (exception?.isNetworkError == true) {
                        loadAttemptFromDB(attemptId)
                    } else {
                        _resourceAttempt.postValue(Resource.error(exception!!, null))
                    }
                }
            })
        return resourceAttempt
    }

    fun createContentAttempt(contentId: Long): LiveData<Resource<DomainContentAttempt>> {
        courseNetwork.createContentAttempt(contentId)
            .enqueue(object: TestpressCallback<NetworkContentAttempt>() {
                override fun onSuccess(result: NetworkContentAttempt?) {
                    saveContentAttempt(result)
                    loadContentAttempt(result!!.id)
                }

                override fun onException(exception: TestpressException?) {
                    if (exception?.isNetworkError == true) {
                        loadContentAttemptFromDB(contentId)
                    } else {
                        _resourceContentAttempt.postValue(Resource.error(exception!!, null))
                    }
                }
            })
        return resourceContentAttempt
    }

    fun loadAttemptFromDB(attemptId: Long) {
        val attempt = getRunningAttemptFromDB(attemptId)
        _resourceAttempt.postValue(Resource.success(attempt?.asDomainModel()))
    }

    fun loadContentAttemptFromDB(contentId: Long) {
        val contentAttempt = getRunningContentAttemptFromDB(contentId) ?: createLocalContentAttempt(contentId)
        _resourceContentAttempt.postValue(Resource.success(contentAttempt.asDomainContentAttempt()))
    }

    private fun getRunningAttemptFromDB(attemptId: Long): Attempt? {
        val attempts = attemptDao.queryBuilder()
            .where(AttemptDao.Properties.State.eq("Running"), AttemptDao.Properties.Id.`in`(attemptId))
            .orderDesc(AttemptDao.Properties.Id).list()
        return attempts.first()
    }

    private fun getRunningContentAttemptFromDB(contentId: Long): CourseAttempt? {
        val contentAttempts = courseAttemptDao.queryBuilder()
            .where(CourseAttemptDao.Properties.ChapterContentId.eq(contentId)).list()
        val attemptIds = contentAttempts.map {it.assessmentId.toInt()}
        val attempts = attemptDao.queryBuilder()
            .where(AttemptDao.Properties.State.eq("Running"), AttemptDao.Properties.Id.`in`(attemptIds))
            .orderDesc(AttemptDao.Properties.Id).list()

        if (attempts.isNotEmpty()) {
            return courseAttemptDao.queryBuilder()
                .where(CourseAttemptDao.Properties.AssessmentId.eq(attempts[0].id)).list()[0]
        }
        return null
    }

    fun createLocalContentAttempt(contentId: Long): CourseAttempt {
        val id = Random.nextLong(9999, 999999)
        val attemptId = Random.nextLong(9999, 999999)
        val attempt = Attempt(attemptId)
        attempt.state = "Running"
        val courseAttempt = CourseAttempt(id, "assessment", attemptId.toInt(), null, null, contentId, attemptId, null)
        attemptDao.insertOrReplaceInTx(attempt)
        courseAttemptDao.insertOrReplaceInTx(courseAttempt)
        return courseAttempt
    }

    fun saveContentAttempt(contentAttempt: NetworkContentAttempt?) {
        contentAttempt?.assessmentId = contentAttempt?.assessment?.id
        contentAttempt?.chapterContentId = contentAttempt?.chapterContent?.id
        attemptDao.insertOrReplaceInTx(contentAttempt?.assessment?.asGreenDaoModel())
        courseAttemptDao.insertOrReplaceInTx(contentAttempt?.asGreenDaoModel())
    }

    fun loadAttempt(attemptId: Long): LiveData<Resource<DomainAttempt>> {
        val attempts = attemptDao.queryBuilder()
            .where(AttemptDao.Properties.Id.eq(attemptId)).list()
        if (attempts.isNotEmpty()) {
            _resourceAttempt.postValue(Resource.success(attempts[0].asDomainModel()))
        }
        return resourceAttempt
    }

    fun loadContentAttempt(contentAttemptId: Long): LiveData<Resource<DomainContentAttempt>> {
        val contentAttempts = courseAttemptDao.queryBuilder()
            .where(CourseAttemptDao.Properties.Id.eq(contentAttemptId)).list()

        if (contentAttempts.isNotEmpty()) {
            _resourceContentAttempt.postValue(Resource.success(contentAttempts[0].asDomainContentAttempt()))
        }
        return resourceContentAttempt
    }

    fun endAttempt(url: String, attemptId: Long) {
        courseNetwork.endAttempt(url)
            .enqueue(object : TestpressCallback<NetworkAttempt>() {
                override fun onSuccess(result: NetworkAttempt?) {
                    attemptDao.insertOrReplaceInTx(result?.asGreenDaoModel())
                    val attempts = attemptDao.queryBuilder()
                        .where(AttemptDao.Properties.Id.eq(result!!.id)).list()
                    _endAttemptState.postValue(Resource.success(attempts[0].asDomainModel()))
                }

                override fun onException(exception: TestpressException?) {
                    val attempt = attemptDao.queryBuilder()
                        .where(AttemptDao.Properties.Id.eq(attemptId)).list()[0]
                    attempt.state = "COMPLETED"
                    attemptDao.insertOrReplaceInTx(attempt)
                    _endAttemptState.postValue(Resource.success(attempt.asDomainModel()))
                }
            })
    }

    fun endContentAttempt(url: String, attemptId: Long) {
        courseNetwork.endContentAttempt(url)
            .enqueue(object : TestpressCallback<NetworkContentAttempt>() {
                override fun onSuccess(result: NetworkContentAttempt?) {
                    courseAttemptDao.insertOrReplaceInTx(result?.asGreenDaoModel())
                    attemptDao.insertOrReplaceInTx(result?.assessment?.asGreenDaoModel())
                    val contentAttempts = courseAttemptDao.queryBuilder()
                        .where(CourseAttemptDao.Properties.Id.eq(result?.id)).list()
                    _endContentAttemptState.postValue(Resource.success(contentAttempts[0].asDomainContentAttempt()))
                }

                override fun onException(exception: TestpressException?) {
                    val attempt = attemptDao.queryBuilder()
                        .where(AttemptDao.Properties.Id.eq(attemptId)).list()[0]
                    attempt.state = "COMPLETED"
                    attemptDao.insertOrReplaceInTx(attempt)
                    val contentAttempts = courseAttemptDao.queryBuilder()
                        .where(CourseAttemptDao.Properties.AssessmentId.eq(attempt.id)).list()
                    _endContentAttemptState.postValue(Resource.success(contentAttempts[0].asDomainContentAttempt()))
                }
            })
    }
}