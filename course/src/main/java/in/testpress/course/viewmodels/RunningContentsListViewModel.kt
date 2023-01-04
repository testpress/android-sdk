package `in`.testpress.course.viewmodels

import `in`.testpress.course.repository.RunningContentsRepository
import androidx.lifecycle.ViewModel

class RunningContentsListViewModel(val repository: RunningContentsRepository) : ViewModel() {
    val items = repository.resourceContents

    fun loadContents() {
        return repository.loadItems()
    }
}