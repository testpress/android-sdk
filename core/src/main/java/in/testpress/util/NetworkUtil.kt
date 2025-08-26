package `in`.testpress.util

import okhttp3.OkHttpClient
import okhttp3.Request

fun makeHeadRequest(url: String): Int {
    val request = Request.Builder().head().url(url).build()
    OkHttpClient().newCall(request).execute().use { response ->
        return response.code
    }
}