package `in`.testpress.course.fragments

import android.content.Context
import android.os.Bundle
import androidx.test.core.app.ApplicationProvider
import `in`.testpress.course.util.WebViewCache
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AIChatPdfFragmentTest {

    private lateinit var context: Context
    private lateinit var fragment: AIChatPdfFragment

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        WebViewCache.init(context)
        fragment = AIChatPdfFragment()
    }

    @After
    fun tearDown() {
        WebViewCache.clearAll()
    }

    @Test
    fun fragmentShouldCreateSuccessfully() {
        fragment.arguments = Bundle().apply {
            putLong("courseId", 123L)
            putLong("contentId", 456L)
            putString("pdfUrl", "https://example.com/test.pdf")
            putString("pdfTitle", "Test PDF")
        }
        
        assertNotNull(fragment)
        assertNotNull(fragment.arguments)
    }

    @Test
    fun fragmentShouldNotCrashOnDestroyViewWithoutCreation() {
        assertDoesNotThrow {
            fragment.onDestroyView()
        }
    }

    @Test
    fun fragmentShouldNotCrashOnRetryClick() {
        assertDoesNotThrow {
            fragment.onRetryClick()
        }
    }

    private fun assertDoesNotThrow(block: () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            fail("Should not throw exception: ${e.message}")
        }
    }
}

