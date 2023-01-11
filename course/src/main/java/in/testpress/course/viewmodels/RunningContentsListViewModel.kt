package `in`.testpress.course.viewmodels

import `in`.testpress.course.repository.RunningContentsRepository
import `in`.testpress.course.repository.UpcomingContentRepository
import androidx.lifecycle.ViewModel

class RunningContentsListViewModel(val repository: RunningContentsRepository) : ViewModel() {
    val items = repository.resourceContents

    fun loadContents() {
        return repository.loadItems()
    }
}

class UpcomingContentsListViewModel(val repository: UpcomingContentRepository) : ViewModel() {
    val items = repository.resourceContents

    fun loadContents() {
        return repository.loadItems()
    }
}