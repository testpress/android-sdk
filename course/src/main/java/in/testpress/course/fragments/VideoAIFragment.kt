package `in`.testpress.course.fragments

import `in`.testpress.course.R
import `in`.testpress.course.util.VideoAIJsInterface
import `in`.testpress.course.ui.videocontent.webview.VideoAIWebView
import `in`.testpress.util.webview.BaseWebChromeClient
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope

class VideoAIFragment : Fragment(), VideoAIJsInterface.Host {

    companion object {
        const val ARG_ASSET_ID = "assetId"
        const val ARG_NOTES_URL = "notesUrl"

        fun newInstance(assetId: String, notesUrl: String?): VideoAIFragment {
            return VideoAIFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_ASSET_ID, assetId)
                    putString(ARG_NOTES_URL, notesUrl ?: "")
                }
            }
        }
    }

    interface Host {
        fun onVideoAISeek(seconds: Double)
        fun onVideoAICloseRequested()
    }

    private var progressBar: ProgressBar? = null
    private var container: FrameLayout? = null
    private lateinit var webChromeClient: BaseWebChromeClient
    private var videoAIWebView: VideoAIWebView? = null
    private var assetId: String = ""
    private var notesUrl: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.learnlens_video_ai_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        parseArguments()
        
        if (assetId.isBlank()) {
            findHost()?.onVideoAICloseRequested()
            return
        }

        setupAndLoadWebView()
    }

    private fun initViews(view: View) {
        progressBar = view.findViewById(R.id.pb_loading)
        container = view.findViewById(R.id.webview_container)
        webChromeClient = BaseWebChromeClient(this)
    }

    private fun parseArguments() {
        arguments?.let {
            assetId = it.getString(ARG_ASSET_ID).orEmpty()
            notesUrl = it.getString(ARG_NOTES_URL).orEmpty()
        }
    }

    private fun setupAndLoadWebView() {
        val targetContainer = container ?: return
        val act = activity ?: return

        val webView = videoAIWebView ?: VideoAIWebView(
            activity = act,
            scope = viewLifecycleOwner.lifecycleScope,
            jsHost = this,
            isViewActive = { isAdded },
            onLoadingChanged = { isLoading -> toggleLoadingState(isLoading) },
            webChromeClient = webChromeClient,
        ).also { videoAIWebView = it }

        webView.attach(targetContainer)
        webView.mount(assetId, notesUrl)
    }

    private fun toggleLoadingState(isLoading: Boolean) {
        progressBar?.isVisible = isLoading
        container?.isVisible = !isLoading
    }

    private fun findHost(): Host? {
        return (parentFragment as? Host) ?: (activity as? Host)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        webChromeClient.cleanup()
        videoAIWebView?.destroy()
        videoAIWebView = null
        progressBar = null
        container = null
    }

    override fun onSeek(seconds: Double) {
        findHost()?.onVideoAISeek(seconds)
    }

    override fun onRequestClose() {
        findHost()?.onVideoAICloseRequested()
    }
}
