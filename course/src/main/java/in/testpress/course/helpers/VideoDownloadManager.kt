package `in`.testpress.course.helpers

import `in`.testpress.course.services.VideoDownloadService
import `in`.testpress.course.util.CourseApplication
import `in`.testpress.course.util.VideoPlayerInterceptor
import `in`.testpress.util.UserAgentProvider
import android.content.Context
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory
import com.google.android.exoplayer2.offline.DefaultDownloadIndex
import com.google.android.exoplayer2.offline.DefaultDownloaderFactory
import com.google.android.exoplayer2.offline.DownloadManager
import com.google.android.exoplayer2.offline.DownloadService
import com.google.android.exoplayer2.offline.DownloaderConstructorHelper
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.FileDataSource
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.google.android.exoplayer2.upstream.cache.Cache
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.Util
import okhttp3.OkHttpClient
import java.io.File

class VideoDownloadManager {
    private lateinit var downloadCache: Cache
    private lateinit var context: Context
    private var downloadManger: DownloadManager? = null
    private lateinit var databaseProvider: ExoDatabaseProvider

    fun init() {
        databaseProvider = ExoDatabaseProvider(context)
    }

    @Synchronized
    fun getDownloadCache(): Cache {
        if (!::downloadCache.isInitialized) {
            val courseApplication = context.applicationContext as CourseApplication
            val downloadContentDirectory =
                File(courseApplication.getDownloadDirectory(), DOWNLOAD_CONTENT_DIRECTORY)
            downloadCache =
                SimpleCache(downloadContentDirectory, NoOpCacheEvictor(), databaseProvider)
        }
        return downloadCache
    }

    fun get(): DownloadManager {
        if (downloadManger == null) {
            initializeDownloadManger()
        }

        return downloadManger!!
    }

    @Synchronized
    private fun initializeDownloadManger() {
        val downloadIndex = DefaultDownloadIndex(databaseProvider)
        val downloaderConstructorHelper =
            DownloaderConstructorHelper(getDownloadCache(), buildHttpDataSourceFactory())
        downloadManger = DownloadManager(
            context,
            downloadIndex,
            DefaultDownloaderFactory(downloaderConstructorHelper)
        )
    }

    private fun buildHttpDataSourceFactory(): HttpDataSource.Factory {
        val userAgent = Util.getUserAgent(context, context.packageName)
        return DefaultHttpDataSourceFactory(userAgent)
    }

    fun buildDataSourceFactory(): CacheDataSourceFactory {
        val userAgent: String = UserAgentProvider.get(context)
        val bandwidthMeter = DefaultBandwidthMeter.Builder(context).build()
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(VideoPlayerInterceptor(context))
            .build()
        val okHttpDataSourceFactory =
            OkHttpDataSourceFactory(okHttpClient, userAgent, bandwidthMeter)
        return buildReadOnlyCacheDataSource(okHttpDataSourceFactory, downloadCache)
    }

    private fun buildReadOnlyCacheDataSource(
        upstreamFactory: DataSource.Factory,
        cache: Cache
    ): CacheDataSourceFactory {
        return CacheDataSourceFactory(
            cache,
            upstreamFactory,
            FileDataSource.Factory(),
            null,
            CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR,
            null
        )
    }

    companion object {
        const val DOWNLOAD_CONTENT_DIRECTORY = "downloads"
        private lateinit var INSTANCE: VideoDownloadManager

        @JvmStatic
        operator fun invoke(context: Context): VideoDownloadManager {
            synchronized(VideoDownloadManager::class.java) {
                if (!::INSTANCE.isInitialized) {
                    INSTANCE = VideoDownloadManager()
                    INSTANCE.context = context
                    INSTANCE.init()
                }
                return INSTANCE
            }
        }
    }
}