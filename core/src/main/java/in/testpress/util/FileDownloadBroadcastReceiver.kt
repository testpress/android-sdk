package `in`.testpress.util

import `in`.testpress.R
import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.FileProvider
import java.io.File

class FileDownloaderBroadcastReceiver: BroadcastReceiver() {

    lateinit var context: Context
    private val notificationId = System.currentTimeMillis().toInt()

    override fun onReceive(context: Context, intent: Intent) {
        this.context = context
        val completedDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
        handleDownloadStatus(completedDownloadId)
    }

    private fun handleDownloadStatus(downloadId: Long) {
        when (getDownloadStatus(downloadId)) {
            DownloadManager.STATUS_SUCCESSFUL -> {
                val localFilePath = getLocalFilePath(downloadId) ?: return
                val pendingIntent = createPendingIntent(localFilePath)
                val localFileName = File(Uri.parse(localFilePath).path.toString()).name
                showCompletedNotification(localFileName, pendingIntent)
            }
            DownloadManager.STATUS_FAILED -> {
                showFailedNotification()
            }
        }
    }

    private fun getDownloadStatus(downloadId: Long): Int? {
        val downloadManager = context.getSystemService(DownloadManager::class.java)
        val cursor = downloadManager.query(DownloadManager.Query().setFilterById(downloadId))
        return if (cursor.moveToFirst()) {
            cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
        } else {
            null
        }
    }

    private fun getLocalFilePath(downloadId: Long): String? {
        val downloadManager = context.getSystemService(DownloadManager::class.java)
        val cursor = downloadManager.query(DownloadManager.Query().setFilterById(downloadId))
        return if (cursor.moveToFirst()) {
            cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
        } else {
            null
        }
    }

    private fun createPendingIntent(localFilePath: String): PendingIntent {
        val intent = Intent(Intent.ACTION_VIEW)
        val pdfFile  = File(Uri.parse(localFilePath).path.toString())
        val uri = FileProvider.getUriForFile(
            context,
            context.packageName + ".provider",
            pdfFile
        )
        intent.setDataAndType(uri, "application/pdf")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun showCompletedNotification(pdfFilename: String, pendingIntent: PendingIntent) {
        createNotificationChannel()

        val builder = NotificationCompat.Builder(context, "file_download_channel").apply {
            setSmallIcon(R.drawable.ic_baseline_file_download_done_24)
            setContentTitle("PDF Download Complete")
            setContentText("$pdfFilename has been downloaded.")
            priority = NotificationCompat.PRIORITY_MIN
            setContentIntent(pendingIntent)
            setAutoCancel(true)
        }

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(notificationId, builder.build())
    }

    private fun showFailedNotification() {
        createNotificationChannel()

        val builder = NotificationCompat.Builder(context, "file_download_channel").apply {
            setSmallIcon(R.drawable.ic_baseline_error_24)
            setContentTitle("PDF Download Failed")
            setContentText("Please try again later")
            priority = NotificationCompat.PRIORITY_MIN
            setAutoCancel(true)
        }

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(notificationId, builder.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "file_download_channel",
                "File Download",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager: NotificationManager =
                context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}