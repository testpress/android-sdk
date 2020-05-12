package `in`.testpress.util

abstract class BaseEventTrackerFacade {
    abstract fun logEvent(name: String, params: HashMap<String, Any>)
}

enum class EventTracker {
    FB_EVENTS_TRACKER, BRANCH_EVENTS_TRACKER, FIREBASE_EVENTS_TRACKER
}