package `in`.testpress.course.util

import android.content.Context
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import okhttp3.OkHttpClient
import java.io.File
import java.io.FileOutputStream
import okhttp3.Request
import java.util.concurrent.ConcurrentHashMap


class ResourceDownloader(val context: Context) {
    private val client = OkHttpClient()
    private val semaphore = Semaphore(10)

    suspend fun downloadResources(
        urls: List<String>,
        onComplete: suspend (HashMap<String, String>) -> Unit
    ) {
        val urlToLocalPathMap = ConcurrentHashMap<String, String>()
        coroutineScope {
            val deferredDownloads = urls.map { url ->
                async {
                    semaphore.withPermit {
                        downloadResource(url)?.let { localPath ->
                            urlToLocalPathMap[url] = localPath
                        }
                    }
                }
            }
            deferredDownloads.awaitAll()
            onComplete(HashMap(urlToLocalPathMap))
        }
    }

    private fun downloadResource(url: String): String? {
        return try {
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                response.body?.let { body ->
                    val fileName = url.substringAfterLast('/')
                    val file = File(context.filesDir, fileName)
                    val fos = FileOutputStream(file)
                    fos.use {
                        it.write(body.bytes())
                    }
                    body.close()
                    return "file://${file.absolutePath}"
                }
            }
            response.close()
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun <T> Semaphore.withPermit(block: suspend () -> T): T {
        acquire()
        try {
            return block()
        } finally {
            release()
        }
    }
}


