package `in`.testpress.course.fragments

import android.view.Menu
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class VideoContentMenuTest {

    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    lateinit var menu: Menu

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun initializedMenuShouldNotThrowError() {
        val fragment = VideoContentFragment()
        fragment.menu = menu
        assertDoesNotThrow {
            fragment.menu.clear()
        }
    }

    @Test
    fun uninitializedMenuShouldThrowError() {
        val fragment = VideoContentFragment()
        assertThrows<UninitializedPropertyAccessException> {
            fragment.menu.clear()
        }
    }

    @Test
    fun uninitializedMenuShouldNotThrowError() {
        val fragment = VideoContentFragment()
        assertDoesNotThrow {
            fragment.setDownloadMenuVisibility()
        }
    }
}
