package `in`.testpress.util

object HtmlResourceProcessor {
    
    private val SCRIPT_SRC_PATTERN = Regex("""<script[^>]+src=["']([^"']+)["']""")
    private val LINK_HREF_PATTERN = Regex("""<link[^>]+href=["']([^"']+)["']""")
    
    fun extractExternalResources(html: String): List<Pair<String, String>> {
        val resources = mutableListOf<Pair<String, String>>()
        
        SCRIPT_SRC_PATTERN.findAll(html).forEach { match ->
            val url = match.groupValues[1]
            if (url.startsWith("http")) {
                val fileName = extractFileName(url, ".js")
                resources.add(url to fileName)
            }
        }
        
        LINK_HREF_PATTERN.findAll(html).forEach { match ->
            val url = match.groupValues[1]
            if (url.startsWith("http")) {
                val fileName = extractFileName(url, ".css")
                resources.add(url to fileName)
            }
        }
        
        return resources
    }
    
    fun replaceUrls(html: String, replacements: Map<String, String>): String {
        var processedHtml = html
        replacements.forEach { (originalUrl, localPath) ->
            processedHtml = processedHtml.replace(originalUrl, localPath)
        }
        return processedHtml
    }
    
    private fun extractFileName(url: String, extension: String): String {
        val segments = url.split("/")
        val lastSegment = segments.lastOrNull() ?: "resource$extension"
        return if (lastSegment.contains(extension)) {
            lastSegment.substringBefore("?")
        } else {
            "resource_${url.hashCode()}$extension"
        }
    }
}

