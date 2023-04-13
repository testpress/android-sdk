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
import android.os.Environment
import android.os.Environment.*
import android.util.Log
import android.view.Display.Mode
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.getSystemService
import java.io.File
import java.util.*

const val LOCAL_FILE_PATH = "/storage/emulated/0/Download"



class FileDownloader(
    val context: Context,
    val fileUrl: String,
    var fileName: String
    ) {

    init {
        val downloadsDir = File("${context.getExternalFilesDir(null)?.parentFile?.parentFile?.parentFile?.parentFile}/${DIRECTORY_DOWNLOADS}")
        val downloadsPath = downloadsDir.absolutePath

        Log.d("TAG", "hihihihihih: $downloadsPath")
        Log.d("TAG", "hihihihihih: ${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)}")
        downloadAndOpenPdfFile()
    }

    private fun downloadAndOpenPdfFile() {

        // Create a DownloadManager.Request object to specify the PDF file to download
        val request = DownloadManager.Request(Uri.parse(fileUrl))
            .setTitle(fileName)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

        // Get the DownloadManager service and enqueue the download request
        val downloadManager =
            context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)

        // Create a BroadcastReceiver to listen for completion of the download
        val onComplete: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                // Get the ID of the completed download from the intent
                val completedDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

                // If the completed download matches the one we started earlier, show a notification and open the PDF file
                if (completedDownloadId == downloadId) {
                    val intent1 = Intent(Intent.ACTION_VIEW)
                    val pdfFile = File(
                        getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                        fileName
                    )
                    val uri = FileProvider.getUriForFile(
                        context,
                        context.packageName + ".provider",
                        pdfFile
                    )
                    intent1.setDataAndType(uri, "application/pdf")
                    intent1.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    val pendingIntent = PendingIntent.getActivity(
                        context,
                        0,
                        intent1,
                        PendingIntent.FLAG_UPDATE_CURRENT
                    )
                    showNotification(fileName, pendingIntent)
                }
            }
        }

        // Register the BroadcastReceiver to listen for completion of the download
        context.registerReceiver(
            onComplete,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        )
    }

    private fun showNotification(pdfFilename: String, pendingIntent: PendingIntent) {
        // Create a notification channel if necessary (for Android 8.0 and higher)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "pdf_download_channel",
                "PDF Download",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager: NotificationManager =
                context.getSystemService<NotificationManager>(
                    NotificationManager::class.java
                )
            notificationManager.createNotificationChannel(channel)
        }

        // Create a notification to show that the download is complete
        val builder: NotificationCompat.Builder =
            NotificationCompat.Builder(context, "pdf_download_channel")
                .setSmallIcon(R.drawable.testpress_tick_black)
                .setContentTitle("PDF Download Complete")
                .setContentText("$pdfFilename has been downloaded.")
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

        // Show the notification
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(1, builder.build())
    }

    companion object {
        fun changeFilenameIfAlreadyExist(fileName: String, fileType: FileType): String {
            var counter = 1
            var newFileName = "$fileName${fileType.extension}"
            var file = File(
                getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                newFileName
            )
            while (file.exists()) {
                // If the file already exists, append a counter to the filename and try again
                newFileName = "$fileName ($counter)${fileType.extension}"
                file = File(
                    getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    newFileName
                )
                counter++
            }
            return newFileName
        }
    }

    enum class FileType(val extension: String) {
        PDF(".pdf")
    }
}