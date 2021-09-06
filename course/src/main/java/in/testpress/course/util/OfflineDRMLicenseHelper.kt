package `in`.testpress.course.util

import `in`.testpress.course.helpers.CustomHttpDrmMediaCallback
import `in`.testpress.course.helpers.VideoDownload
import `in`.testpress.course.helpers.VideoDownload.getDownloadRequest
import `in`.testpress.course.helpers.VideoDownloadManager
import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.drm.DefaultDrmSessionManager
import com.google.android.exoplayer2.drm.DrmSession
import com.google.android.exoplayer2.drm.DrmSessionEventListener
import com.google.android.exoplayer2.drm.OfflineLicenseHelper
import com.google.android.exoplayer2.offline.Download
import com.google.android.exoplayer2.offline.DownloadHelper
import com.google.android.exoplayer2.offline.DownloadRequest
import com.google.android.exoplayer2.source.dash.DashUtil
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


object OfflineDRMLicenseHelper {
    @JvmStatic
    fun renewLicense(url:String, contentId: Long, context: Context, callback: DRMLicenseFetchCallback) {
        CoroutineScope(Dispatchers.IO).launch {
            val dataSource = DefaultHttpDataSource.Factory().createDataSource()
            val dashManifest = DashUtil.loadManifest(dataSource, Uri.parse(url))
            val sessionManager = DefaultDrmSessionManager.Builder()
                .build(CustomHttpDrmMediaCallback(context, contentId))
            val drmInitData = DashUtil.loadFormatWithDrmInitData(dataSource, dashManifest.getPeriod(0))
            val keySetId = OfflineLicenseHelper(
                sessionManager,
                DrmSessionEventListener.EventDispatcher()
            ).downloadLicense(
                drmInitData!!
            )

            replaceKeysInExistingDownloadedVideo(url, context, keySetId)
            callback.onLicenseFetchSuccess(keySetId)
        }
    }

    private fun replaceKeysInExistingDownloadedVideo(
        url: String,
        context: Context,
        keySetId: ByteArray
    ) {
        val downloadRequest = getDownloadRequest(url, context)
        if (downloadRequest != null) {
            val newDownloadRequest: DownloadRequest =
                cloneDownloadRequestWithNewKeys(downloadRequest, keySetId)
            val download = VideoDownload.getDownload(url, context)
            val newDownload = cloneDownloadWithNewDownloadRequest(download!!, newDownloadRequest)
            val dowloadIndex = VideoDownloadManager(context).getDownloadIndex()
            dowloadIndex.putDownload(newDownload)
        }
    }

    private fun cloneDownloadRequestWithNewKeys(downloadRequest: DownloadRequest, keySetId: ByteArray): DownloadRequest {
        return DownloadRequest.Builder(
            downloadRequest.id,
            downloadRequest.uri
        )
            .setStreamKeys(downloadRequest.streamKeys)
            .setCustomCacheKey(downloadRequest.customCacheKey)
            .setKeySetId(keySetId)
            .setData(downloadRequest.data)
            .setMimeType(downloadRequest.mimeType)
            .build()
    }

    fun cloneDownloadWithNewDownloadRequest(download: Download, downloadRequest: DownloadRequest): Download {
        return Download(
            download.request.copyWithMergedRequest(downloadRequest),
            download.state,
            download.startTimeMs,
            download.updateTimeMs,
            download.contentLength,
            download.stopReason,
            download.failureReason
        )
    }

    fun fetchLicense(context: Context, contentId: Long, downloadHelper: DownloadHelper, callback: DRMLicenseFetchCallback) {
        val sessionManager = DefaultDrmSessionManager.Builder()
            .build(CustomHttpDrmMediaCallback(context, contentId))
        val offlineLicenseHelper = OfflineLicenseHelper(
            sessionManager, DrmSessionEventListener.EventDispatcher()
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val keySetId = offlineLicenseHelper.downloadLicense(
                    VideoUtils.getAudioOrVideoInfoWithDrmInitData(
                        downloadHelper
                    )!!)
                callback.onLicenseFetchSuccess(keySetId)
            } catch (e: DrmSession.DrmSessionException) {
                callback.onLicenseFetchFailure()
            } finally {
                offlineLicenseHelper.release()
            }
        }
    }
}

interface DRMLicenseFetchCallback {
    fun onLicenseFetchSuccess(keySetId: ByteArray)
    fun onLicenseFetchFailure()
}