package `in`.testpress.course.viewmodels

import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.domain.DomainLanguage
import `in`.testpress.course.network.NetworkContentAttempt
import `in`.testpress.course.network.Resource
import `in`.testpress.course.repository.ContentRepository
import `in`.testpress.course.repository.ExamContentRepository
import `in`.testpress.models.greendao.Content
import `in`.testpress.models.greendao.CourseAttempt
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class ContentViewModel(
    val repository: ContentRepository,
    val examRepository: ExamContentRepository
) : ViewModel() {

    fun getContent(
        contentId: Long,
        forceRefresh: Boolean = false
    ): LiveData<Resource<DomainContent>> {
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

    fun getContentFromDB(contentId: Long): Content? {
        return repository.getContentFromDB(contentId)
    }

    fun loadAttempts(url: String, contentId: Long): LiveData<Resource<ArrayList<NetworkContentAttempt>>> {
        return examRepository.loadAttempts(url, contentId)
    }

    fun getContentAttemptsFromDB(contentId: Long): List<CourseAttempt> {
        return examRepository.getContentAttemptsFromDB(contentId)
    }

    fun getLanguages(examSlug: String, examId: Long): LiveData<Resource<List<DomainLanguage>>> {
        return examRepository.fetchLanguages(examSlug, examId)
    }
}