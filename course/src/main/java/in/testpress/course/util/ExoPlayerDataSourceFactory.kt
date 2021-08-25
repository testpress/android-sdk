package `in`.testpress.course.util

import `in`.testpress.course.helpers.VideoDownloadManager
import `in`.testpress.util.UserAgentProvider
import android.content.Context
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import okhttp3.OkHttpClient

class ExoPlayerDataSourceFactory(val context: Context) {
    private val userAgent: String = UserAgentProvider.get(context)

    fun getHttpDataSourceFactory(): OkHttpDataSource.Factory {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(VideoPlayerInterceptor(context))
            .build()
        val okHttpClientFactory = OkHttpDataSource.Factory(okHttpClient)
        okHttpClientFactory.setUserAgent(userAgent)
        return okHttpClientFactory
    }

    fun build(): CacheDataSource.Factory {
        val cache = VideoDownloadManager(context).getDownloadCache()
        return CacheDataSource.Factory()
                .setCache(cache)
                .setUpstreamDataSourceFactory(getHttpDataSourceFactory())
                .setCacheWriteDataSinkFactory(null)
                .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }
}