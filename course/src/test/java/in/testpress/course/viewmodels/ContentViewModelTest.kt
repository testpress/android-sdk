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
}