package `in`.testpress.util.extension

import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher

fun OnBackPressedCallback.passThrough(dispatcher: OnBackPressedDispatcher) {
    try {
        isEnabled = false
        dispatcher.onBackPressed()
    } finally {
        isEnabled = true
    }
}
