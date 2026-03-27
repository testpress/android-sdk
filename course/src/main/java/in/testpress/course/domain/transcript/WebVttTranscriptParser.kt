package `in`.testpress.course.domain.transcript

data class TranscriptSegment(
    val startTimeSeconds: Double,
    val endTimeSeconds: Double,
    val text: String,
)

data class WebVttTranscriptParserConfig(
    val maxGapSeconds: Double = 2.0,
    val maxDurationSeconds: Double = 10.0,
    val maxTextLength: Int = 160,
)

class WebVttTranscriptParser(
    private val config: WebVttTranscriptParserConfig = WebVttTranscriptParserConfig(),
) {

    fun parse(vttText: String?): List<TranscriptSegment> {
        val normalized = normalize(vttText) ?: return emptyList()

        val cues = splitBlocks(normalized)
            .mapNotNull(::parseBlock)

        return groupCues(cues)
    }

    private fun normalize(input: String?): String? {
        if (input.isNullOrBlank()) return null

        val normalized = input
            .replace("\r\n", "\n")
            .replace("\r", "\n")
            .trim()

        return normalized.takeIf { it.isNotBlank() }
    }

    private fun splitBlocks(normalized: String): List<String> {
        return normalized.split(BLOCK_SEPARATOR_REGEX)
            .map(String::trim)
            .filter(String::isNotBlank)
    }

    private fun parseBlock(block: String): TranscriptSegment? {
        val nonEmptyLines = block.lines()
            .asSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toList()

        if (nonEmptyLines.size < 2) return null
        if (nonEmptyLines.first().equals(WEBVTT_HEADER, ignoreCase = true)) return null

        val timeLineIndex = nonEmptyLines.indexOfFirst { it.contains(TIMELINE_SEPARATOR) }
        if (timeLineIndex == -1) return null

        val timeline = parseTimeline(nonEmptyLines[timeLineIndex]) ?: return null
        val text = extractAndCleanText(nonEmptyLines, timeLineIndex) ?: return null

        return TranscriptSegment(
            startTimeSeconds = timeline.startSeconds,
            endTimeSeconds = timeline.endSeconds,
            text = text,
        )
    }

    private fun parseTimeline(line: String): Timeline? {
        val parts = line.split(TIMELINE_SEPARATOR)
        if (parts.size < 2) return null

        val start = timestampToSeconds(parts[0]) ?: return null
        val endToken = parts[1].trim()
            .split(WHITESPACE_REGEX)
            .firstOrNull()
            ?: return null
        val end = timestampToSeconds(endToken) ?: return null

        if (end <= start) return null
        return Timeline(startSeconds = start, endSeconds = end)
    }

    private fun extractAndCleanText(lines: List<String>, timeLineIndex: Int): String? {
        val rawText = lines
            .drop(timeLineIndex + 1)
            .joinToString(separator = " ")
            .trim()

        if (rawText.isBlank()) return null

        val cleaned = rawText
            .replace(HTML_TAG_REGEX, "")
            .trim()

        return cleaned.takeIf { it.isNotBlank() }
    }

    private fun groupCues(cues: List<TranscriptSegment>): List<TranscriptSegment> {
        if (cues.isEmpty()) return emptyList()

        val grouped = ArrayList<TranscriptSegment>(cues.size)
        var current: TranscriptSegment = cues.first()

        for (cue in cues.drop(1)) {
            val shouldSplit = shouldSplitGroup(current, cue)

            if (shouldSplit) {
                grouped.add(current)
                current = cue
            } else {
                current = current.copy(
                    endTimeSeconds = cue.endTimeSeconds,
                    text = "${current.text} ${cue.text}".trim()
                )
            }
        }

        grouped.add(current)
        return grouped
    }

    private fun shouldSplitGroup(current: TranscriptSegment, next: TranscriptSegment): Boolean {
        val gapSeconds = next.startTimeSeconds - current.endTimeSeconds
        if (gapSeconds > config.maxGapSeconds) return true

        val groupDurationSeconds = next.endTimeSeconds - current.startTimeSeconds
        if (groupDurationSeconds > config.maxDurationSeconds) return true

        val combinedTextLength = current.text.length + next.text.length
        return combinedTextLength > config.maxTextLength
    }

    private fun timestampToSeconds(ts: String): Double? {
        val parts = ts.trim().split(":")
        return try {
            when (parts.size) {
                2 -> {
                    val minutes = parts[0].toDouble()
                    val seconds = parts[1].toDouble()
                    (minutes * 60.0) + seconds
                }
                3 -> {
                    val hours = parts[0].toDouble()
                    val minutes = parts[1].toDouble()
                    val seconds = parts[2].toDouble()
                    (hours * 3600.0) + (minutes * 60.0) + seconds
                }
                else -> null
            }
        } catch (_: Throwable) {
            null
        }
    }

    private data class Timeline(
        val startSeconds: Double,
        val endSeconds: Double,
    )

    private companion object {
        private const val WEBVTT_HEADER = "WEBVTT"
        private const val TIMELINE_SEPARATOR = "-->"
        private val WHITESPACE_REGEX = Regex("\\s+")
        private val BLOCK_SEPARATOR_REGEX = Regex("\n{2,}")
        private val HTML_TAG_REGEX = Regex("<[^>]+>")
    }
}
