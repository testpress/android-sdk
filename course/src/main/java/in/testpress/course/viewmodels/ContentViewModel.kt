package `in`.testpress.course.viewmodels

import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.network.NetworkContentAttempt
import `in`.testpress.course.network.Resource
import `in`.testpress.course.repository.ContentRepository
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel


class ContentViewModel(val repository: ContentRepository) : ViewModel() {
    fun getContent(contentId: Long,
                 forceRefresh: Boolean = false): LiveData<Resource<DomainContent>> {
        return repository.loadContent(contentId, forceRefresh)
    }

    fun getContent(position: Int, chapterId: Long): DomainContent {
        return repository.getContent(position, chapterId)
    }

    fun getContentsForChapter(chapterId: Long): LiveData<List<DomainContent>>? {
        return repository.getContentsForChapterFromDB(chapterId)
    }

    fun createContentAttempt(contentId: Long): LiveData<Resource<NetworkContentAttempt>> {
        return repository.createContentAttempt(contentId)
    }

    fun storeBookmarkIdToContent(bookmarkId: Long?, contentId: Long) {
        return repository.storeBookmarkIdToContent(bookmarkId, contentId)
    }
}