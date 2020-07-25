package `in`.testpress.course.util

import com.google.android.exoplayer2.Format
import com.google.android.exoplayer2.ui.TrackNameProvider

class ExoPlayerTrackNameProvider : TrackNameProvider {
    override fun getTrackName(format: Format): String {
        return when {
            format.width <= 240 -> "Very Low"
            format.width <= 360 -> "Low"
            format.width <= 480 -> "High"
            format.width <= 540 -> "High"
            format.width <= 720 -> "HD"
            format.width <= 1080 -> "HD1080"
            else -> "${format.width}p"
        }
    }
}