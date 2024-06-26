package `in`.testpress.util

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.network.RetrofitCall
import `in`.testpress.v2_4.models.ApiResponse

abstract class PagedApiFetcher<NetworkDataType> {

    // Initiates the API call for a specific page
    abstract fun createApiCall(page: Int): RetrofitCall<ApiResponse<NetworkDataType>>

    // Handles the processing of results from a single page response
    abstract fun handlePageResults(results: NetworkDataType)

    // Called when all pages have been successfully fetched and processed
    abstract fun onAllPagesFetched()

    // Called when an error occurs during the API call
    abstract fun onFetchError(exception: TestpressException)

    // Public method to start fetching all pages
    fun fetchAllPages() {
        var currentPage = 1

        // Internal function to fetch the next page
        fun fetchNextPage() {
            createApiCall(currentPage).enqueue(object : TestpressCallback<ApiResponse<NetworkDataType>>() {
                override fun onSuccess(response: ApiResponse<NetworkDataType>) {
                    handlePageResults(response.results)
                    if (response.next != null) {
                        currentPage++
                        fetchNextPage()
                    } else {
                        onAllPagesFetched()
                    }
                }

                override fun onException(exception: TestpressException) {
                    onFetchError(exception)
                }
            })
        }

        fetchNextPage()
    }
}