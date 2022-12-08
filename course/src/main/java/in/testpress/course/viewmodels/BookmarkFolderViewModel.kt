package `in`.testpress.course.viewmodels

import `in`.testpress.course.repository.BookmarkFolderRepository
import androidx.lifecycle.ViewModel

class BookmarkFolderViewModel(val repository: BookmarkFolderRepository) : ViewModel() {

    val folders = repository.resourceBookmarkFolders

    val deleteFolder = repository.deleteBookmarkFolders

    val updateFolder = repository.updateBookmarkFolders

    fun loadFolders(url: String) {
        repository.loadBookmarkFolders(url)
    }

    fun deleteFolder(folderId: Long) {
        repository.deleteFolder(folderId)
    }

    fun updateFolder(folderId: Long, folderName: String) {
        repository.updateBookmarkFolder(folderId, folderName)
    }

}