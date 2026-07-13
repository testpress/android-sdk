package `in`.testpress.course.domain

enum class NewOfflineAttachmentDownloadFailureReason {
    FAILURE_REASON_NONE,
    FAILURE_REASON_NETWORK,
    FAILURE_REASON_IO,
    FAILURE_REASON_HTTP,
    FAILURE_REASON_UNKNOWN
}

interface NewOfflineAttachmentDownloadListener {
    fun onDownloadProgress(id: Long, bytesDownloaded: Long, contentLength: Long, percent: Float)
    fun onDownloadCompleted(id: Long, filePath: String)
    fun onDownloadFailed(id: Long, failureReason: NewOfflineAttachmentDownloadFailureReason, exception: Exception?)
}
