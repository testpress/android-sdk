package `in`.testpress.exam.repository

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.exam.api.TestpressExamApiClient
import `in`.testpress.exam.models.AttemptItem
import `in`.testpress.models.TestpressApiResponse
import `in`.testpress.network.Resource
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class AttemptItemRepository(val context: Context) {

    var page = 1
    private val apiClient: TestpressExamApiClient = TestpressExamApiClient(context)

    private val _attemptItemsResource = MutableLiveData<Resource<List<AttemptItem>>>()
    val attemptItemsResource: LiveData<Resource<List<AttemptItem>>> get() = _attemptItemsResource


    fun fetchAttemptItems(questionsUrlFrag: String, fetchSinglePageOnly: Boolean) {
        _attemptItemsResource.postValue(Resource.loading(null))
        val queryParams = hashMapOf<String, Any>("page" to page)
        val attemptItem = mutableListOf<AttemptItem>()
        apiClient.getQuestions(questionsUrlFrag, queryParams)
            .enqueue(object : TestpressCallback<TestpressApiResponse<AttemptItem>>() {
                override fun onSuccess(result: TestpressApiResponse<AttemptItem>) {
                    Log.d("TAG", "onSuccess: ${result.results.size}")
                    if (result.hasMore() && !fetchSinglePageOnly){
                        attemptItem.addAll(result.results)
                        page++
                        fetchAttemptItems(questionsUrlFrag,fetchSinglePageOnly)
                    } else {
                        _attemptItemsResource.postValue(Resource.success(attemptItem))
                    }
                }

                override fun onException(exception: TestpressException) {
                    _attemptItemsResource.postValue(Resource.error(exception,null))
                }

            })


    }

}