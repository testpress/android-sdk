package `in`.testpress.course.util

import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

class ResourcesDownloader(val context: Context) {
    private val client = OkHttpClient()

    fun downloadResources(
        urls: List<String>,
        onComplete: suspend (HashMap<String, String>) -> Unit
    ) {
        val urlToLocalPathMap = HashMap<String, String>()
        val scope = CoroutineScope(Dispatchers.IO)

        urls.forEach {
            Log.d("TAG", "downloadResources: $it")
        }

        scope.launch {
            val deferredDownloads = urls.map { url ->
                async {
                    downloadResource(url)?.let { localPath ->
                        urlToLocalPathMap[url] = localPath
                    }
                }
            }
            deferredDownloads.awaitAll()
            onComplete(urlToLocalPathMap)
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
                    return "file://${file.absolutePath}"
                }
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}


