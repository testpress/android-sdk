package `in`.testpress.util

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.core.os.bundleOf
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger

class FBEventsTrackerFacade(val context: Context) {
    private val logger: AppEventsLogger = AppEventsLogger.newLogger(context)


    companion object {
        fun initFacebook(applicationId: String, application: Application) {
            FacebookSdk.setApplicationId(applicationId);
            AppEventsLogger.activateApp(application);
        }
    }

    fun logEvent(name: String, params: HashMap<String, Any>) {
        val bundle = generateBundle(params)
        logger.logEvent(name, bundle)
    }

    private fun generateBundle(params: HashMap<String, Any>): Bundle {
        val paramsList = mutableListOf<Pair<String, Any>>()
        for ((key, value) in params) {
            paramsList.add(Pair(key, value))
        }
        paramsList.toTypedArray()
        return bundleOf(*paramsList.toTypedArray())
    }
}