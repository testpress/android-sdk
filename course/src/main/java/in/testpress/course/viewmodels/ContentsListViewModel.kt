package `in`.testpress.course.viewmodels

import `in`.testpress.course.repository.ContentsRepository
import androidx.lifecycle.ViewModel

open class ContentsListViewModel(val repository: ContentsRepository): ViewModel() {
    val items = repository.resourceContents

    fun loadContents() {
        return repository.loadItems()
    }
}