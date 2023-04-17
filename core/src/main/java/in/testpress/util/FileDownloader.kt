package `in`.testpress.util

import `in`.testpress.R
import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment.*
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.FileProvider
import androidx.core.content.getSystemService
import java.io.File


class FileDownloader(
    private val context: Context,
    private val fileUrl: String,
    private var fileName: String
) {

    fun downloadFile() {

        val request = DownloadManager.Request(Uri.parse(fileUrl)).apply {
            setTitle(fileName)
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            setAllowedOverRoaming(true)
            setDestinationInExternalPublicDir(DIRECTORY_DOWNLOADS, fileName)
        }

        val downloadManager = context.getSystemService<DownloadManager>()
        val downloadId = downloadManager?.enqueue(request) ?: return

        val onComplete: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val completedDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

                if (completedDownloadId == downloadId) {
                    when (checkDownloadStatus(completedDownloadId)) {
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            val pendingIntent = createPendingIntentToLaunchActivity()
                            showCompleteNotification(fileName, pendingIntent)
                        }
                        DownloadManager.STATUS_FAILED -> {
                            showFailedNotification()
                        }
                    }
                }
            }
        }

        context.registerReceiver(
            onComplete,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        )
    }

    private fun checkDownloadStatus(downloadId: Long): Int? {
        val downloadManager = context.getSystemService(DownloadManager::class.java)
        val cursor = downloadManager.query(DownloadManager.Query().setFilterById(downloadId))

        return if (cursor.moveToFirst()) {
            cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
        } else {
            null
        }
    }

    private fun createPendingIntentToLaunchActivity(): PendingIntent {
        val intent = Intent(Intent.ACTION_VIEW)
        val pdfFile = File(
            getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS),
            fileName
        )
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
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun showCompleteNotification(pdfFilename: String, pendingIntent: PendingIntent) {
        createNotificationChannel()

        val builder = NotificationCompat.Builder(context, "pdf_download_channel").apply {
            setSmallIcon(R.drawable.testpress_tick_black)
            setContentTitle("PDF Download Complete")
            setContentText("$pdfFilename has been downloaded.")
            priority = NotificationCompat.PRIORITY_MIN
            setContentIntent(pendingIntent)
            setAutoCancel(true)
        }

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(1, builder.build())
    }

    private fun showFailedNotification() {
        createNotificationChannel()

        val builder = NotificationCompat.Builder(context, "pdf_download_channel").apply {
            setSmallIcon(R.drawable.testpress_tick_black)
            setContentTitle("PDF Download Failed")
            setContentText("Please try again later")
            priority = NotificationCompat.PRIORITY_MIN
            setAutoCancel(true)
        }

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(1, builder.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "pdf_download_channel",
                "PDF Download",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager: NotificationManager =
                context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        fun changeFilenameIfAlreadyExist(fileName: String, fileType: FileType): String {
            var counter = 1
            var newFileName = "$fileName${fileType.extension}"
            val downloadsDirectory = getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS)
            var file = File(downloadsDirectory, newFileName)
            while (file.exists()) {
                newFileName = "$fileName ($counter)${fileType.extension}"
                file = File(downloadsDirectory, newFileName)
                counter++
            }
            return newFileName
        }
    }

    enum class FileType(val extension: String) {
        PDF(".pdf")
    }
}