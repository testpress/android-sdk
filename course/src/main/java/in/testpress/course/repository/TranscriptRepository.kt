package `in`.testpress.course.repository

import `in`.testpress.course.network.TranscriptTextDownloader
import `in`.testpress.course.domain.transcript.TranscriptSegment
import `in`.testpress.course.domain.transcript.WebVttTranscriptParser

class TranscriptRepository(
    private val downloader: TranscriptTextDownloader = TranscriptTextDownloader(),
    private val parser: WebVttTranscriptParser = WebVttTranscriptParser(),
) {

    suspend fun getTranscriptSegments(url: String): Result<List<TranscriptSegment>> {
        if (url.isBlank()) return Result.failure(IllegalArgumentException("url is blank"))

        val vttResult = downloader.download(url)
        if (vttResult.isFailure) return Result.failure(vttResult.exceptionOrNull()!!)

        return runCatching { parser.parse(vttResult.getOrNull()) }
    }
}
