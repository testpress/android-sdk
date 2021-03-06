package `in`.testpress.course.util

import `in`.testpress.course.helpers.VideoDownloadManager
import `in`.testpress.util.UserAgentProvider
import android.content.Context
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.FileDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import okhttp3.OkHttpClient

class ExoPlayerDataSourceFactory(val context: Context) {
    private val bandwidthMeter = DefaultBandwidthMeter.Builder(context).build()
    private val userAgent: String = UserAgentProvider.get(context)

    fun getHttpDataSourceFactory(): OkHttpDataSourceFactory {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(VideoPlayerInterceptor(context))
            .build()
        return OkHttpDataSourceFactory(okHttpClient, userAgent, bandwidthMeter)
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