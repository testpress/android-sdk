package `in`.testpress.course.helpers

import `in`.testpress.course.network.CourseNetwork
import `in`.testpress.course.util.ExoPlayerDataSourceFactory
import android.content.Context
import com.google.android.exoplayer2.drm.ExoMediaDrm
import com.google.android.exoplayer2.drm.HttpMediaDrmCallback
import com.google.android.exoplayer2.drm.MediaDrmCallback
import java.util.*

class DrmPhaseException(val phase: String, cause: Throwable) : Exception(phase, cause)


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
        return try {
            val drmLicenseURL: String = fetchDrmLicenseURL()
            val updatedRequest = ExoMediaDrm.KeyRequest(request.data, drmLicenseURL)
            httpMediaDrmCallback.executeKeyRequest(uuid, updatedRequest)
        } catch (t: Throwable) {
            throw DrmPhaseException("LICENSE_REQUEST", t)
        }
    }

    override fun executeProvisionRequest(
        uuid: UUID,
        request: ExoMediaDrm.ProvisionRequest
    ): ByteArray {
        return try {
            val updatedRequest = ExoMediaDrm.ProvisionRequest(request.data, request.defaultUrl)
            httpMediaDrmCallback.executeProvisionRequest(uuid, updatedRequest)
        } catch (t: Throwable) {
            throw DrmPhaseException("PROVISION_REQUEST", t)
        }
    }
}