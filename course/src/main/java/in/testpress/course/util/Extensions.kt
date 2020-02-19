package `in`.testpress.course.util

import android.arch.lifecycle.MutableLiveData

public operator fun <T> MutableLiveData<ArrayList<T>>.plusAssign(values: List<T>) {
    val value = this.value ?: arrayListOf()
    value.addAll(values)
    this.value = value
}