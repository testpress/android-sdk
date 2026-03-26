package `in`.testpress.course.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class TranscriptTextDownloader(
    private val client: OkHttpClient = OkHttpClient(),
) {
    suspend fun download(url: String): Result<String> {
        if (url.isBlank()) return Result.failure(IllegalArgumentException("url is blank"))

        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder().url(url).build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext Result.failure(
                            IllegalStateException("HTTP ${response.code}")
                        )
                    }
                    val body = response.body?.string().orEmpty()
                    Result.success(body)
                }
            } catch (t: Throwable) {
                Result.failure(t)
            }
        }
    }
}

