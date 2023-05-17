package `in`.testpress.fragments

import `in`.testpress.databinding.WebviewFragmentBinding
import `in`.testpress.util.ActivityUtil
import `in`.testpress.util.ViewUtils
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import kotlinx.parcelize.Parcelize
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class WebViewFragment(
    private val javaScriptInterfaceWithNameList: List<Pair<BaseJavaScriptInterface,String>>? = null
) : Fragment() {
    companion object {
        const val URL_TO_OPEN = "URL"
    }

    private val TAG = "WebViewFragment"
    private var _binding: WebviewFragmentBinding? = null
    private val binding: WebviewFragmentBinding get() = _binding!!
    private lateinit var webView: WebView
    private var listener : Listener? = null
    private var url = ""
    private var mCM: String? = null
    private var mUM: ValueCallback<Uri?>? = null
    private var mUMA: ValueCallback<Array<Uri>?>? = null
    private val FCR = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parseArguments()
    }

    private fun parseArguments() {
        url = arguments!!.getString(URL_TO_OPEN) ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = WebviewFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        webView = binding.webView
        setupCookieManager()
        setupWebViewSettings()
        setupWebViewClient()
        setupWebChromeClient()
        setJavaScriptInterface()
        loadWebView()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)

        if (Build.VERSION.SDK_INT >= 21) {
            var results: Array<Uri>? = null

            //Check if response is positive
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == FCR) {

                    if (null == mUMA) {
                        return
                    }
                    if (intent == null) {
                        //Capture Photo if no image available
                        if (mCM != null) {
                            results = arrayOf(Uri.parse(mCM))
                        }
                    } else {
                        val dataString = intent.dataString
                        if (dataString != null) {
                            results = arrayOf(Uri.parse(dataString))
                        }
                    }
                }
            }
            mUMA?.onReceiveValue(results)
            mUMA = null
        } else {

            if (requestCode == FCR) {
                if (null == mUM) return
                val result = if (intent == null || resultCode != RESULT_OK) null else intent.data
                mUM?.onReceiveValue(result)
                mUM = null
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun setupCookieManager(){
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.removeAllCookies(null)
    }

    private fun setupWebViewSettings() {
        webView.settings.javaScriptEnabled = true
        webView.settings.allowFileAccess = true
        webView.settings.useWideViewPort = false
        webView.settings.loadWithOverviewMode = true
        // Allow use of Local Storage
        webView.settings.domStorageEnabled = true
        // Hide the zoom controls for HONEYCOMB+
        webView.settings.displayZoomControls = false
        // Enable pinch to zoom without the zoom buttons
        webView.settings.builtInZoomControls = false
    }

    private fun setupWebViewClient(){
        webView.webViewClient = object : WebViewClient(){
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                if (!(listener?.shouldOverrideUrlLoading(view, request)!!)) {
                    view?.loadUrl(request?.url.toString())
                } else {
                    ActivityUtil.openUrl(requireContext(),request?.url.toString())
                }
                return true
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                listener?.onPageStarted(view,url,favicon)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                listener?.onPageFinished(view,url)
            }
        }
    }

    private fun setupWebChromeClient() {
        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?, filePathCallback: ValueCallback<Array<Uri>?>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                if (mUMA != null) {
                    mUMA?.onReceiveValue(null)
                }
                mUMA = filePathCallback
                var takePictureIntent: Intent? = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if (takePictureIntent!!.resolveActivity(this@WebViewFragment.requireActivity().packageManager) != null) {
                    var photoFile: File? = null
                    try {
                        photoFile = createImageFile()
                        takePictureIntent.putExtra("PhotoPath", mCM)
                    } catch (ex: IOException) {
                        Log.e(TAG, "Image file creation failed", ex)
                    }
                    if (photoFile != null) {
                        mCM = "file:" + photoFile.absolutePath
                        takePictureIntent!!.putExtra(
                            MediaStore.EXTRA_OUTPUT,
                            Uri.fromFile(photoFile)
                        )
                    } else {
                        takePictureIntent = null
                    }
                }
                val contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT)
                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE)
                contentSelectionIntent.type = "*/*"
                val intentArray: Array<Intent?> =
                    takePictureIntent?.let { arrayOf(it) } ?: arrayOfNulls(0)
                val chooserIntent = Intent(Intent.ACTION_CHOOSER)
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
                chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser")
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)
                startActivityForResult(chooserIntent, FCR)
                return true
            }
        }
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

    private fun setJavaScriptInterface() {
        if (javaScriptInterfaceWithNameList == null) return
        for (javaScriptInterfaceWithName in javaScriptInterfaceWithNameList) {
            webView.addJavascriptInterface(
                javaScriptInterfaceWithName.first,
                javaScriptInterfaceWithName.second
            )
        }
    }

    private fun loadWebView() {
        webView.loadUrl(url)
    }

    fun setListener(listener: Listener){
        this.listener = listener
    }

    fun canGoBack(): Boolean {
        return webView.canGoBack()
    }

    fun goBack() {
        webView.goBack()
    }

    interface Listener {
        fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean
        fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?)
        fun onPageFinished(view: WebView?, url: String?)
    }

    class BaseJavaScriptInterface

}