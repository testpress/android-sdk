package `in`.testpress.course.helpers

import `in`.testpress.course.network.CourseNetwork
import `in`.testpress.course.util.ExoPlayerDataSourceFactory
import android.content.Context
import com.google.android.exoplayer2.drm.ExoMediaDrm
import com.google.android.exoplayer2.drm.HttpMediaDrmCallback
import com.google.android.exoplayer2.drm.MediaDrmCallback
import java.util.*


internal class CustomHttpDrmMediaCallback(val context: Context, val contentId: Long) :
    MediaDrmCallback {

    private val httpMediaDrmCallback = HttpMediaDrmCallback(
        "",
        false,
        ExoPlayerDataSourceFactory(context).getHttpDataSourceFactory()
    )
    val courseNetwork = CourseNetwork(context)

    private fun fetchDrmLicenseURL(): String {
        val response = courseNetwork.getDRMLicenseURL(contentId).execute()
        return if (response.isSuccessful) {
            val drmLicense = response.body()!!
            drmLicense.licenseUrl ?: ""
        } else {
            ""
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
        val licenseUrl: String = fetchDrmLicenseURL()
        val updatedRequest = ExoMediaDrm.ProvisionRequest(request.data, licenseUrl)
        return httpMediaDrmCallback.executeProvisionRequest(uuid, updatedRequest)
    }
}