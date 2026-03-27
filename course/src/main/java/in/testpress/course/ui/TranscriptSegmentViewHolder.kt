package `in`.testpress.course.ui

import `in`.testpress.course.R
import `in`.testpress.course.databinding.TranscriptSegmentItemBinding
import `in`.testpress.course.domain.TranscriptSegment
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class TranscriptSegmentViewHolder private constructor(
    private val binding: TranscriptSegmentItemBinding,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(
        segment: TranscriptSegment,
        isActive: Boolean,
        onClick: (TranscriptSegment) -> Unit,
    ) {
        binding.time.text = formatTime(segment.startTimeSeconds)
        binding.text.text = segment.text
        binding.root.setBackgroundResource(
            if (isActive) R.drawable.transcript_item_active_background else R.drawable.transcript_item_background
        )
        binding.root.setOnClickListener { onClick(segment) }
    }

    private fun formatTime(seconds: Double): String {
        val totalSeconds = seconds.toInt().coerceAtLeast(0)
        val h = totalSeconds / 3600
        val m = (totalSeconds % 3600) / 60
        val s = totalSeconds % 60

        return if (h > 0) {
            "%d:%02d:%02d".format(h, m, s)
        } else {
            "%02d:%02d".format(m, s)
        }
    }

    companion object {
        fun create(parent: ViewGroup): TranscriptSegmentViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = TranscriptSegmentItemBinding.inflate(inflater, parent, false)
            return TranscriptSegmentViewHolder(binding)
        }
    }
}
