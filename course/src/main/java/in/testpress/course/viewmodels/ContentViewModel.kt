package `in`.testpress.course.viewmodels

import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.network.NetworkContentAttempt
import `in`.testpress.course.repository.ContentRepository
import `in`.testpress.network.Resource
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel


open class ContentViewModel(open val repository: ContentRepository) : ViewModel() {
    fun getContent(contentId: Long,
                 forceRefresh: Boolean = false): LiveData<Resource<DomainContent>> {
        return repository.loadContent(contentId, forceRefresh)
    }

    fun getContentInChapterForPosition(position: Int, chapterId: Long): LiveData<DomainContent> {
        return repository.getContentInChapterForPosition(position, chapterId)
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