package `in`.testpress.course.viewmodels

import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.network.Resource
import `in`.testpress.course.repository.ContentRepository
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel


class ContentViewModel(val repository: ContentRepository) : ViewModel() {
    fun getContent(contentId: Long,
                 forceRefresh: Boolean = false): LiveData<Resource<DomainContent>> {
        return repository.loadContent(contentId, forceRefresh)
    }

    fun getContentsForChapter(chapterId: Long): LiveData<List<DomainContent>>? {
        return repository.getContentsForChapterFromDB(chapterId)
    }
}