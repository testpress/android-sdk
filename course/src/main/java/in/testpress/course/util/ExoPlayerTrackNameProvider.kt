package `in`.testpress.course.util

import com.google.android.exoplayer2.Format
import com.google.android.exoplayer2.ui.TrackNameProvider

class ExoPlayerTrackNameProvider : TrackNameProvider {
    override fun getTrackName(format: Format): String {
        return when {
            format.height <= 240 -> "Very Low"
            format.height <= 360 -> "Low"
            format.height <= 480 -> "Medium"
            format.height <= 540 -> "High"
            format.height <= 720 -> "Very High"
            format.height <= 1080 -> "HD"
            else -> "${format.width}p"
        }
    }
}