package `in`.testpress.util

import `in`.testpress.core.TestpressSdk
import `in`.testpress.models.InstituteSettings
import android.app.Application
import android.content.Context
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger

class EventsTrackerFacade(val context: Context) {
    val instituteSettings: InstituteSettings = TestpressSdk.getTestpressSession(context)!!.instituteSettings;
    private val fbEventsTrackerFacade = FBEventsTrackerFacade(context)
    private val firebaseEventsTrackerFacade = FirebaseEventsTrackerFacade(context)

    companion object {
        const val ACCOUNT_REGISTERED = "Account Registered"
        const val VIEWED_POST_EVENT = "Viewed Post"
        const val VIEWED_NOTES_EVENT = "Viewed Notes"
        const val VIEWED_PRODUCT_EVENT = "Viewed Product"
        const val CLICKED_BUY_NOW = "Clicked Buy Now"
        const val PAYMENT_SUCCESS = "Payment Success"
        const val PAYMENT_FAILURE = "Payment Failure"
        const val CANCELLED_PAYMENT = "Cancelled Payment"
        const val STARTED_EXAM = "Started Exam"
        const val ENDED_EXAM = "Ended Exam"

        fun init(application: Application) {
            val instituteSettings: InstituteSettings = TestpressSdk.getTestpressSession(application)!!.instituteSettings

            if(instituteSettings.isFacebookEventTrackingEnabled) {
                FacebookSdk.setApplicationId(instituteSettings.facebookAppId)
                AppEventsLogger.activateApp(application)
            }
        }
    }

    fun logEvent(name: String, params: HashMap<String, Any>) {
        if(instituteSettings.isFacebookEventTrackingEnabled) {
            fbEventsTrackerFacade.logEvent(name, params)
        }

        if (instituteSettings.isFirebaseEventTrackingEnabled) {
            firebaseEventsTrackerFacade.logEvent(name, params)
        }
    }
}