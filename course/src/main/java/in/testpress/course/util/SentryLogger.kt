package `in`.testpress.course.util

import android.media.MediaCodecList
import com.google.android.exoplayer2.PlaybackException
import io.sentry.Scope
import io.sentry.Sentry
import io.sentry.SentryLevel

fun logPlaybackException(
    username: String,
    packageName: String,
    contentId: Long,
    errorMessage: String,
    playbackId: String,
    exception: PlaybackException
) {
    val extraInfo = mutableMapOf<String, Any?>(
        "Content Id" to contentId,
        "Playback Id" to playbackId,
        "Error Code" to exception.errorCode,
        "Player Error Message" to errorMessage,
        "Exception Error Message" to exception.message,
        "Error Cause" to (exception.cause?.toString() ?: "Cause not found")
    )

    if (exception.errorCode == PlaybackException.ERROR_CODE_DECODING_FAILED) {
        extraInfo["Codec Details"] = runCatching { getAVCCodecSupportInfo() }
            .getOrElse { ex -> "Failed to get codec info: ${ex.message}" }
    }

    Sentry.captureException(exception) { scope: Scope ->
        scope.level = SentryLevel.ERROR
        scope.setTag("playback_id", playbackId)
        scope.setTag("package_name", packageName)
        scope.setTag("user_name", username)
        scope.setContexts("Player Error", extraInfo)
    }
}

private fun getAVCCodecSupportInfo(): Map<String, Map<String, Any>> {
    val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)
    return codecList.codecInfos
        .filter { !it.isEncoder && "avc" in it.name.lowercase() }
        .associate { codec ->
            val capabilities = codec.getCapabilitiesForType("video/avc")
            val videoCapabilities = capabilities.videoCapabilities
            val profileLevels = capabilities.profileLevels?.map {
                mapOf(
                    "Profile" to it.profile,
                    "Level" to it.level
                )
            } ?: emptyList()

            codec.name to mapOf(
                "Width Alignment" to videoCapabilities.widthAlignment,
                "Height Alignment" to videoCapabilities.heightAlignment,
                "Supported Widths" to videoCapabilities.supportedWidths.toString(),
                "Supported Heights" to videoCapabilities.supportedHeights.toString(),
                "Supported Profiles & Levels" to profileLevels
            )
        }
}