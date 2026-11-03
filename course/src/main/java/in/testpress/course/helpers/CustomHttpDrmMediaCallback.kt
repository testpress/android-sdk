package `in`.testpress.course.helpers

import `in`.testpress.course.network.CourseNetwork
import `in`.testpress.course.util.ExoPlayerDataSourceFactory
import android.content.Context
import com.google.android.exoplayer2.drm.ExoMediaDrm
import com.google.android.exoplayer2.drm.HttpMediaDrmCallback
import com.google.android.exoplayer2.drm.MediaDrmCallback
import `in`.testpress.course.util.PlayerDebugDiagnostics
import java.util.*


internal class CustomHttpDrmMediaCallback @JvmOverloads constructor(
    val context: Context, 
    val contentId: Long, 
    val isDownload: Boolean = false
) : MediaDrmCallback {

    private val httpMediaDrmCallback = HttpMediaDrmCallback(
        "",
        false,
        ExoPlayerDataSourceFactory(context).getHttpDataSourceFactory()
    )
    val courseNetwork = CourseNetwork(context)

    private fun fetchDrmLicenseURL(): String {
        try {
            val response = courseNetwork.getDRMLicenseURL(contentId, isDownload).execute()
            if (response.isSuccessful) {
                val drmLicense = response.body()!!
                return drmLicense.licenseUrl ?: ""
            } else {
                val errorBody = response.errorBody()?.string()?.take(300) ?: "No error body"
                throw Exception("HTTP ${response.code()}: $errorBody")
            }
        } catch (t: Throwable) {
            throw Exception("Failed to fetch license URL: ${t.message}", t)
        }
    }

    override fun executeKeyRequest(uuid: UUID, request: ExoMediaDrm.KeyRequest): ByteArray {
        val drmLicenseURL: String = fetchDrmLicenseURL()
        val updatedRequest = ExoMediaDrm.KeyRequest(request.data, drmLicenseURL)
        return httpMediaDrmCallback.executeKeyRequest(uuid, updatedRequest)
    }

    override fun executeProvisionRequest(
        uuid: UUID,
        request: ExoMediaDrm.ProvisionRequest
    ): ByteArray {
        val sanitizedUrl = PlayerDebugDiagnostics.extractHostAndPath(request.defaultUrl)
        PlayerDebugDiagnostics.lastProvisioningUrl = sanitizedUrl
        PlayerDebugDiagnostics.lastProvisioningRequestSize = request.data.size
        val updatedRequest = ExoMediaDrm.ProvisionRequest(request.data, request.defaultUrl)
        val startTime = System.currentTimeMillis()
        return try {
            val response = httpMediaDrmCallback.executeProvisionRequest(uuid, updatedRequest)
            PlayerDebugDiagnostics.lastProvisioningTimeMs = System.currentTimeMillis() - startTime
            PlayerDebugDiagnostics.lastProvisioningResponseSize = response.size
            PlayerDebugDiagnostics.lastProvisioningException = "N/A"
            response
        } catch (t: Throwable) {
            PlayerDebugDiagnostics.lastProvisioningTimeMs = System.currentTimeMillis() - startTime
            PlayerDebugDiagnostics.lastProvisioningException = t.javaClass.simpleName + ": " + t.message
            PlayerDebugDiagnostics.lastProvisioningResponseSize = -1
            throw t
        }
    }
}