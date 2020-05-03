package `in`.testpress.course.repository

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.core.TestpressSDKDatabase
import `in`.testpress.course.domain.DomainAttempt
import `in`.testpress.course.domain.DomainContentAttempt
import `in`.testpress.course.domain.asDomainContentAttempt
import `in`.testpress.course.domain.asDomainModel
import `in`.testpress.course.network.CourseNetwork
import `in`.testpress.course.network.NetworkContentAttempt
import `in`.testpress.course.network.Resource
import `in`.testpress.course.network.asGreenDaoModel
import `in`.testpress.exam.network.NetworkAttempt
import `in`.testpress.exam.network.asGreenDaoModel
import `in`.testpress.models.greendao.AttemptDao
import `in`.testpress.models.greendao.CourseAttemptDao
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

open class QuizExamRepository(val context: Context) {
    private val courseNetwork = CourseNetwork(context)
    val courseAttemptDao = TestpressSDKDatabase.getCourseAttemptDao(context)
    val attemptDao = TestpressSDKDatabase.getAttemptDao(context)

    var _resourceContentAttempt: MutableLiveData<Resource<DomainContentAttempt>> = MutableLiveData()
    val resourceContentAttempt: LiveData<Resource<DomainContentAttempt>>
        get() = _resourceContentAttempt

    var resourceAttempt: MutableLiveData<Resource<DomainAttempt>> = MutableLiveData()

    fun createAttempt(contentId: Long): LiveData<Resource<DomainContentAttempt>> {
        courseNetwork.createContentAttempt(contentId)
            .enqueue(object: TestpressCallback<NetworkContentAttempt>() {
                override fun onSuccess(result: NetworkContentAttempt?) {
                    val contentAttempt = result
                    contentAttempt?.assessmentId = result?.assessment?.id
                    attemptDao.insertOrReplaceInTx(contentAttempt?.assessment?.asGreenDaoModel())
                    courseAttemptDao.insertOrReplaceInTx(contentAttempt?.asGreenDaoModel())
                    loadAttempt(result!!.id)
                }

                override fun onException(exception: TestpressException?) {
                }
            })
        return resourceContentAttempt
    }

    fun loadAttempt(attemptId: Long): LiveData<Resource<DomainContentAttempt>> {
        val attempts = courseAttemptDao.queryBuilder().where(CourseAttemptDao.Properties.Id.eq(attemptId)).list()
        if (attempts.isNotEmpty()) {
            _resourceContentAttempt.postValue(Resource.success(attempts[0].asDomainContentAttempt()))
        }
        return resourceContentAttempt
    }

    fun endExam(url: String): MutableLiveData<Resource<DomainAttempt>> {
        courseNetwork.endContentAttempt(url)
            .enqueue(object : TestpressCallback<NetworkAttempt>() {
                override fun onSuccess(result: NetworkAttempt?) {
                    attemptDao.insertOrReplaceInTx(result?.asGreenDaoModel())
                    val attempts = attemptDao.queryBuilder()
                        .where(AttemptDao.Properties.Id.eq(result!!.id)).list()
                    resourceAttempt.postValue(Resource.success(attempts[0].asDomainModel()))
                }

                override fun onException(exception: TestpressException?) {

                }
            })
        return resourceAttempt
    }
}