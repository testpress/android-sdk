package `in`.testpress.course.ui.callbacks


open class BaseCallback<T : BaseEvent?> {
    var callbacks: MutableList<T> = ArrayList()

    fun addListener(event: T) {
        if (!callbacks.contains(event)) {
            callbacks.add(event)
        }
    }

    fun removeListener(event: T) {
        callbacks.remove(event)
    }
}