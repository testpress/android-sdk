package `in`.testpress.course.models

import `in`.testpress.core.TestpressException
import `in`.testpress.course.enums.DataSource
import `in`.testpress.course.enums.Status


data class Resource<out T>(
        val status: Status,
        val data: T?,
        val exception: TestpressException?,
        var source: DataSource
) {
    companion object {
        fun <T> success(data: T?, source: DataSource = DataSource.NETWORK): Resource<T> {
            return Resource(Status.SUCCESS, data, null, source)
        }

        fun <T> error(exception: TestpressException, data: T?, source: DataSource = DataSource.NETWORK): Resource<T> {
            return Resource(Status.ERROR, data, exception, source)
        }

        fun <T> loading(data: T?, source: DataSource = DataSource.NETWORK): Resource<T> {
            return Resource(Status.LOADING, data, null, source)
        }
    }
}