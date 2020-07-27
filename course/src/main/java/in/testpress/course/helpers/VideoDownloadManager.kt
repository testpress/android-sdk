package `in`.testpress.course.helpers

import `in`.testpress.course.util.CourseApplication
import `in`.testpress.course.util.ExoPlayerDataSourceFactory
import android.content.Context
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.offline.DefaultDownloadIndex
import com.google.android.exoplayer2.offline.DefaultDownloaderFactory
import com.google.android.exoplayer2.offline.DownloadManager
import com.google.android.exoplayer2.offline.DownloaderConstructorHelper
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.google.android.exoplayer2.upstream.cache.Cache
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.Util
import java.io.File

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