package `in`.testpress.course.viewmodels

import `in`.testpress.course.repository.BookmarksRepository
import androidx.lifecycle.ViewModel

class BookmarkListViewModel(val repository: BookmarksRepository) : ViewModel() {

    val items = repository.resourceBookmarks

    fun loadBookmarks() {
        return repository.loadData()
    }

}