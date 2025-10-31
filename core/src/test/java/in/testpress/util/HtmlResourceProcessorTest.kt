package `in`.testpress.util

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class HtmlResourceProcessorTest {

    @Test
    fun shouldExtractScriptTagsWithHttpUrls() {
        val html = """<script src="https://example.com/app.js"></script>"""

        val resources = HtmlResourceProcessor.extractExternalResources(html)

        assertEquals(1, resources.size)
        assertEquals("https://example.com/app.js", resources[0].first)
        assertTrue(resources[0].second.endsWith(".js"))
    }

    @Test
    fun shouldExtractLinkTagsWithHttpUrls() {
        val html = """<link rel="stylesheet" href="https://example.com/style.css">"""

        val resources = HtmlResourceProcessor.extractExternalResources(html)

        assertEquals(1, resources.size)
        assertEquals("https://example.com/style.css", resources[0].first)
        assertTrue(resources[0].second.endsWith(".css"))
    }

    @Test
    fun shouldExtractMultipleResources() {
        val html = """
            <script src="https://cdn.com/app.js"></script>
            <link rel="stylesheet" href="https://cdn.com/style.css">
            <script src="https://cdn.com/vendor.js"></script>
        """.trimIndent()

        val resources = HtmlResourceProcessor.extractExternalResources(html)

        assertEquals(3, resources.size)
    }

    @Test
    fun shouldIgnoreRelativeUrls() {
        val html = """
            <script src="/local/script.js"></script>
            <link rel="stylesheet" href="./style.css">
        """.trimIndent()

        val resources = HtmlResourceProcessor.extractExternalResources(html)

        assertEquals(0, resources.size)
    }

    @Test
    fun shouldReplaceUrlsWithLocalPaths() {
        val html = """<script src="https://cdn.com/app.js"></script>"""
        val replacements = mapOf("https://cdn.com/app.js" to "file:///cache/app.js")

        val result = HtmlResourceProcessor.replaceUrls(html, replacements)

        assertTrue(result.contains("file:///cache/app.js"))
        assertFalse(result.contains("https://cdn.com/app.js"))
    }

    @Test
    fun shouldHandleEmptyHtml() {
        val html = ""

        val resources = HtmlResourceProcessor.extractExternalResources(html)

        assertEquals(0, resources.size)
    }

    @Test
    fun shouldHandleHtmlWithoutExternalResources() {
        val html = """
            <html>
            <body>
                <h1>Hello World</h1>
            </body>
            </html>
        """.trimIndent()

        val resources = HtmlResourceProcessor.extractExternalResources(html)

        assertEquals(0, resources.size)
    }

    @Test
    fun shouldExtractFileNameFromUrl() {
        val html = """<script src="https://cdn.com/path/to/app.min.js?version=1"></script>"""

        val resources = HtmlResourceProcessor.extractExternalResources(html)

        assertEquals("app.min.js", resources[0].second)
    }
}


