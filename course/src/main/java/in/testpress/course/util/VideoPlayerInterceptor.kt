package `in`.testpress.course.util

import `in`.testpress.core.TestpressSdk
import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response
import java.net.URI

class VideoPlayerInterceptor(val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        if (request.url.toString().contains("encryption_key")) {
            val session = TestpressSdk.getTestpressSession(context)
            val hostUrl = getHostUrl(context)
            val updatedUrl = request.url.newBuilder().host(hostUrl).build()
            request = request.newBuilder()
                .addHeader("Authorization", "JWT " + session?.token)
                .url(updatedUrl)
                .build()
        }
        return chain.proceed(request)
    }
}

private fun getHostUrl(context: Context): String {
    val uri = URI.create(TestpressSdk.getTestpressSession(context)?.instituteSettings?.baseUrl)
    return uri.host
}