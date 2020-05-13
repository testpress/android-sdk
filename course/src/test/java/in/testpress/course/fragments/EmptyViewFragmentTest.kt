package `in`.testpress.course.fragments

import `in`.testpress.core.TestpressException
import `in`.testpress.course.R
import `in`.testpress.fragments.EmptyViewFragment
import `in`.testpress.models.TestpressApiResponse
import `in`.testpress.models.greendao.Content
import android.content.Context
import android.view.View
import androidx.test.core.app.ApplicationProvider
import okhttp3.MediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.ResponseBody
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.support.v4.SupportFragmentTestUtil
import retrofit2.Response
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
class EmptyViewFragmentTest {
    lateinit var fragment: EmptyViewFragment
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Before
    fun setUp() {
        fragment = EmptyViewFragment()
        SupportFragmentTestUtil.startFragment(fragment)
    }

    private fun buildErrorResponse(errorCode: Int): Response<TestpressApiResponse<Content>> {
        val responseBody = ResponseBody.create(MediaType.parse("application/json"), """{"detail":"Error"}""")
        return Response.error<TestpressApiResponse<Content>>(responseBody, okhttp3.Response.Builder()
                .code(errorCode)
                .message("Error")
                .protocol(Protocol.HTTP_1_1)
                .request(Request.Builder().url("http://localhost/").build())
                .build())
    }

    @Test
    fun testNetworkError() {
        val ioException = IOException()
        val exception = TestpressException.networkError(ioException)
        fragment.displayError(exception)

        Assert.assertEquals(View.VISIBLE, fragment.emptyContainer.visibility)
        Assert.assertEquals(context.getString(R.string.testpress_network_error), fragment.emptyTitleView.text)
        Assert.assertEquals(context.getString(R.string.testpress_no_internet_try_again), fragment.emptyDescView.text)
    }

    @Test
    fun testForbiddenError() {
        val exception = TestpressException.httpError(buildErrorResponse(403))
        fragment.displayError(exception)

        Assert.assertEquals(View.VISIBLE, fragment.emptyContainer.visibility)
        Assert.assertEquals(context.getString(R.string.permission_denied), fragment.emptyTitleView.text)
        Assert.assertEquals(context.getString(R.string.testpress_no_permission), fragment.emptyDescView.text)
    }

    @Test
    fun testIsPageNotFound() {
        val exception = TestpressException.httpError(buildErrorResponse(404))
        fragment.displayError(exception)

        Assert.assertEquals(View.VISIBLE, fragment.emptyContainer.visibility)
        Assert.assertEquals(context.getString(R.string.testpress_content_not_available), fragment.emptyTitleView.text)
        Assert.assertEquals(context.getString(R.string.testpress_content_not_available_description), fragment.emptyDescView.text)
    }

    @Test
    fun testOtherErrorCode() {
        val exception = TestpressException.httpError(buildErrorResponse(405))
        fragment.displayError(exception)

        Assert.assertEquals(View.VISIBLE, fragment.emptyContainer.visibility)
        Assert.assertEquals(context.getString(R.string.testpress_error_loading_contents), fragment.emptyTitleView.text)
        Assert.assertEquals(context.getString(R.string.testpress_some_thing_went_wrong_try_again), fragment.emptyDescView.text)
    }

    @Test
    fun testRetryButtonClick() {
        val ioException = IOException()
        val exception = TestpressException.networkError(ioException)
        fragment.displayError(exception)
        Assert.assertEquals(View.VISIBLE, fragment.emptyContainer.visibility)

        fragment.retryButton.performClick()

        Assert.assertEquals(View.GONE, fragment.emptyContainer.visibility)
    }
}