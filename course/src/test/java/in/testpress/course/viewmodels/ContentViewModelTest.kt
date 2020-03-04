package `in`.testpress.course.viewmodels

import `in`.testpress.course.repository.ContentRepository
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ContentViewModelTest {
    private val repository = mock(ContentRepository::class.java)
    private val viewModel = ContentViewModel(repository)

    @Test
    fun getContentsCallsRepository() {
        viewModel.getContent(1)
        verify(repository, times(1)).loadContent(1)
    }

    @Test
    fun getChapterContentsCallsRepository() {
        viewModel.getContentsForChapter(1)
        verify(repository, times(1)).getContentsForChapterFromDB(1)
    }

    @Test
    fun createContentAttemptShouldCallRepository() {
        viewModel.createContentAttempt(1)
        verify(repository).createContentAttempt(1)
    }

    @Test
    fun getContentWithPositionAndChapterId() {
        viewModel.getContentInChapterForPosition(1, 2)
        verify(repository).getContent(1, 2)
    }

    @Test
    fun storeBookmarkIdToContentCallsRepository() {
        viewModel.storeBookmarkIdToContent(1, 2)
        verify(repository).storeBookmarkIdToContent(1, 2)
    }
}