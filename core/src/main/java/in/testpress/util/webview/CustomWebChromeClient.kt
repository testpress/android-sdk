package `in`.testpress.util.webview

import `in`.testpress.fragments.WebViewFragment
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.webkit.*
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CustomWebChromeClient(val fragment: WebViewFragment) : WebChromeClient() {

    private val onActivityResult = fragment.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        var results: Array<Uri>? = null

        if (result.resultCode == RESULT_OK) {
            if (fragment.filePathCallback == null) {
                return@registerForActivityResult
            }
            // Check if data is available from the result
            results = if (result.data == null) {
                capturedImageNotAvailable()
            } else {
                retrieveDataFromIntent(result.data!!)
            }
        }
        // Pass the results to the webView callback
        fragment.filePathCallback?.onReceiveValue(results)
        fragment.filePathCallback = null
    }

    private var customView: View? = null
    private var customViewCallback: CustomViewCallback? = null
    private var backCallback: OnBackPressedCallback? = null

    private fun capturedImageNotAvailable(): Array<Uri>? {
        return if (fragment.imagePath != null) {
            arrayOf(Uri.parse(fragment.imagePath))
        } else {
            null
        }
    }

    private fun retrieveDataFromIntent(intent: Intent): Array<Uri>? {
        val dataString = intent.dataString
        return if (dataString != null) {
            arrayOf(Uri.parse(dataString))
        } else {
            null
        }
    }

    override fun onShowFileChooser(
        webView: WebView?, filePathCallback: ValueCallback<Array<Uri>?>?,
        fileChooserParams: FileChooserParams?
    ): Boolean {
        if (fragment.filePathCallback != null) {
            fragment.filePathCallback?.onReceiveValue(null)
        }
        fragment.filePathCallback = filePathCallback
        val takePictureIntent = createTakePictureIntent()
        val fileSelectionIntent = createFileSelectionIntent()
        val chooserIntent = createChooserIntent(takePictureIntent, fileSelectionIntent)
        onActivityResult.launch(chooserIntent)
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

    override fun onShowCustomView(view: View, callback: CustomViewCallback) {
        if (customView != null) {
            callback.onCustomViewHidden()
            return
        }

        customView = view
        customViewCallback = callback

        // Add the fullscreen view
        (fragment.requireActivity().window.decorView as FrameLayout).addView(
            view,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        // Change to landscape orientation
        fragment.requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

        hideSystemUI()

        fragment.webView.visibility = View.GONE

        backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (customView != null) {
                    customViewCallback?.onCustomViewHidden()
                } else {
                    isEnabled = false
                    fragment.requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        }
        fragment.requireActivity().onBackPressedDispatcher.addCallback(fragment.viewLifecycleOwner, backCallback!!)
    }


    override fun onHideCustomView() {
        if (customView == null) return

        // Remove the fullscreen view
        (fragment.requireActivity().window.decorView as FrameLayout).removeView(customView)
        customView = null
        customViewCallback?.onCustomViewHidden()

        // Restore to user's default orientation
        fragment.requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

        showSystemUI()

        fragment.webView.visibility = View.VISIBLE

        backCallback?.remove()
        backCallback = null
    }

    private fun hideSystemUI() {
        fragment.requireActivity().window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_FULLSCREEN
    }

    private fun showSystemUI() {
        fragment.requireActivity().window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    }
}