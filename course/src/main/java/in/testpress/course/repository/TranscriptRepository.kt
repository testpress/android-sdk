package `in`.testpress.course.repository

import `in`.testpress.course.network.TranscriptTextDownloader
import `in`.testpress.course.domain.TranscriptSegment
import `in`.testpress.course.domain.WebVttTranscriptParser
import android.util.LruCache

class TranscriptRepository(
    private val downloader: TranscriptTextDownloader = TranscriptTextDownloader(),
    private val parser: WebVttTranscriptParser = WebVttTranscriptParser(),
) {

    private companion object {
        private const val MAX_CACHE_ENTRIES = 10
    }

    private val parsedSegmentsCache = LruCache<String, List<TranscriptSegment>>(MAX_CACHE_ENTRIES)

    suspend fun getTranscriptSegments(url: String): Result<List<TranscriptSegment>> {
        if (url.isBlank()) return Result.failure(IllegalArgumentException("url is blank"))

        getCachedSegments(url)?.let { return Result.success(it) }

        val vttResult = downloader.download(url)
        if (vttResult.isFailure) return Result.failure(vttResult.exceptionOrNull()!!)

        return runCatching { parser.parse(vttResult.getOrNull()) }
            .onSuccess { segments ->
                if (segments.isNotEmpty()) {
                    putCachedSegments(url, segments)
                }
            }
    }

    private fun getCachedSegments(url: String): List<TranscriptSegment>? = synchronized(parsedSegmentsCache) {
        parsedSegmentsCache.get(url)
    }

    private fun putCachedSegments(url: String, segments: List<TranscriptSegment>) = synchronized(parsedSegmentsCache) {
        parsedSegmentsCache.put(url, segments)
    }
}
