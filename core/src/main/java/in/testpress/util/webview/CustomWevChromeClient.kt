package `in`.testpress.util.webview

import `in`.testpress.fragments.WebViewFragment
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.webkit.*
import android.util.Log
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.activity.result.contract.ActivityResultContracts
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CustomWevChromeClient(val fragment: WebViewFragment) : WebChromeClient() {

    private val chooserIntentResult =
        fragment.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            var results: Array<Uri>? = null
            //Check if response is positive
            if (result.resultCode == RESULT_OK) {
                if (fragment.filePathCallback == null) {
                    return@registerForActivityResult
                }
                if (result.data == null) {
                    //Capture Photo if no image available
                    if (fragment.imagePath != null) {
                        results = arrayOf(Uri.parse(fragment.imagePath))
                    }
                } else {
                    val dataString = result.data?.dataString
                    if (dataString != null) {
                        results = arrayOf(Uri.parse(dataString))
                    }
                }
            }
            fragment.filePathCallback?.onReceiveValue(results)
            fragment.filePathCallback = null
        }

    override fun onShowFileChooser(
        webView: WebView?, filePathCallback: ValueCallback<Array<Uri>?>?,
        fileChooserParams: FileChooserParams?
    ): Boolean {
        if (fragment.filePathCallback != null) {
            fragment.filePathCallback?.onReceiveValue(null)
        }
        fragment.filePathCallback = filePathCallback
        chooserIntentResult.launch(createChooserIntent(createTakePictureIntent(),createFileSelectionIntent()))
        return true
    }

    private fun createTakePictureIntent(): Intent? {
        var intent: Intent? = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent!!.resolveActivity(fragment.requireActivity().packageManager) != null) {
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
                intent.putExtra("PhotoPath", fragment.imagePath)
            } catch (ex: IOException) {
                Log.e(fragment.TAG, "Image file creation failed", ex)
            }
            if (photoFile != null) {
                fragment.imagePath = "file:" + photoFile.absolutePath
                intent.putExtra(
                    MediaStore.EXTRA_OUTPUT,
                    Uri.fromFile(photoFile)
                )
            } else {
                intent = null
            }
        }
        return intent
    }

    private fun createFileSelectionIntent(): Intent {
        return Intent(Intent.ACTION_GET_CONTENT)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .setType("*/*")
    }

    private fun createChooserIntent(takePictureIntent: Intent?,fileChooserIntent:Intent): Intent {
        val intentArray: Array<Intent?> = takePictureIntent?.let { arrayOf(it) } ?: arrayOfNulls(0)
        return Intent(Intent.ACTION_CHOOSER)
            .putExtra(Intent.EXTRA_INTENT, fileChooserIntent)
            .putExtra(Intent.EXTRA_TITLE, "Image Chooser")
            .putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)

    }

    override fun onPermissionRequest(request: PermissionRequest?) {
        val permissions = arrayOf(
            PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID,
            PermissionRequest.RESOURCE_AUDIO_CAPTURE,
            PermissionRequest.RESOURCE_VIDEO_CAPTURE
        )
        request?.grant(permissions)
    }

    // Create an image file
    @Throws(IOException::class)
    private fun createImageFile(): File? {
        @SuppressLint("SimpleDateFormat") val timeStamp =
            SimpleDateFormat("yyyyMMdd_HHmmss").format(
                Date()
            )
        val imageFileName = "img_" + timeStamp + "_"
        val storageDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }
}