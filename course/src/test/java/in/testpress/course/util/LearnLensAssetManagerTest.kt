package `in`.testpress.course.util

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
class LearnLensAssetManagerTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        cleanupCache()
    }

    @After
    fun tearDown() {
        cleanupCache()
    }

    @Test
    fun isAssetsCachedShouldReturnFalseWhenFilesDoNotExist() {
        val cached = LearnLensAssetManager.isAssetsCached(context)
        assertFalse(cached)
    }

    @Test
    fun getJsFilePathShouldReturnCdnUrlWhenFileDoesNotExist() {
        val jsPath = LearnLensAssetManager.getJsFilePath(context)
        assertTrue(jsPath.startsWith("https://"))
    }

    @Test
    fun getCssFilePathShouldReturnCdnUrlWhenFileDoesNotExist() {
        val cssPath = LearnLensAssetManager.getCssFilePath(context)
        assertTrue(cssPath.startsWith("https://"))
    }

    @Test
    fun getJsFilePathShouldReturnFileUrlWhenCached() {
        createDummyAssets()
        val jsPath = LearnLensAssetManager.getJsFilePath(context)
        assertTrue(jsPath.startsWith("file://"))
    }

    @Test
    fun getCssFilePathShouldReturnFileUrlWhenCached() {
        createDummyAssets()
        val cssPath = LearnLensAssetManager.getCssFilePath(context)
        assertTrue(cssPath.startsWith("file://"))
    }

    @Test
    fun isAssetsCachedShouldReturnTrueWhenBothFilesExist() {
        createDummyAssets()
        val cached = LearnLensAssetManager.isAssetsCached(context)
        assertTrue(cached)
    }

    @Test
    fun generateLearnLensHtmlShouldContainPdfUrl() {
        val html = LearnLensAssetManager.generateLearnLensHtml(
            context,
            "https://example.com/test.pdf",
            "Test PDF",
            "123",
            "token123"
        )
        assertTrue(html.contains("https://example.com/test.pdf"))
    }

    @Test
    fun generateLearnLensHtmlShouldContainPdfTitle() {
        val html = LearnLensAssetManager.generateLearnLensHtml(
            context,
            "https://example.com/test.pdf",
            "Test PDF",
            "123",
            "token123"
        )
        assertTrue(html.contains("Test PDF"))
    }

    @Test
    fun generateLearnLensHtmlShouldContainAuthToken() {
        val html = LearnLensAssetManager.generateLearnLensHtml(
            context,
            "https://example.com/test.pdf",
            "Test PDF",
            "123",
            "token123"
        )
        assertTrue(html.contains("token123"))
    }

    @Test
    fun generateLearnLensHtmlShouldContainPdfId() {
        val html = LearnLensAssetManager.generateLearnLensHtml(
            context,
            "https://example.com/test.pdf",
            "Test PDF",
            "123",
            "token123"
        )
        assertTrue(html.contains("\"123\""))
    }

    @Test
    fun generateLearnLensHtmlShouldBeValidHtml() {
        val html = LearnLensAssetManager.generateLearnLensHtml(
            context,
            "https://example.com/test.pdf",
            "Test PDF",
            "123",
            "token123"
        )
        assertTrue(html.contains("<!DOCTYPE html>"))
        assertTrue(html.contains("<html>"))
        assertTrue(html.contains("</html>"))
        assertTrue(html.contains("<body>"))
        assertTrue(html.contains("</body>"))
    }

    @Test
    fun downloadAssetsInBackgroundShouldNotCrash() {
        assertDoesNotThrow {
            LearnLensAssetManager.downloadAssetsInBackground(context)
        }
    }

    private fun createDummyAssets() {
        val cacheDir = File(context.filesDir, "learnlens_cache")
        cacheDir.mkdirs()
        File(cacheDir, "learnlens-pdfchat.iife.js").writeText("// dummy js")
        File(cacheDir, "learnlens-frontend.css").writeText("/* dummy css */")
    }

    private fun cleanupCache() {
        val cacheDir = File(context.filesDir, "learnlens_cache")
        cacheDir.deleteRecursively()
    }

    private fun assertDoesNotThrow(block: () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            fail("Should not throw exception: ${e.message}")
        }
    }
}

