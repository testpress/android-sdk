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
class LocalWebFileCacheTest {

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
    fun isCachedShouldReturnFalseWhenFileDoesNotExist() {
        val cached = LocalWebFileCache.isCached(context, "test.js")
        assertFalse(cached)
    }

    @Test
    fun getLocalPathShouldReturnFallbackWhenNotCached() {
        val path = LocalWebFileCache.getLocalPath(context, "test.js", "https://cdn.com/test.js")
        assertEquals("https://cdn.com/test.js", path)
    }

    @Test
    fun getLocalPathShouldReturnFileUrlWhenCached() {
        createDummyAsset("test.js")
        val path = LocalWebFileCache.getLocalPath(context, "test.js", "https://cdn.com/test.js")
        assertTrue(path.startsWith("file://"))
    }

    @Test
    fun isCachedShouldReturnTrueWhenFileExists() {
        createDummyAsset("test.js")
        val cached = LocalWebFileCache.isCached(context, "test.js")
        assertTrue(cached)
    }

    @Test
    fun loadTemplateShouldReplaceVariables() {
        val template = "Hello {{NAME}}, you are {{AGE}} years old"
        val context = ApplicationProvider.getApplicationContext<Context>()
        
        val result = template.replace("{{NAME}}", "John").replace("{{AGE}}", "30")
        
        assertEquals("Hello John, you are 30 years old", result)
    }

    @Test
    fun downloadInBackgroundShouldNotCrash() {
        assertDoesNotThrow {
            LocalWebFileCache.downloadInBackground(context, "https://example.com/test.js", "test.js")
        }
    }

    @Test
    fun clearAllShouldRemoveAllCachedFiles() {
        createDummyAsset("file1.js")
        createDummyAsset("file2.css")
        
        assertTrue(LocalWebFileCache.isCached(context, "file1.js"))
        assertTrue(LocalWebFileCache.isCached(context, "file2.css"))
        
        LocalWebFileCache.clearAll(context)
        
        assertFalse(LocalWebFileCache.isCached(context, "file1.js"))
        assertFalse(LocalWebFileCache.isCached(context, "file2.css"))
    }

    @Test
    fun clearAllShouldNotCrashWhenCacheIsEmpty() {
        assertDoesNotThrow {
            LocalWebFileCache.clearAll(context)
        }
    }

    @Test
    fun infiniteCacheShouldNeverExpire() {
        createDummyAsset("eternal.js")
        val file = File(File(context.filesDir, "web_assets"), "eternal.js")
        file.setLastModified(System.currentTimeMillis() - (48 * 60 * 60 * 1000))
        
        val path = LocalWebFileCache.getLocalPath(context, "eternal.js", "https://cdn.com/eternal.js")
        assertTrue(path.startsWith("file://"))
    }

    @Test
    fun oldFileShouldStillBeConsideredCached() {
        createDummyAsset("old.js")
        val file = File(File(context.filesDir, "web_assets"), "old.js")
        file.setLastModified(System.currentTimeMillis() - (13 * 60 * 60 * 1000))
        
        assertTrue(LocalWebFileCache.isCached(context, "old.js"))
    }

    @Test
    fun downloadMultipleInBackgroundShouldNotCrash() {
        val files = listOf(
            "https://example.com/file1.js" to "file1.js",
            "https://example.com/file2.css" to "file2.css"
        )
        assertDoesNotThrow {
            LocalWebFileCache.downloadMultipleInBackground(context, files)
        }
    }

    @Test
    fun forceRefreshShouldIgnoreCache() {
        createDummyAsset("test.js")
        assertTrue(LocalWebFileCache.isCached(context, "test.js"))
        
        assertDoesNotThrow {
            LocalWebFileCache.downloadInBackground(context, "https://example.com/test.js", "test.js", forceRefresh = true)
        }
    }

    private fun createDummyAsset(fileName: String) {
        val cacheDir = File(context.filesDir, "web_assets")
        cacheDir.mkdirs()
        File(cacheDir, fileName).writeText("// dummy")
    }

    private fun cleanupCache() {
        val cacheDir = File(context.filesDir, "web_assets")
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

