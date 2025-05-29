package `in`.testpress.course.util.extension

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SampleDebouncedClickable() {
    Row(
        modifier = Modifier
            .debouncedClickable(debounceDuration = 500, onClick = {  }),
    ) {
        Text(text = "Click Me")
    }
}