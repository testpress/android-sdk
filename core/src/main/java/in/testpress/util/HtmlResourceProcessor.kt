package `in`.testpress.util

object HtmlResourceProcessor {
    
    private val SCRIPT_SRC_PATTERN = Regex("""<script[^>]+src=["']([^"']+)["']""")
    private val LINK_HREF_PATTERN = Regex("""<link[^>]+href=["']([^"']+)["']""")
    
    fun extractExternalResources(html: String): List<Pair<String, String>> {
        val resources = mutableListOf<Pair<String, String>>()
        
        extractScriptResources(html, resources)
        extractLinkResources(html, resources)
        
        return resources
    }
    
    private fun extractScriptResources(html: String, resources: MutableList<Pair<String, String>>) {
        SCRIPT_SRC_PATTERN.findAll(html).forEach { match ->
            val url = match.groupValues[1].trim()
            if (url.startsWith("http")) {
                resources.add(url to extractFileName(url, ".js"))
            }
        }
    }
    
    private fun extractLinkResources(html: String, resources: MutableList<Pair<String, String>>) {
        LINK_HREF_PATTERN.findAll(html).forEach { match ->
            val url = match.groupValues[1].trim()
            if (url.startsWith("http")) {
                resources.add(url to extractFileName(url, ".css"))
            }
        }
    }
    
    fun replaceUrls(html: String, replacements: Map<String, String>): String {
        var processedHtml = html
        replacements.forEach { (originalUrl, localPath) ->
            val escapedUrl = Regex.escape(originalUrl)
            val pattern = Regex("""(src|href)=["'](\s*$escapedUrl\s*)["']""")
            processedHtml = pattern.replace(processedHtml) { matchResult ->
                val attribute = matchResult.groupValues[1]
                """$attribute="$localPath""""
            }
        }
        return processedHtml
    }
    
    private fun extractFileName(url: String, extension: String): String {
        val lastSegment = url.split("/").lastOrNull() ?: return "resource$extension"
        return if (lastSegment.contains(extension)) {
            lastSegment.substringBefore("?")
        } else {
            "resource_${url.hashCode()}$extension"
        }
    }
}
