package `in`.testpress.course.util

import com.google.android.exoplayer2.source.TrackGroup
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.Format
import com.google.android.exoplayer2.offline.DownloadHelper

object VideoUtils {
    @JvmStatic
    fun getLowBitrateTrackIndex(trackGroups: TrackGroupArray): Pair<Int, Int> {
        var lowBandwithTrackIndex = 0
        var lowBandwithGroupIndex = 0
        var lowestBitrate: Int = Integer.MAX_VALUE

        for (groupIndex in 0 until trackGroups.length) {
            val group: TrackGroup = trackGroups.get(groupIndex)
            for (trackIndex in 0 until group.length) {
                val trackInfo = group.getFormat(trackIndex)
                lowestBitrate = minOf(trackInfo.bitrate, lowestBitrate)
                if (trackInfo.bitrate == lowestBitrate) {
                    lowBandwithTrackIndex = trackIndex
                    lowBandwithGroupIndex = groupIndex
                }
            }
        }
        return Pair(lowBandwithTrackIndex, lowBandwithGroupIndex)
    }

    @JvmStatic
    fun getAudioOrVideoInfoWithDrmInitData(helper: DownloadHelper): Format? {
        for (periodIndex in 0 until helper.periodCount) {
            val mappedTrackInfo = helper.getMappedTrackInfo(periodIndex)
            for (rendererIndex in 0 until mappedTrackInfo.rendererCount) {
                val trackGroups = mappedTrackInfo.getTrackGroups(rendererIndex)
                for (trackGroupIndex in 0 until trackGroups.length) {
                    val trackGroup = trackGroups[trackGroupIndex]
                    for (formatIndex in 0 until trackGroup.length) {
                        val format = trackGroup.getFormat(formatIndex)
                        if (format.drmInitData != null) {
                            return format
                        }
                    }
                }
            }
        }
        return null
    }

    fun generatePlayerIdString(): String {
        return (1..10)
            .map { ('a'..'z').toList() + ('0'..'9').toList() }
            .map { it.random() }
            .joinToString("")
    }
}