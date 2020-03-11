package `in`.testpress.course.viewmodels

import `in`.testpress.course.repository.ContentRepository
import `in`.testpress.course.repository.ExamContentRepository
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ContentViewModelTest {
    private val repository = mock(ContentRepository::class.java)
    private val examRepository = mock(ExamContentRepository::class.java)
    private val viewModel = ContentViewModel(repository, examRepository)

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
        verify(repository).getContentInChapterForPosition(1, 2)
    }

    @Test
    fun storeBookmarkIdToContentCallsRepository() {
        viewModel.storeBookmarkIdToContent(1, 2)
        verify(repository).storeBookmarkIdToContent(1, 2)
    }

    @Test
    fun getContentFromDBShouldCallRepositoryMethod() {
        viewModel.getContentFromDB(1)
        verify(repository).getContentFromDB(1)
    }

    @Test
    fun loadAttemptsShouldCallExamRepositoryMethod() {
        viewModel.loadAttempts("url", 1)
        verify(examRepository).loadAttempts("url", 1)
    }

    @Test
    fun getContentAttemptsFromDBShouldCallExamRepositoryMethod() {
        viewModel.getContentAttemptsFromDB(1)
        verify(examRepository).getContentAttemptsFromDB(1)
    }

    @Test
    fun getLanguages() {
        viewModel.getLanguages("slug", 1)
        verify(examRepository).loadLanguages("slug", 1)
    }
}