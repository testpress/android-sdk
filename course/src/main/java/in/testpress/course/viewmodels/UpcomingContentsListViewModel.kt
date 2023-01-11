package `in`.testpress.course.viewmodels

import `in`.testpress.course.repository.UpcomingContentRepository
import androidx.lifecycle.ViewModel

class UpcomingContentsListViewModel(val repository: UpcomingContentRepository) : ViewModel() {
    val items = repository.resourceContents

    fun loadContents() {
        return repository.loadItems()
    }
}