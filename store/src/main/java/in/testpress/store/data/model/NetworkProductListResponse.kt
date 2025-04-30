package `in`.testpress.store.data.model

data class NetworkProductListResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val perPage: Int,
    val results: Results
)

data class Results(
    val products: List<NetworkProductLite>,
    val courses: List<NetworkCourses>
)