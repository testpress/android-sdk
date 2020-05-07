package `in`.testpress.util

import android.content.Context
import android.os.Bundle
import androidx.core.os.bundleOf
import com.google.firebase.analytics.FirebaseAnalytics

class FirebaseEventsTrackerFacade(val context: Context) {
    private var firebaseAnalytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(context)

    fun logEvent(name: String, params: HashMap<String, Any>) {
        val bundle = generateBundle(params)
        val eventName = name.replace(" ", "_")
        firebaseAnalytics.logEvent(eventName, bundle)
    }

    private fun generateBundle(params: HashMap<String, Any>): Bundle {
        val bundle = Bundle()
        for ((key, value) in params) {
            bundle.apply {
                when(value) {
                    is Boolean -> putBoolean(key, value)
                    is Byte -> putByte(key, value)
                    is Char -> putChar(key, value)
                    is Double -> putDouble(key, value)
                    is Float -> putFloat(key, value)
                    is Int -> putInt(key, value)
                    is Long -> putLong(key, value)
                    is Short -> putShort(key, value)
                    else -> putString(key, value.toString())
                }
            }
        }
        return bundle
    }
}