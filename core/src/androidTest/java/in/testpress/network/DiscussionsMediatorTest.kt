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

    val discussions = listOf<DiscussionPostEntity>(createDiscussion())
    fun createDiscussion(): DiscussionPostEntity {
        return DiscussionPostEntity(
                id = 6550,
                shortWebUrl = "https://brilliantpalalms.testpress.in/p/43p6c6/",
                shortUrl = "https://brilliantpalalms.testpress.in/api/v2.3/forum/43p6c6/",
                webUrl = "https://brilliantpalalms.testpress.in/posts/help-help-help/",
                created = "2021-06-23T16:25:50.473836Z",
                commentsUrl = "https://brilliantpalalms.testpress.in/api/v2.3/forum/6550/comments/",
                url = "https://brilliantpalalms.testpress.in/api/v2.3/forum/help-help-help/",
                modified = "2021-06-24T03:30:38.556042Z",
                upvotes = 0, downvotes = 0,
                title = "help help help",
                summary = "thermodynamics chemistry apply cheyyan ulla equations and important theoryum ulla short note aarengilu ayachtharuoo?",
                isActive = true,
                publishedDate = "2021-06-23T16:25:50.473836Z",
                commentsCount = 22, isLocked = false, subject = null,
                viewsCount = 104, participantsCount = 6,
                lastCommentedTime = "2021-06-23T16:46:07.810845Z",
                contentHtml = null, isPublic = null, shortLink = null,
                institute = null, slug = "help-help-help", isPublished = null,
                isApproved = null, forum = null, ipAddress = null, voteId = null,
                typeOfVote = null, published = null, modifiedDate = null, creatorId = null,
                commentorId = null, categoryId = null,
                createdBy = UserEntity(
                        id = 21307,
                        url = "https://brilliantpalalms.testpress.in/api/v2.3/users/21307/",
                        username = null, firstName = "appukuttan", lastName = "",
                        displayName = "appukuttan", photo = "https://static.testpress.in/institute/brilliantpalalms/user_profiles/21307/2cbfd9b0fe1b4766b0068c0a56eb8ecd.jpg",
                        largeImage = "https://media.testpress.in/institute/brilliantpalalms/user_profiles/21307/c3501ed886694fad97243486cd8ba544.jpeg",
                        mediumImage = "https://media.testpress.in/institute/brilliantpalalms/user_profiles/21307/b05124ac3e684b2c92225281478928f7.jpeg",
                        mediumSmallImage = "https://media.testpress.in/institute/brilliantpalalms/user_profiles/21307/b77b0de4c1d64991bc61ae5fc4eaa0f1.jpeg"
                ),
                category = CategoryEntity(id = 8, name = "CHEMISTRY", color = "ff0000", slug = "chemistry")
        )
    }

    @Before
    @Throws(IOException::class)
    fun setUp() {
        val instituteSettings = InstituteSettings("http://localhost:9200")
        TestpressSdk.setTestpressSession(ApplicationProvider.getApplicationContext<Context>(),
                TestpressSession(instituteSettings, "aef"))
    }


    @Test
    fun refreshLoadReturnsSuccessResultWhenMoreDataIsPresent() = runBlocking {
        val instituteSettings = InstituteSettings("http://google.com")
        TestpressSdk.setTestpressSession(ApplicationProvider.getApplicationContext<Context>(),
                TestpressSession(instituteSettings, "aef"))
        val mediator = DiscussionsMediator(
                FakeAPIClient(ApplicationProvider.getApplicationContext(), true),
                db
        )
        val pagingState = PagingState<Int, DiscussionPostEntity>(
                listOf(),
                null,
                PagingConfig(10),
                10
        )
        val result = mediator.load(LoadType.REFRESH, pagingState)

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertFalse((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
    }

    @Test
    fun refreshLoadSuccessAndEndOfPaginationWhenNoMoreData() = runBlocking {
        val instituteSettings = InstituteSettings("http://google.com")
        TestpressSdk.setTestpressSession(ApplicationProvider.getApplicationContext<Context>(),
                TestpressSession(instituteSettings, "aef"))
        val mediator = DiscussionsMediator(
                FakeAPIClient(ApplicationProvider.getApplicationContext()),
                db
        )
        val pagingState = PagingState<Int, DiscussionPostEntity>(
                listOf(),
                null,
                PagingConfig(10),
                10
        )
        val result = mediator.load(LoadType.REFRESH, pagingState)

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertTrue((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
    }

    @Test
    fun refreshLoadReturnsErrorResultWhenErrorOccurs() = runBlocking {
        val apiClient = FakeAPIClient(ApplicationProvider.getApplicationContext())
        apiClient.failureMessage = "Unable to load data"
        val mediator = DiscussionsMediator(
                apiClient,
                db
        )
        val pagingState = PagingState<Int, DiscussionPostEntity>(
                listOf(),
                null,
                PagingConfig(10),
                10
        )
        val result = mediator.load(LoadType.REFRESH, pagingState)
        assertTrue(result is RemoteMediator.MediatorResult.Error)
    }
}