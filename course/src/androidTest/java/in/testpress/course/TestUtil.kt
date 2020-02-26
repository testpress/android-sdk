package `in`.testpress.course

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


fun <T> LiveData<T>.getAbsoluteValue(): T {
    val data = arrayOfNulls<Any>(1)
    val latch = CountDownLatch(1)
    val observer = object : Observer<T> {
        override fun onChanged(o: T?) {
            data[0] = o
            latch.countDown()
            this@getAbsoluteValue.removeObserver(this)
        }
    }
    this.observeForever(observer)
    latch.await(2, TimeUnit.SECONDS)

    @Suppress("UNCHECKED_CAST")
    return data[0] as T
}