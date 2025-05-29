package `in`.testpress.course.util.extension

import androidx.compose.foundation.clickable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

/**
 * Creates a clickable modifier with debounce functionality to prevent rapid successive clicks.
 *
 * @param debounceDuration The minimum time interval (in milliseconds) between consecutive clicks.
 *                         Defaults to 500ms.
 * @param timeProvider A lambda that provides the current time in milliseconds.
 *                     Defaults to [System.currentTimeMillis]. Can be overridden for testing or
 *                     custom time sources.
 * @param onClick The callback to invoke when a valid (non-debounced) click occurs.
 * @return A [Modifier] that handles debounced click events.
 *
 * Example usage:
 * @sample in.testpress.course.util.extension.SampleDebouncedClickable
 *
 * [materialize] must be called to create instance-specific modifiers if you are directly
 * applying a [Modifier] to an element tree node.
 */
fun Modifier.debouncedClickable(
    debounceDuration: Long = 500L,
    timeProvider: () -> Long = { System.currentTimeMillis() },
    onClick: () -> Unit
) = composed {
    var lastClickTime by remember { mutableLongStateOf(0L) }

    this.clickable {
        val currentTime = timeProvider()
        if (currentTime - lastClickTime >= debounceDuration) {
            lastClickTime = currentTime
            onClick()
        }
    }
}