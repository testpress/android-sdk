package `in`.testpress.network

import `in`.testpress.core.TestpressSdk
import `in`.testpress.core.TestpressSession
import `in`.testpress.core.database.DbTestMixin
import `in`.testpress.database.entities.CategoryEntity
import `in`.testpress.database.entities.DiscussionPostEntity
import `in`.testpress.database.entities.UserEntity
import `in`.testpress.models.DiscussionsMediator
import `in`.testpress.models.InstituteSettings
import android.content.Context
import androidx.paging.*
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@ExperimentalPagingApi
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class DiscussionsMediatorTest: DbTestMixin() {

    @Before
    @Throws(IOException::class)
    fun setUp() {
        val instituteSettings = InstituteSettings("http://localhost:9200")
        TestpressSdk.setTestpressSession(ApplicationProvider.getApplicationContext<Context>(),
                TestpressSession(instituteSettings, "aef"))
    }


    @Test
    fun endOfPaginationShouldNotBeReachedIfNextPageIsAvailable() = runBlocking {
        val mediator = DiscussionsMediator(
                FakeAPIClient(ApplicationProvider.getApplicationContext(), true),
                db
        )
        val result = mediator.load(LoadType.REFRESH, getPagingState())

        assertFalse((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
    }

    @Test
    fun endOfPaginationShouldBeReachedIfNextPageIsNotAvailable() = runBlocking {
        val mediator = DiscussionsMediator(
                FakeAPIClient(ApplicationProvider.getApplicationContext()),
                db
        )
        val result = mediator.load(LoadType.REFRESH, getPagingState())

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertTrue((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
    }

    @Test
    fun errorShouldBeReturnedForAPIFailure() = runBlocking {
        val apiClient = FakeAPIClient(ApplicationProvider.getApplicationContext())
        apiClient.failureMessage = "Unable to load data"
        val mediator = DiscussionsMediator(
                apiClient,
                db
        )
        val result = mediator.load(LoadType.REFRESH, getPagingState())
        assertTrue(result is RemoteMediator.MediatorResult.Error)
    }

    private fun getPagingState() = PagingState<Int, DiscussionPostEntity>(
            listOf(),
            null,
            PagingConfig(10),
            10
    )
}