package `in`.testpress.course

import `in`.testpress.course.util.DownloadTracker
import android.app.Application
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.offline.ActionFileUpgradeUtil
import com.google.android.exoplayer2.offline.DefaultDownloadIndex
import com.google.android.exoplayer2.offline.DefaultDownloaderFactory
import com.google.android.exoplayer2.offline.DownloadManager
import com.google.android.exoplayer2.offline.DownloaderConstructorHelper
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.FileDataSource
import com.google.android.exoplayer2.upstream.cache.Cache
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.Log
import com.google.android.exoplayer2.util.Util
import java.io.File
import java.io.IOException

class CourseApplication: Application() {
    var mDownloadManager: DownloadManager? = null
    private var mDownloadTracker: DownloadTracker? = null
    lateinit var userAgent: String
    lateinit var databaseProvider:ExoDatabaseProvider
    private val downloadDirectory: File?
        get() {
            return if (getExternalFilesDir(null) != null) getExternalFilesDir(null) else filesDir
        }
    lateinit var downloadCache: Cache


    override fun onCreate() {
        super.onCreate()
        userAgent = Util.getUserAgent(this, "TestpressSdk")
        databaseProvider =  ExoDatabaseProvider(this)
        val downloadContentDirectory = File(downloadDirectory, DOWNLOAD_CONTENT_DIRECTORY)
        downloadCache = SimpleCache(downloadContentDirectory, NoOpCacheEvictor(), databaseProvider)
    }

    fun getDownloadManager(): DownloadManager {
        initDownloadManager()
        return mDownloadManager!!
    }

    fun getDownloadTracker(): DownloadTracker {
        initDownloadManager()
        return mDownloadTracker!!
    }

    @Synchronized
    private fun initDownloadManager() {
        if (mDownloadManager == null) {
            val downloadIndex = DefaultDownloadIndex(databaseProvider)
            upgradeActionFile(
                DOWNLOAD_ACTION_FILE,
                downloadIndex,
                false
            )
            upgradeActionFile(
                DOWNLOAD_TRACKER_ACTION_FILE,
                downloadIndex,
                true
            )
            val downloaderConstructorHelper =
                DownloaderConstructorHelper(downloadCache, DefaultHttpDataSourceFactory(userAgent))
            mDownloadManager = DownloadManager(
                this, downloadIndex, DefaultDownloaderFactory(downloaderConstructorHelper)
            )
            mDownloadTracker =
                DownloadTracker(this, buildDataSourceFactory(), mDownloadManager!!)
        }
    }

    fun buildDataSourceFactory(): DataSource.Factory {
        val upstreamFactory =
            DefaultDataSourceFactory(this, DefaultHttpDataSourceFactory(userAgent))
        return buildReadOnlyCacheDataSource(upstreamFactory, downloadCache)
    }

    private fun upgradeActionFile(
        fileName: String,
        downloadIndex: DefaultDownloadIndex,
        addNewDownloadsAsCompleted: Boolean
    ) {
        try {
            ActionFileUpgradeUtil.upgradeAndDelete(
                File(downloadDirectory, fileName),
                null,
                downloadIndex,
                true,
                addNewDownloadsAsCompleted
            )
        } catch (e: IOException) {
            Log.e(
                "CourseApplication",
                "Failed to upgrade action file: $fileName",
                e
            )
        }
    }

    companion object {
        private const val TAG = "DemoApplication"
        private const val DOWNLOAD_ACTION_FILE = "actions"
        private const val DOWNLOAD_TRACKER_ACTION_FILE = "tracked_actions"
        private const val DOWNLOAD_CONTENT_DIRECTORY = "downloads"

        protected fun buildReadOnlyCacheDataSource(
            upstreamFactory: DataSource.Factory?,
            cache: Cache?
        ): CacheDataSourceFactory {
            return CacheDataSourceFactory(
                cache,
                upstreamFactory,
                FileDataSource.Factory(),  /* cacheWriteDataSinkFactory= */
                null,
                CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR,  /* eventListener= */
                null
            )
        }
    }
}