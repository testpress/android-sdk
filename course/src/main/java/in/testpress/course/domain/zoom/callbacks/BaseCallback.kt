package `in`.testpress.course.domain.zoom.callbacks


open class BaseCallback<T : BaseEvent?> {
    protected var callbacks: MutableList<T> = ArrayList()

    fun addListener(event: T) {
        if (!callbacks.contains(event)) {
            callbacks.add(event)
        }
    }

    fun removeListener(event: T) {
        callbacks.remove(event)
    }
}

interface BaseEvent
