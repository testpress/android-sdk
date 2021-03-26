package `in`.testpress.course.util

import `in`.testpress.util.TimeUtils.convertMilliSecondsToSeconds
import com.google.android.exoplayer2.analytics.AnalyticsListener

class ExoplayerAnalyticsListener(val videoTimeRangeListener: VideoTimeRangeListener): AnalyticsListener {
    var endPosition: Long = 0
    var startPosition: Long = 0

    override fun onSeekStarted(eventTime: AnalyticsListener.EventTime) {
        super.onSeekStarted(eventTime)
        endPosition = convertMilliSecondsToSeconds(eventTime.currentPlaybackPositionMs).toLong()

        if (startPosition < endPosition) {
            videoTimeRangeListener.onTimeRangeChange(startPosition, endPosition)
        }
    }

    override fun onPositionDiscontinuity(eventTime: AnalyticsListener.EventTime, reason: Int) {
        super.onPositionDiscontinuity(eventTime, reason)
        startPosition = convertMilliSecondsToSeconds(eventTime.currentPlaybackPositionMs).toLong()
    }
}

interface VideoTimeRangeListener {
    fun onTimeRangeChange(startTime: Long, endTime: Long)
}
