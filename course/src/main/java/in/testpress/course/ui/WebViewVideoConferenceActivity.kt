package `in`.testpress.course.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import `in`.testpress.course.domain.VideoConferenceProviderType
import `in`.testpress.course.R
import `in`.testpress.course.ui.videoconference.webview.WebViewConferenceProviderHandler

class WebViewVideoConferenceActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var progressOverlay: View
    private var providerHandler: WebViewConferenceProviderHandler? = null
    private var joinUrl: String? = null
    private var title: String? = null

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001
        
        fun createIntent(context: Context, joinUrl: String, title: String?, provider: String?): Intent {
            val intent = Intent(context, WebViewVideoConferenceActivity::class.java)
            intent.putExtra("join_url", joinUrl)
            intent.putExtra("title", title)
            intent.putExtra("provider", provider)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.webview_video_conference)

        val originalUrl = intent.getStringExtra("join_url")
        val provider = intent.getStringExtra("provider")
        joinUrl = originalUrl
        title = intent.getStringExtra("title")

        supportActionBar?.title = title ?: "Meeting"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        webView = findViewById(R.id.web_view)
        progressOverlay = findViewById(R.id.progress_container)
        providerHandler = WebViewConferenceProviderHandler.create(
            VideoConferenceProviderType.fromProviderName(provider),
            ::showLoader,
            ::hideLoader
        )
        webView.visibility = View.INVISIBLE
        providerHandler?.attach(webView)

        // Check and request hardware permissions before loading
        checkPermissionsAndLoad()
    }

    private fun checkPermissionsAndLoad() {
        val permissions = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )

        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        } else {
            setupWebView()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            setupWebView()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.mediaPlaybackRequiresUserGesture = false
        
        // Spoof as desktop Chrome to bypass Teams blocking mobile WebViews
        val userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36"
        webSettings.userAgentString = userAgent

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                providerHandler?.onPageStarted(url) ?: showLoader()
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                providerHandler?.onPageFinished(url) ?: hideLoader()
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest) {
                runOnUiThread {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        val allowedResources = request.resources.filter {
                            it == PermissionRequest.RESOURCE_AUDIO_CAPTURE ||
                                it == PermissionRequest.RESOURCE_VIDEO_CAPTURE
                        }
                        if (allowedResources.isNotEmpty()) {
                            request.grant(allowedResources.toTypedArray())
                        } else {
                            request.deny()
                        }
                    }
                }
            }
        }

        joinUrl?.let {
            showLoader()
            webView.loadUrl(it)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun showLoader() {
        runOnUiThread {
            progressOverlay.visibility = View.VISIBLE
            webView.visibility = View.INVISIBLE
        }
    }

    private fun hideLoader() {
        runOnUiThread {
            progressOverlay.visibility = View.GONE
            webView.visibility = View.VISIBLE
        }
    }
}
