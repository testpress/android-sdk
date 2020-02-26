package `in`.testpress.course.util

import `in`.testpress.core.TestpressCallback
import `in`.testpress.course.enums.Status
import `in`.testpress.course.models.Resource
import `in`.testpress.network.RetrofitCall
import retrofit2.Response

class RetrofitCallMock<T>(val resource: Resource<T>): RetrofitCall<T> {
    override fun execute(): Response<T> {
        return Response.success<T>(resource.data)
    }

    override fun clone(): RetrofitCall<T> {
        return this
    }

    override fun cancel() {

    }

    override fun enqueue(callback: TestpressCallback<T>?): RetrofitCall<T> {
        when(resource.status) {
            Status.SUCCESS -> callback?.onSuccess(resource.data)
            Status.ERROR -> callback?.onException(resource.exception)
        }
        return this
    }
}