package `in`.testpress.util

import android.content.Context
import android.os.Bundle
import com.facebook.appevents.AppEventsLogger

class FBEventsTrackerFacade(val context: Context) {
    private val logger: AppEventsLogger = AppEventsLogger.newLogger(context)

    fun logEvent(name: String, params: HashMap<String, Any>) {
        val bundle = generateBundle(params)
        logger.logEvent(name, bundle)
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