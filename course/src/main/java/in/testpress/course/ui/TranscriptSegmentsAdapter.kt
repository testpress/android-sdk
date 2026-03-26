package `in`.testpress.course.ui

import `in`.testpress.course.util.TranscriptSegment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

class TranscriptSegmentsAdapter(
    private val onClick: (TranscriptSegment) -> Unit,
) : ListAdapter<TranscriptSegment, TranscriptSegmentViewHolder>(DiffCallback) {

    private var activeIndex: Int = -1

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): TranscriptSegmentViewHolder {
        return TranscriptSegmentViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: TranscriptSegmentViewHolder, position: Int) {
        holder.bind(
            segment = getItem(position),
            isActive = position == activeIndex,
            onClick = onClick,
        )
    }

    fun setActiveIndex(newIndex: Int) {
        if (newIndex == activeIndex) return
        val old = activeIndex
        activeIndex = newIndex
        if (old != -1) notifyItemChanged(old)
        if (newIndex != -1) notifyItemChanged(newIndex)
    }

    fun resetActiveIndex() {
        setActiveIndex(-1)
    }

    private object DiffCallback : DiffUtil.ItemCallback<TranscriptSegment>() {
        override fun areItemsTheSame(oldItem: TranscriptSegment, newItem: TranscriptSegment): Boolean {
            return oldItem.startTimeSeconds == newItem.startTimeSeconds &&
                oldItem.endTimeSeconds == newItem.endTimeSeconds &&
                oldItem.text == newItem.text
        }

        override fun areContentsTheSame(oldItem: TranscriptSegment, newItem: TranscriptSegment): Boolean {
            return oldItem == newItem
        }
    }
}

