package `in`.testpress.course.helpers

import `in`.testpress.course.util.CourseApplication
import `in`.testpress.course.util.ExoPlayerDataSourceFactory
import `in`.testpress.util.DateUtils
import android.content.Context
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.offline.DefaultDownloadIndex
import com.google.android.exoplayer2.offline.DefaultDownloaderFactory
import com.google.android.exoplayer2.offline.DownloadManager
import com.google.android.exoplayer2.offline.DownloaderConstructorHelper
import com.google.android.exoplayer2.upstream.cache.Cache
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import java.io.File
import java.util.Date

class VideoDownloadManager {
    private lateinit var downloadCache: Cache
    private lateinit var context: Context
    private var downloadManger: DownloadManager? = null
    private lateinit var databaseProvider: ExoDatabaseProvider

    fun init() {
        databaseProvider = ExoDatabaseProvider(context)
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
            DownloaderConstructorHelper(
                getDownloadCache(),
                ExoPlayerDataSourceFactory(context).getHttpDataSourceFactory()
            )
        downloadManger = DownloadManager(
            context,
            downloadIndex,
            DefaultDownloaderFactory(downloaderConstructorHelper)
        )
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


    fun isVideosRefreshed(): Boolean {
        val sharedPreferences = context.getSharedPreferences(DOWNLOADS_SHARED_PREFERENCE, Context.MODE_PRIVATE)
        if (sharedPreferences.getLong(LAST_REFRESH_DATE, -1) != -1L) {
            val lastRefreshedDate = Date(sharedPreferences.getLong(LAST_REFRESH_DATE, 0))
            val today = Date()
            return DateUtils.difference(lastRefreshedDate, today) <= 15
        }
        return false
    }

    fun updateRefreshDate() {
        val today = Date()
        val sharedPreferences = context.getSharedPreferences(DOWNLOADS_SHARED_PREFERENCE, Context.MODE_PRIVATE)
        sharedPreferences.edit().putLong(LAST_REFRESH_DATE, today.time).apply();
    }

    companion object {
        const val DOWNLOAD_CONTENT_DIRECTORY = "downloads"
        const val DOWNLOADS_SHARED_PREFERENCE = "downloadsSharedPreference"
        const val LAST_REFRESH_DATE = "lastRefreshDate"
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