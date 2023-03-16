package `in`.testpress.course.pagination

import androidx.paging.*
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import `in`.testpress.core.TestpressSdk
import `in`.testpress.core.TestpressSession
import `in`.testpress.course.util.FakeAPIClient
import `in`.testpress.course.util.TestDatabase
import `in`.testpress.database.entities.RunningContentEntity
import `in`.testpress.models.InstituteSettings
import junit.framework.Assert
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@ExperimentalPagingApi
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class RunningContentRemoteMediatorTest: TestDatabase() {

    @Before
    @Throws(IOException::class)
    fun setUp() {
        val instituteSettings = InstituteSettings("http://localhost:9200")
        TestpressSdk.setTestpressSession(
            ApplicationProvider.getApplicationContext(),
            TestpressSession(instituteSettings, "aef")
        )
    }

    @Test
    fun endOfPaginationShouldNotBeReachedIfNextPageIsAvailable() = runBlocking {
        val mediator = RunningContentRemoteMediator(
            FakeAPIClient(ApplicationProvider.getApplicationContext(), true),
            database,1
        )
        val result = mediator.load(LoadType.REFRESH, getPagingState())

        Assert.assertFalse((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
    }

    @Test
    fun endOfPaginationShouldBeReachedIfNextPageIsNotAvailable() = runBlocking {
        val mediator = RunningContentRemoteMediator(
            FakeAPIClient(ApplicationProvider.getApplicationContext(), true),
            database,1
        )
        val result = mediator.load(LoadType.REFRESH, getPagingState())

        Assert.assertTrue(result is RemoteMediator.MediatorResult.Success)
        Assert.assertTrue((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
    }

    @Test
    fun errorShouldBeReturnedForAPIFailure() = runBlocking {
        val apiClient = FakeAPIClient(ApplicationProvider.getApplicationContext())
        apiClient.failureMessage = "Unable to load data"
        val mediator = RunningContentRemoteMediator(
            apiClient,
            database,1
        )
        val result = mediator.load(LoadType.REFRESH, getPagingState())
        Assert.assertTrue(result is RemoteMediator.MediatorResult.Error)
    }

    private fun getPagingState() = PagingState<Int, RunningContentEntity>(
        listOf(),
        null,
        PagingConfig(15),
        10
    )
}