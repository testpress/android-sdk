package `in`.testpress.course.util

import android.content.Context
import android.media.MediaCodec
import android.media.MediaDrm
import android.media.NotProvisionedException
import android.media.DeniedByServerException
import android.media.ResourceBusyException
import android.net.Uri
import android.os.Build
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayerLibraryInfo
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.drm.DrmSession
import com.google.android.exoplayer2.drm.MediaDrmCallbackException
import com.google.android.exoplayer2.upstream.HttpDataSource
import `in`.testpress.course.BuildConfig
import `in`.testpress.models.greendao.Content
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import java.util.Date
import java.util.TimeZone
import java.text.SimpleDateFormat
import java.util.Locale
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

/**
 * Temporary debug-only diagnostics helper for ExoPlayer & DRM debugging.
 * Collects device info, Widevine/MediaDrm state, playback & network exception details,
 * and outputs them in the player error UI.
 */
object PlayerDebugDiagnostics {

    const val NA = "N/A"
    
    var lastProvisioningUrl: String = NA
    var lastProvisioningRequestSize: Int = 0
    var lastProvisioningResponseSize: Int = -1
    var lastProvisioningTimeMs: Long = 0
    var lastProvisioningException: String = NA

    /**
     * Formats an existing user-facing error message with DEBUG DETAILS appended if running in a DEBUG build.
     * In release builds, returns the userFacingMessage unmodified.
     */
    @JvmStatic
    fun formatErrorMessageWithDebugDetails(
        context: Context?,
        userFacingMessage: String,
        exception: PlaybackException?,
        playbackId: String?,
        content: Content?,
        mediaUrl: String?,
        isL3FallbackAttempted: Boolean = false
    ): String {
        if (!BuildConfig.DEBUG || exception == null) {
            return userFacingMessage
        }

        // 1. Build UI debug details section
        val uiDetails = buildUiDebugDetails(context, exception, playbackId, content, mediaUrl, isL3FallbackAttempted)

        // 2. Append to user-facing message, handling HTML if needed
        return if (userFacingMessage.contains("<html>", ignoreCase = true)) {
            val htmlDebugSection = "<br><br><b>DEBUG DETAILS</b><br>" +
                    htmlEscape(uiDetails).replace("\n", "<br>")
            if (userFacingMessage.contains("</p>", ignoreCase = true)) {
                userFacingMessage.replaceFirst("</p>", "$htmlDebugSection</p>")
            } else if (userFacingMessage.contains("</body>", ignoreCase = true)) {
                userFacingMessage.replaceFirst("</body>", "<p>$htmlDebugSection</p></body>")
            } else {
                "$userFacingMessage<br>$htmlDebugSection"
            }
        } else {
            "$userFacingMessage\n\nDEBUG DETAILS\n$uiDetails"
        }
    }

    /**
     * Builds the concise UI-friendly debug details section.
     */
    private fun buildUiDebugDetails(
        context: Context?,
        exception: PlaybackException,
        playbackId: String?,
        content: Content?,
        mediaUrl: String?,
        isL3FallbackAttempted: Boolean
    ): String {
        val sb = StringBuilder()

        val errorCode = exception.errorCode.toString()
        val errorName = exception.errorCodeName
        val exceptionClass = exception.javaClass.name
        val causeChain = getExceptionCauseChain(exception).joinToString("\n")
        
        var deepest: Throwable = exception
        while (deepest.cause != null) {
            deepest = deepest.cause!!
        }
        val stackTrace = "${deepest.javaClass.name}: ${deepest.message}\n" +
                deepest.stackTrace.take(8).joinToString("\n") { "  at $it" }.take(2000)

        val playId = playbackId ?: NA
        val contentId = content?.id?.toString() ?: NA

        val deviceManufacturer = Build.MANUFACTURER?.takeIf { it.isNotBlank() } ?: NA
        val deviceModel = Build.MODEL?.takeIf { it.isNotBlank() } ?: NA
        val androidVer = "${Build.VERSION.RELEASE ?: NA} (SDK ${Build.VERSION.SDK_INT})"
        val exoVer = ExoPlayerLibraryInfo.VERSION.takeIf { it.isNotBlank() } ?: NA

        val drmEnv = getWidevineEnvironment()
        val drmErr = getDrmErrorSummary(exception)
        val netErr = getNetworkErrorSummary(exception)
        val httpStatus = getHttpStatusSummary(exception)
        val licenseHost = getLicenseHostSummary(exception)

        sb.append("Error: ").append(errorCode).append("\n")
        sb.append("Name: ").append(errorName).append("\n")
        sb.append("Exception: ").append(exceptionClass).append("\n")
        sb.append("Cause Chain:\n").append(causeChain).append("\n")
        sb.append("Stack Trace:\n").append(stackTrace).append("\n\n")
        sb.append("Playback ID: ").append(playId).append("\n")
        sb.append("Content ID: ").append(contentId).append("\n")
        sb.append("L3 Fallback Attempted: ").append(isL3FallbackAttempted).append("\n\n")

        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US)
        dateFormat.timeZone = TimeZone.getDefault()
        val timeString = dateFormat.format(Date())
        var connectivityStr = NA
        if (context != null) {
            try {
                val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val activeNetwork = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) cm.activeNetwork else null
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activeNetwork != null) {
                    val caps = cm.getNetworkCapabilities(activeNetwork)
                    if (caps != null) {
                        val isWifi = caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        val isCell = caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                        val isVpn = caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
                        val isMetered = !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
                        connectivityStr = "WiFi:$isWifi Cell:$isCell VPN:$isVpn Metered:$isMetered"
                    }
                } else {
                    connectivityStr = "Legacy/No active network info"
                }
            } catch (e: Exception) {
                connectivityStr = "Error reading connectivity"
            }
        }

        sb.append("Time: ").append(timeString).append("\n")
        sb.append("Network: ").append(connectivityStr).append("\n\n")

        sb.append("Device: ").append(deviceManufacturer).append(" ").append(deviceModel).append("\n")
        sb.append("Android: ").append(androidVer).append("\n")
        sb.append("ExoPlayer: ").append(exoVer).append("\n")
        sb.append("Widevine: ").append(drmEnv.availability).append("\n")
        sb.append("Security Level: ").append(drmEnv.securityLevel).append("\n\n")

        sb.append("DRM Error: ").append(drmErr).append("\n")
        if (drmEnv.summary.isNotBlank() && drmEnv.summary != NA) {
            sb.append("MediaDrm: ").append(drmEnv.summary).append("\n")
        }
        sb.append("Network Error: ").append(netErr).append("\n")
        sb.append("HTTP Status: ").append(httpStatus).append("\n")
        sb.append("License Host: ").append(licenseHost)

        if (lastProvisioningUrl != NA) {
            sb.append("\n\nDRM Phase: PROVISION_REQUEST\n")
            sb.append("Prov URL: ").append(lastProvisioningUrl).append("\n")
            sb.append("Prov Req Size: ").append(lastProvisioningRequestSize).append(" bytes\n")
            sb.append("Prov Time: ").append(lastProvisioningTimeMs).append(" ms\n")
            sb.append("Prov Resp Size: ").append(lastProvisioningResponseSize).append(" bytes\n")
            if (lastProvisioningException != NA) {
                sb.append("Prov Exception: ").append(lastProvisioningException).append("\n")
            }
        }

        return sb.toString()
    }

    // --- Device / DRM Environment Data ---

    data class WidevineEnvironment(
        val availability: String = NA,
        val securityLevel: String = NA,
        val vendor: String = NA,
        val version: String = NA,
        val description: String = NA,
        val algorithms: String = NA,
        val systemId: String = NA,
        val hdcpLevel: String = NA,
        val maxHdcpLevel: String = NA,
        val maxSessions: String = NA,
        val openSessions: String = NA,
        val oemCryptoApiVersion: String = NA,
        val summary: String = NA
    )

    fun getWidevineEnvironment(): WidevineEnvironment {
        return try {
            val supported = MediaDrm.isCryptoSchemeSupported(C.WIDEVINE_UUID)
            if (!supported) {
                return WidevineEnvironment(
                    availability = "Not Supported",
                    securityLevel = NA
                )
            }

            var mediaDrm: MediaDrm? = null
            try {
                mediaDrm = MediaDrm(C.WIDEVINE_UUID)

                val vendor = safeGetProperty(mediaDrm, MediaDrm.PROPERTY_VENDOR)
                val version = safeGetProperty(mediaDrm, MediaDrm.PROPERTY_VERSION)
                val description = safeGetProperty(mediaDrm, MediaDrm.PROPERTY_DESCRIPTION)
                val algorithms = safeGetProperty(mediaDrm, MediaDrm.PROPERTY_ALGORITHMS)
                val securityLevel = safeGetProperty(mediaDrm, "securityLevel")
                val systemId = safeGetProperty(mediaDrm, "systemId")
                val hdcpLevel = safeGetProperty(mediaDrm, "hdcpLevel")
                val maxHdcpLevel = safeGetProperty(mediaDrm, "maxHdcpLevel")
                val maxSessions = safeGetProperty(mediaDrm, "maxNumberOfSessions")
                val openSessions = safeGetProperty(mediaDrm, "numberOfOpenSessions")
                val oemCrypto = safeGetProperty(mediaDrm, "oemCryptoApiVersion")

                val summaryParts = mutableListOf<String>()
                if (vendor != NA) summaryParts.add("vendor=$vendor")
                if (version != NA) summaryParts.add("version=$version")
                if (systemId != NA) summaryParts.add("systemId=$systemId")
                if (hdcpLevel != NA) summaryParts.add("hdcp=$hdcpLevel")
                if (oemCrypto != NA) summaryParts.add("oemCrypto=$oemCrypto")

                WidevineEnvironment(
                    availability = "Available",
                    securityLevel = securityLevel,
                    vendor = vendor,
                    version = version,
                    description = description,
                    algorithms = algorithms,
                    systemId = systemId,
                    hdcpLevel = hdcpLevel,
                    maxHdcpLevel = maxHdcpLevel,
                    maxSessions = maxSessions,
                    openSessions = openSessions,
                    oemCryptoApiVersion = oemCrypto,
                    summary = if (summaryParts.isNotEmpty()) summaryParts.joinToString(", ") else NA
                )
            } finally {
                if (mediaDrm != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        mediaDrm.close()
                    } else {
                        @Suppress("DEPRECATION")
                        mediaDrm.release()
                    }
                }
            }
        } catch (t: Throwable) {
            WidevineEnvironment(
                availability = "Error checking Widevine: ${t.javaClass.simpleName} (${t.message ?: "unknown"})",
                securityLevel = NA
            )
        }
    }

    private fun safeGetProperty(mediaDrm: MediaDrm, propertyName: String): String {
        return try {
            val value = mediaDrm.getPropertyString(propertyName)
            if (value.isNullOrBlank()) NA else value
        } catch (e: Exception) {
            NA
        }
    }

    // --- Exception Traversal & Diagnostics ---

    data class DrmExceptionInfo(
        val summary: String = NA,
        val diagnosticInfo: String = NA,
        val cryptoErrorCode: String = NA,
        val licenseHostPath: String = NA
    )

    data class NetworkExceptionInfo(
        val httpStatus: String = NA,
        val networkError: String = NA,
        val requestHostPath: String = NA
    )

    private fun getExceptionCauseChain(throwable: Throwable): List<String> {
        val list = mutableListOf<String>()
        var current = throwable.cause
        var depth = 1
        val seen = mutableSetOf<Throwable>()

        while (current != null && depth <= 12 && seen.add(current)) {
            list.add("  -> [#$depth] ${current.javaClass.name}: ${current.message ?: "(no message)"}")
            current = current.cause
            depth++
        }
        return list
    }

    private fun getRootCauseSummary(exception: PlaybackException): String {
        val cause = exception.cause ?: return NA
        var current: Throwable? = cause
        var deepest: Throwable = cause
        val seen = mutableSetOf<Throwable>()

        while (current != null && seen.add(current)) {
            deepest = current
            current = current.cause
        }

        val msg = deepest.message?.takeIf { it.isNotBlank() }
        return if (msg != null) {
            "${deepest.javaClass.simpleName}: $msg"
        } else {
            deepest.javaClass.name
        }
    }

    private fun getDrmErrorSummary(exception: PlaybackException): String {
        val info = getDetailedDrmExceptionInfo(exception)
        return info.summary
    }

    fun getDetailedDrmExceptionInfo(exception: PlaybackException): DrmExceptionInfo {
        var summary = NA
        var diagnosticInfo = NA
        var cryptoErrorCode = NA
        var licenseHostPath = NA

        var current: Throwable? = exception
        val seen = mutableSetOf<Throwable>()

        while (current != null && seen.add(current)) {
            when (current) {
                is MediaCodec.CryptoException -> {
                    val codeName = getCryptoErrorCodeName(current.errorCode)
                    cryptoErrorCode = "${current.errorCode} ($codeName)"
                    val diag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        try {
                            @Suppress("AnnotateVersionCheck")
                            val field = current.javaClass.getMethod("getDiagnosticInfo").invoke(current) as? String
                            field ?: NA
                        } catch (e: Exception) {
                            NA
                        }
                    } else NA
                    diagnosticInfo = diag
                    summary = "CryptoException: code=${current.errorCode} ($codeName) ${current.message ?: ""}".trim()
                }

                is MediaDrm.MediaDrmStateException -> {
                    val diag = current.diagnosticInfo.takeIf { !it.isNullOrBlank() } ?: NA
                    diagnosticInfo = diag
                    summary = "MediaDrmStateException: $diag".trim()
                }

                is NotProvisionedException -> {
                    summary = "NotProvisionedException: Device DRM requires provisioning"
                }

                is DeniedByServerException -> {
                    summary = "DeniedByServerException: DRM server denied license request"
                }

                is ResourceBusyException -> {
                    summary = "ResourceBusyException: DRM resources busy or max sessions exceeded"
                }

                is MediaDrmCallbackException -> {
                    val hostPath = extractHostAndPath(current.dataSpec.uri.toString())
                    licenseHostPath = hostPath
                    val headersStr = current.responseHeaders.filterKeys { key ->
                        key != null && !key.equals("Authorization", true) && !key.equals("Cookie", true) && !key.equals("Set-Cookie", true)
                    }.map { "${it.key}: ${it.value.joinToString(",")}" }.joinToString(" | ")
                    if (summary == NA) {
                        summary = "MediaDrmCallbackException: License fetch failed for $hostPath. Headers: $headersStr"
                    }
                }

                is DrmSession.DrmSessionException -> {
                    if (summary == NA) {
                        summary = "DrmSessionException: ${current.message ?: "DRM session error"}"
                    }
                }
            }
            current = current.cause
        }

        return DrmExceptionInfo(
            summary = summary,
            diagnosticInfo = diagnosticInfo,
            cryptoErrorCode = cryptoErrorCode,
            licenseHostPath = licenseHostPath
        )
    }

    private fun getNetworkErrorSummary(exception: PlaybackException): String {
        val info = getDetailedNetworkExceptionInfo(exception)
        return info.networkError
    }

    private fun getHttpStatusSummary(exception: PlaybackException): String {
        val info = getDetailedNetworkExceptionInfo(exception)
        return info.httpStatus
    }

    private fun getLicenseHostSummary(exception: PlaybackException): String {
        val drmInfo = getDetailedDrmExceptionInfo(exception)
        if (drmInfo.licenseHostPath != NA) {
            return drmInfo.licenseHostPath
        }
        val netInfo = getDetailedNetworkExceptionInfo(exception)
        return netInfo.requestHostPath
    }

    fun getDetailedNetworkExceptionInfo(exception: PlaybackException): NetworkExceptionInfo {
        var httpStatus = NA
        var networkError = NA
        var requestHostPath = NA

        var current: Throwable? = exception
        val seen = mutableSetOf<Throwable>()

        while (current != null && seen.add(current)) {
            when (current) {
                is HttpDataSource.InvalidResponseCodeException -> {
                    val statusMsg = current.responseMessage?.takeIf { it.isNotBlank() } ?: ""
                    httpStatus = "${current.responseCode} $statusMsg".trim()
                    val hostPath = extractHostAndPath(current.dataSpec.uri.toString())
                    requestHostPath = hostPath
                    
                    val headersStr = current.headerFields.filterKeys { key ->
                        key != null && !key.equals("Authorization", true) && !key.equals("Cookie", true) && !key.equals("Set-Cookie", true)
                    }.map { "${it.key}: ${it.value.joinToString(",")}" }.joinToString(" | ")
                    
                    val bodyBytes = try {
                        val field = current.javaClass.getField("responseBody")
                        field.get(current) as? ByteArray
                    } catch (e: Exception) { null }
                    
                    val bodyStr = if (bodyBytes != null && bodyBytes.isNotEmpty()) String(bodyBytes).take(300) else "N/A"
                    
                    networkError = "HTTP ${current.responseCode}: ${current.message ?: "Invalid response code"}\nHeaders: $headersStr\nBody: $bodyStr"
                }

                is HttpDataSource.InvalidContentTypeException -> {
                    networkError = "Invalid Content-Type: ${current.contentType}"
                    requestHostPath = extractHostAndPath(current.dataSpec.uri.toString())
                }

                is HttpDataSource.CleartextNotPermittedException -> {
                    networkError = "Cleartext HTTP traffic disallowed by device policy"
                    requestHostPath = extractHostAndPath(current.dataSpec.uri.toString())
                }

                is HttpDataSource.HttpDataSourceException -> {
                    val typeName = getHttpDataSourceErrorTypeName(current.type)
                    if (networkError == NA) {
                        networkError = "HttpDataSourceException: type=${current.type} ($typeName) ${current.message ?: ""}".trim()
                    }
                    if (requestHostPath == NA) {
                        requestHostPath = extractHostAndPath(current.dataSpec.uri.toString())
                    }
                }

                is UnknownHostException -> {
                    if (networkError == NA) {
                        networkError = "UnknownHostException: Unable to resolve host '${current.message ?: NA}'"
                    }
                }

                is SocketTimeoutException -> {
                    if (networkError == NA) {
                        networkError = "SocketTimeoutException: Connection/read timed out"
                    }
                }

                is ConnectException -> {
                    if (networkError == NA) {
                        networkError = "ConnectException: Failed to connect to server"
                    }
                }

                is SSLException -> {
                    if (networkError == NA) {
                        networkError = "SSLException: ${current.message ?: "TLS/SSL negotiation failed"}"
                    }
                }
            }
            current = current.cause
        }

        return NetworkExceptionInfo(
            httpStatus = httpStatus,
            networkError = networkError,
            requestHostPath = requestHostPath
        )
    }

    // --- Helpers: Sanitization and Mapping ---

    fun sanitizeUrl(rawUrl: String?): String {
        if (rawUrl.isNullOrBlank()) return NA
        return try {
            val uri = Uri.parse(rawUrl)
            val scheme = uri.scheme ?: ""
            val host = uri.host ?: ""
            val path = uri.path ?: ""
            if (host.isNotBlank()) {
                val prefix = if (scheme.isNotBlank()) "$scheme://" else ""
                "$prefix$host$path"
            } else if (path.isNotBlank()) {
                path
            } else {
                NA
            }
        } catch (e: Exception) {
            NA
        }
    }

    fun extractHostAndPath(rawUrl: String?): String {
        if (rawUrl.isNullOrBlank()) return NA
        return try {
            val uri = Uri.parse(rawUrl)
            val host = uri.host ?: ""
            val path = uri.path ?: ""
            if (host.isNotBlank()) {
                "$host$path"
            } else if (path.isNotBlank()) {
                path
            } else {
                NA
            }
        } catch (e: Exception) {
            NA
        }
    }

    private fun getCryptoErrorCodeName(errorCode: Int): String {
        return when (errorCode) {
            MediaCodec.CryptoException.ERROR_NO_KEY -> "ERROR_NO_KEY"
            MediaCodec.CryptoException.ERROR_KEY_EXPIRED -> "ERROR_KEY_EXPIRED"
            MediaCodec.CryptoException.ERROR_RESOURCE_BUSY -> "ERROR_RESOURCE_BUSY"
            MediaCodec.CryptoException.ERROR_INSUFFICIENT_OUTPUT_PROTECTION -> "ERROR_INSUFFICIENT_OUTPUT_PROTECTION"
            MediaCodec.CryptoException.ERROR_SESSION_NOT_OPENED -> "ERROR_SESSION_NOT_OPENED"
            MediaCodec.CryptoException.ERROR_UNSUPPORTED_OPERATION -> "ERROR_UNSUPPORTED_OPERATION"
            MediaCodec.CryptoException.ERROR_INSUFFICIENT_SECURITY -> "ERROR_INSUFFICIENT_SECURITY"
            MediaCodec.CryptoException.ERROR_FRAME_TOO_LARGE -> "ERROR_FRAME_TOO_LARGE"
            MediaCodec.CryptoException.ERROR_LOST_STATE -> "ERROR_LOST_STATE"
            else -> "UNKNOWN_CRYPTO_ERROR ($errorCode)"
        }
    }

    private fun getHttpDataSourceErrorTypeName(type: Int): String {
        return when (type) {
            HttpDataSource.HttpDataSourceException.TYPE_OPEN -> "TYPE_OPEN"
            HttpDataSource.HttpDataSourceException.TYPE_READ -> "TYPE_READ"
            HttpDataSource.HttpDataSourceException.TYPE_CLOSE -> "TYPE_CLOSE"
            else -> "TYPE_UNSPECIFIED"
        }
    }

    private fun htmlEscape(text: String): String {
        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }
}
