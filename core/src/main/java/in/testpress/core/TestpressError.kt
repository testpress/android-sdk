package `in`.testpress.core

data class TestpressError(
    val detail: ErrorDetail? = null
)

data class ErrorDetail(
    val errorCode: String? = null,
    val message: String? = null
)

data class TestpressErrorDetail(
    val detail: String? = null
)
