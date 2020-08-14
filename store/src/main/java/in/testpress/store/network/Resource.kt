package `in`.testpress.store.network

import `in`.testpress.core.TestpressException

enum class Status {
    SUCCESS, ERROR, LOADING
}

data class Resource<out T>(val status: Status, val data: T?, val exception: TestpressException?) {
    companion object {
        fun <T> success(data: T?): Resource<T> {
            return Resource(Status.SUCCESS, data, null)
        }

        fun <T> error(exception: TestpressException, data: T?): Resource<T> {
            return Resource(Status.ERROR, data, exception)
        }

        fun <T> loading(data: T?): Resource<T> {
            return Resource(Status.LOADING, data, null)
        }
    }
}