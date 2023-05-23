package `in`.testpress.fragments

import `in`.testpress.R
import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.core.TestpressSdk
import `in`.testpress.databinding.WebviewFragmentBinding
import `in`.testpress.models.InstituteSettings
import `in`.testpress.models.SSOUrl
import `in`.testpress.network.TestpressApiClient
import `in`.testpress.util.ActivityUtil
import `in`.testpress.util.BaseJavaScriptInterface
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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import kotlinx.parcelize.Parcelize
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class WebViewFragment(
    var url: String = "",
    val data: String = "",
    val webViewFragmentSettings: Settings
) : Fragment() {

    private val TAG = "WebViewFragment"
    private var _layout: WebviewFragmentBinding? = null
    private val layout: WebviewFragmentBinding get() = _layout!!
    private lateinit var webView: WebView
    private lateinit var instituteSettings: InstituteSettings
    private var listener : Listener? = null
    private var mCM: String? = null
    private var mUM: ValueCallback<Uri?>? = null
    private var mUMA: ValueCallback<Array<Uri>?>? = null
    private val FCR = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instituteSettings = TestpressSdk.getTestpressSession(requireContext())?.instituteSettings!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _layout = WebviewFragmentBinding.inflate(inflater, container, false)
        return layout.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showLoading()
        initializedSwipeRefresh()
        webView = layout.webView
        listener?.onWebViewInitializationSuccess()
        setupCookieManager()
        setupWebViewSettings()
        setupWebViewClient()
        setupWebChromeClient()
        loadContent()
    }

    private fun initializedSwipeRefresh(){
        layout.swipeRefreshLayout.setColorSchemeColors(
            ContextCompat.getColor(requireContext(), R.color.testpress_color_primary),
        )
        layout.swipeRefreshLayout.setOnRefreshListener {
            webView.loadUrl(webView.url.toString())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _layout = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)

        if (Build.VERSION.SDK_INT >= 21) {
            var results: Array<Uri>? = null

            //Check if response is positive
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == FCR) {

                    if (mUMA == null) {
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
                if (mUM == null) return
                val result = if (intent == null || resultCode != RESULT_OK) null else intent.data
                mUM?.onReceiveValue(result)
                mUM = null
            }
        }
    }

    private fun loadContent(){
        if (webViewFragmentSettings.isSSORequired){
            fetchSsoLink()
            return
        }
        loadContentInWebView(url = url, data = data)
    }

    private fun fetchSsoLink() {
        showLoading()
        TestpressApiClient(requireContext(), TestpressSdk.getTestpressSession(requireContext())).ssourl
            .enqueue(object : TestpressCallback<SSOUrl>() {
                override fun onSuccess(result: SSOUrl?) {
                    url = "${instituteSettings.baseUrl}${result?.ssoUrl}&next=$url"
                    loadContentInWebView(url = url)
                }

                override fun onException(exception: TestpressException?) {
                    hideLoading()
                    listener?.onError(exception)
                }
            })
    }

    private fun showLoading() {
        layout.pbLoading.visibility = View.VISIBLE
        layout.webView.visibility = View.GONE
    }

    private fun hideLoading() {
        layout.pbLoading.visibility = View.GONE
        layout.webView.visibility = View.VISIBLE
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
        webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
    }

    private fun setupWebViewClient(){
        webView.webViewClient = object : WebViewClient(){
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                return if (instituteSettings.isInstituteUrl(request?.url.toString())){
                    false
                } else {
                    if (webViewFragmentSettings.allowNonInstituteUrlInWebView){
                        false
                    } else {
                        ActivityUtil.openUrl(requireContext(),request?.url.toString())
                        true
                    }
                }
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                if (webViewFragmentSettings.showLoadingBetweenPages){
                    showLoading()
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                layout.swipeRefreshLayout.isRefreshing = false
                hideLoading()
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                //TODO("Need implementation")
                listener?.onError(TestpressException.unexpectedError(Exception("WebView error")))
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

            override fun onPermissionRequest(request: PermissionRequest?) {
                val permissions = arrayOf(
                    PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID,
                    PermissionRequest.RESOURCE_AUDIO_CAPTURE,
                    PermissionRequest.RESOURCE_VIDEO_CAPTURE
                )
                request?.grant(permissions)
            }
        }
    }

    private fun loadContentInWebView(url: String = "", data: String = "") {
        if (url.isNotEmpty()){
            webView.loadUrl(url)
        } else if (data.isNotEmpty()){
            webView.loadData(data,"text/html", null)
        } else {
            // If both the URL and data are empty, pass an unexpected error
            listener?.onError(TestpressException.unexpectedError(Exception("URL not found and data not found.")))
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

    fun setListener(listener: Listener){
        this.listener = listener
    }

    fun addJavascriptInterface(javascriptInterface: BaseJavaScriptInterface, name: String){
        webView.addJavascriptInterface(javascriptInterface,name)
    }

    fun canGoBack(): Boolean {
        return webView.canGoBack()
    }

    fun goBack() {
        webView.goBack()
    }

    fun retryLoad(){
        showLoading()
        if (!webView.url.isNullOrEmpty()){
            webView.loadUrl(webView.url!!)
        } else {
            loadContent()
        }
    }

    @Parcelize
    data class Settings(
        val showLoadingBetweenPages: Boolean = false,
        val isSSORequired: Boolean = true,
        val allowNonInstituteUrlInWebView: Boolean = true
    ) : Parcelable

    interface Listener {
        fun onWebViewInitializationSuccess()
        fun onError(exception: TestpressException?)
    }

}