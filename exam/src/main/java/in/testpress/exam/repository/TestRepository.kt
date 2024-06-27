package `in`.testpress.exam.repository

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.database.TestpressDatabase
import `in`.testpress.database.entities.OfflineAttempt
import `in`.testpress.exam.api.TestpressExamApiClient
import `in`.testpress.exam.models.Permission
import `in`.testpress.models.greendao.Attempt
import `in`.testpress.models.greendao.CourseAttempt
import `in`.testpress.models.greendao.Language
import `in`.testpress.network.Resource
import `in`.testpress.v2_4.models.ApiResponse
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.HashMap

class TestRepository(val context: Context) {

    var isOfflineExam = false

    private val database = TestpressDatabase.invoke(context)
    //private val offlineExamDao = database.offlineExamDao()
    //private val languageDao = database.languageDao()
    //private val directionDao = database.directionDao()
    //private val subjectDao = database.subjectDao()
    //private val sectionsDao = database.sectionsDao()
    private val examQuestionDao = database.examQuestionDao()
    //private val questionDao = database.questionDao()

    private val _attemptResource = MutableLiveData<Resource<Attempt>>()
    val attemptResource: LiveData<Resource<Attempt>> get() = _attemptResource

    private val _contentAttemptResource = MutableLiveData<Resource<CourseAttempt>>()
    val contentAttemptResource: LiveData<Resource<CourseAttempt>> get() = _contentAttemptResource

    private val _languageResource = MutableLiveData<Resource<List<Language>>>()
    val languageResource: LiveData<Resource<List<Language>>> get() = _languageResource

    private val _permissionResource = MutableLiveData<Resource<Permission>>()
    val permissionResource: LiveData<Resource<Permission>> get() = _permissionResource

    private val apiClient: TestpressExamApiClient = TestpressExamApiClient(context)

    fun createContentAttempt(examId: Long, attemptUrlFrag: String,queryParams: HashMap<String,Any>) {
        _contentAttemptResource.postValue(Resource.loading(null))
        if (isOfflineExam) {
            createContentAttemptOffline(examId)
        } else {
            createContentAttemptOnline(attemptUrlFrag, queryParams)
        }
    }

    private fun createContentAttemptOffline(examId: Long) {
        CoroutineScope(Dispatchers.IO).launch {

            val sectionIds = examQuestionDao.getUniqueSectionIdsByExamId(examId)

        }
    }

    private fun createContentAttemptOnline(attemptUrlFrag: String, queryParams: HashMap<String,Any>) {
        apiClient.createContentAttempt(attemptUrlFrag, queryParams)
            .enqueue(object : TestpressCallback<CourseAttempt>() {
                override fun onSuccess(result: CourseAttempt) {
                    _contentAttemptResource.postValue(Resource.success(result))
                }

                override fun onException(exception: TestpressException) {
                    _contentAttemptResource.postValue(Resource.error(exception,null))
                }
            })
    }

    fun createAttempt(examId: Long, attemptUrlFrag: String,queryParams: HashMap<String,Any>) {
        _attemptResource.postValue(Resource.loading(null))
        if (isOfflineExam) {
            createAttemptOffline(examId)
        } else {
            createAttemptOnline(attemptUrlFrag, queryParams)
        }
    }

    private fun createAttemptOffline(examId: Long) {


    }

    private fun createAttemptOnline(attemptUrlFrag: String, queryParams: HashMap<String,Any>) {
        apiClient.createAttempt(attemptUrlFrag, queryParams)
            .enqueue(object : TestpressCallback<Attempt>() {
                override fun onSuccess(response: Attempt) {
                    _attemptResource.postValue(Resource.success(response))
                }

                override fun onException(exception: TestpressException) {
                    _attemptResource.postValue(Resource.error(exception,null))
                }
            })
    }

    fun startAttempt(attemptStartFrag: String) {
        _attemptResource.postValue(Resource.loading(null))
        apiClient.startAttempt(attemptStartFrag).enqueue(object: TestpressCallback<Attempt>(){
            override fun onSuccess(result: Attempt) {
                _attemptResource.postValue(Resource.success(result))
            }

            override fun onException(exception: TestpressException) {
                _attemptResource.postValue(Resource.error(exception,null))
            }

        })
    }

    fun endContentAttempt(attemptEndFrag: String) {
        _contentAttemptResource.postValue(Resource.loading(null))
        apiClient.endContentAttempt(attemptEndFrag)
            .enqueue(object : TestpressCallback<CourseAttempt>() {
                override fun onSuccess(result: CourseAttempt) {
                    _contentAttemptResource.postValue(Resource.success(result))
                }

                override fun onException(exception: TestpressException) {
                    _contentAttemptResource.postValue(Resource.error(exception,null))
                }
            })
    }

    fun endAttempt(attemptEndFrag: String) {
        _attemptResource.postValue(Resource.loading(null))
        apiClient.endAttempt(attemptEndFrag)
            .enqueue(object : TestpressCallback<Attempt>() {
                override fun onSuccess(response: Attempt) {
                    _attemptResource.postValue(Resource.success(response))
                }

                override fun onException(exception: TestpressException) {
                    _attemptResource.postValue(Resource.error(exception,null))
                }
            })
    }

    fun fetchLanguages(examSlug: String) {
        _languageResource.postValue(Resource.loading(null))
        apiClient.getLanguages(examSlug)
            .enqueue(object : TestpressCallback<ApiResponse<List<Language>>>() {
                override fun onSuccess(result: ApiResponse<List<Language>>) {
                    _languageResource.postValue(Resource.success(result.results))
                }

                override fun onException(exception: TestpressException) {
                    _languageResource.postValue(Resource.error(exception, null))
                }
            })
    }

    fun checkPermission(contentId: Long) {
        _permissionResource.postValue(Resource.loading(null))
        apiClient.checkPermission(contentId)
            .enqueue(object : TestpressCallback < Permission >() {
                override fun onSuccess(result: Permission) {
                    _permissionResource.postValue(Resource.success(result))
                }

                override fun onException(exception: TestpressException) {
                    _permissionResource.postValue(Resource.error(exception, null))
                }

            })
    }
}