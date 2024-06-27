package `in`.testpress.exam.repository

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.exam.api.TestpressExamApiClient
import `in`.testpress.exam.models.AttemptItem
import `in`.testpress.exam.network.NetworkAttemptSection
import `in`.testpress.exam.ui.TestFragment.Action
import `in`.testpress.models.TestpressApiResponse
import `in`.testpress.network.Resource
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class AttemptRepository(val context: Context) {

    var isOfflineExam = false
    var page = 1
    val attemptItem = mutableListOf<AttemptItem>()
    private var _totalQuestions = 0
    val totalQuestions get() = _totalQuestions
    private val apiClient: TestpressExamApiClient = TestpressExamApiClient(context)
    private val _attemptItemsResource = MutableLiveData<Resource<List<AttemptItem>>>()
    val attemptItemsResource: LiveData<Resource<List<AttemptItem>>> get() = _attemptItemsResource

    private val _saveResultResource = MutableLiveData<Resource<Triple<Int, AttemptItem?, Action>>>()
    val saveResultResource: LiveData<Resource<Triple<Int, AttemptItem?, Action>>> get() = _saveResultResource

    private val _updateSectionResource = MutableLiveData<Resource<Pair<NetworkAttemptSection?,Action>>>()
    val updateSectionResource: LiveData<Resource<Pair<NetworkAttemptSection?,Action>>> get() = _updateSectionResource

    fun fetchAttemptItems(questionsUrlFrag: String, fetchSinglePageOnly: Boolean) {
        _attemptItemsResource.postValue(Resource.loading(null))
        val queryParams = hashMapOf<String, Any>("page" to page)
        apiClient.getQuestions(questionsUrlFrag, queryParams)
            .enqueue(object : TestpressCallback<TestpressApiResponse<AttemptItem>>() {
                override fun onSuccess(result: TestpressApiResponse<AttemptItem>) {
                    if (fetchSinglePageOnly) {
                        _totalQuestions = result.count
                        attemptItem.addAll(result.results)
                        _attemptItemsResource.postValue(Resource.success(attemptItem))
                        if (result.hasMore()) {
                            page++
                        }
                        return
                    }
                    if (result.hasMore()) {
                        _totalQuestions = result.count
                        attemptItem.addAll(result.results)
                        page++
                        fetchAttemptItems(questionsUrlFrag, fetchSinglePageOnly)
                    } else {
                        attemptItem.addAll(result.results)
                        _attemptItemsResource.postValue(Resource.success(attemptItem))
                    }
                }

                override fun onException(exception: TestpressException) {
                    _attemptItemsResource.postValue(Resource.error(exception, null))
                }

            })


    }

    fun saveAnswer(position: Int, attemptItem: AttemptItem, action: Action) {
        apiClient.postAnswer(attemptItem).enqueue(object : TestpressCallback<AttemptItem>() {
            override fun onSuccess(result: AttemptItem) {
                _saveResultResource.postValue(Resource.success(Triple(position, result, action)))
            }

            override fun onException(exception: TestpressException) {
                _saveResultResource.postValue(
                    Resource.error(
                        exception,
                        Triple(position, null, action)
                    )
                )
            }
        })
    }

    fun updateSection(url: String, action: Action) {
        _updateSectionResource.postValue(Resource.loading(Pair(null, action)))
        apiClient.updateSection(url).enqueue(object : TestpressCallback<NetworkAttemptSection>() {
            override fun onSuccess(result: NetworkAttemptSection) {
                _updateSectionResource.postValue(Resource.success(Pair(result, action)))
            }

            override fun onException(exception: TestpressException) {
                _updateSectionResource.postValue(Resource.error(exception, Pair(null, action)))
            }
        })
    }

    fun clearAttemptItem() {
        attemptItem.clear()
    }

    fun resetPageCount() {
        page = 1
    }

}