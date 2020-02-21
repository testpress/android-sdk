package `in`.testpress.course.ui.view_models

import `in`.testpress.course.models.Resource
import `in`.testpress.course.repository.ContentRepository
import `in`.testpress.models.greendao.Content
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel

class ContentViewModel(val repository: ContentRepository): ViewModel() {
    fun getContent(contentId: Int, forceRefresh:Boolean=false): LiveData<Resource<Content>> {
        return repository.loadContent(contentId, forceRefresh)
    }

    fun getChapterContents(chapterId: Long): List<Content> {
        return repository.getChapterContentsFromDB(chapterId)
    }
}
