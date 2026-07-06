package `in`.testpress.course.repository

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.course.domain.DomainContentArtifact
import `in`.testpress.course.network.CourseNetwork
import `in`.testpress.course.network.NetworkContentArtifact
import `in`.testpress.network.Resource
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData


class ContentArtifactsRepository(private val context: Context) {

    private val courseNetwork = CourseNetwork(context)

    fun loadArtifacts(contentId: Long): LiveData<Resource<List<DomainContentArtifact>>> {
        val result = MutableLiveData<Resource<List<DomainContentArtifact>>>()
        result.value = Resource.loading(null)

        courseNetwork.getArtifacts(contentId).enqueue(
            object : TestpressCallback<`in`.testpress.course.network.NetworkContentArtifactsResponse>() {
                override fun onSuccess(response: `in`.testpress.course.network.NetworkContentArtifactsResponse) {
                    val artifacts = response.results.map { it.toDomain() }
                    result.postValue(Resource.success(artifacts))
                }

                override fun onException(exception: TestpressException) {
                    result.postValue(Resource.error(exception, null))
                }
            }
        )

        return result
    }

    private fun NetworkContentArtifact.toDomain() = DomainContentArtifact(
        id = id,
        name = name,
        url = url,
        accessibleWithoutAttempt = accessibleWithoutAttempt
    )
}
