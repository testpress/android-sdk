package `in`.testpress.course.fragments

import android.content.Context
import android.os.Bundle
import androidx.test.core.app.ApplicationProvider
import `in`.testpress.course.util.PdfWebViewCache
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
        PdfWebViewCache.init(context)
        fragment = AIChatPdfFragment()
    }

    @After
    fun tearDown() {
        PdfWebViewCache.clearAll()
    }

    @Test
    fun fragmentShouldRequireContentIdArgument() {
        fragment.arguments = Bundle().apply {
            putLong("courseId", 123L)
        }
        
        try {
            fragment.onViewCreated(null, null)
            fail("Should throw IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("contentId"))
        }
    }

    @Test
    fun fragmentShouldRequireCourseIdArgument() {
        fragment.arguments = Bundle().apply {
            putLong("contentId", 456L)
        }
        
        try {
            fragment.onViewCreated(null, null)
            fail("Should throw IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("courseId"))
        }
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

