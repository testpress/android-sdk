package `in`.testpress.exam.repository

import `in`.testpress.database.dao.CommentDao
import `in`.testpress.exam.network.service.CommentApiClient
import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

class CommentRepositoryTest {

    @Mock
    lateinit var commentDao: CommentDao

    @Mock
    lateinit var commentApiClient: CommentApiClient

    @Mock
    lateinit var context: Context

    private lateinit var repository: CommentRepository

    private lateinit var spy: CommentRepository

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        repository = CommentRepository(commentDao, commentApiClient)
        spy = spy(repository)
    }

    @Test
    fun whenDbDataIsAvailableReturnDataImmediately() {
        repository.getComments(false, "url")
        verify(spy).getCommentFromDB()
    }

    @Test
    fun whenForceFetchFalseReturnDataImmediately() {
        repository.getComments(false, "url")
        verify(spy).getCommentFromDB()
    }

    @Test
    fun whenNetworkFetchedSaveResponseToDb() {
        repository.getComments(true, "url")
        verify(spy).saveCommentToDb(item = listOf())
    }
}
