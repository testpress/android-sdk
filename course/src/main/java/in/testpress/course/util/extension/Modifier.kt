package `in`.testpress.course.util.extension

import androidx.compose.foundation.clickable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

fun Modifier.debouncedClickable(
    debounceDuration: Long = 500L,
    onClick: () -> Unit
) = composed {
    var lastClickTime by remember { mutableLongStateOf(0L) }

    this.clickable {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime >= debounceDuration) {
            lastClickTime = currentTime
            onClick()
        }
    }
}