package `in`.testpress.course.view_models

import `in`.testpress.course.repository.ContentRepository
import `in`.testpress.course.ui.view_models.ContentViewModel
import android.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify


@RunWith(JUnit4::class)
class ContentViewModelTest {
    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()
    private val repository = mock(ContentRepository::class.java)
    private val repoViewModel = ContentViewModel(repository)

    @Test
    fun testGetChapterContents() {
        repoViewModel.getChapterContents(1)
        verify(repoViewModel.repository).getChapterContentsFromDB(1)
    }

    @Test
    fun testGetContent() {
        repoViewModel.getContent(1)
        verify(repoViewModel.repository).loadContent(1)
    }
}