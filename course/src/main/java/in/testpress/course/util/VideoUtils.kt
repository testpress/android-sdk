package `in`.testpress.course.util

import com.google.android.exoplayer2.source.TrackGroup
import com.google.android.exoplayer2.source.TrackGroupArray

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
}