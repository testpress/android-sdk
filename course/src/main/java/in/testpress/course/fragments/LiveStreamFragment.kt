package `in`.testpress.course.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import `in`.testpress.course.R
import `in`.testpress.course.domain.getGreenDaoContent
import `in`.testpress.course.fragments.FermionLiveStreamFragment.Companion.ARG_STREAM_URL
import `in`.testpress.course.fragments.FermionLiveStreamFragment.Companion.ARG_TITLE
import `in`.testpress.course.util.ExoPlayerUtil
import `in`.testpress.course.util.ExoplayerFullscreenHelper
import `in`.testpress.fragments.WebViewFragment
import `in`.testpress.fragments.WebViewFragment.Companion.IS_AUTHENTICATION_REQUIRED
import `in`.testpress.fragments.WebViewFragment.Companion.URL_TO_OPEN
import `in`.testpress.util.makeHeadRequest
import android.os.Handler
import android.os.Looper
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import kotlinx.coroutines.*
import java.lang.Runnable


class LiveStreamFragment : BaseContentDetailFragment(), LiveStreamCallbackListener {
    override var isBookmarkEnabled: Boolean = false
    private lateinit var exoPlayerView: AspectRatioFrameLayout
    private lateinit var exoplayerFullscreenHelper: ExoplayerFullscreenHelper
    private var exoPlayerUtil: ExoPlayerUtil? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var pendingFermionLaunch = false

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (pendingFermionLaunch && results.values.all { it }) {
            pendingFermionLaunch = false
            showFermionPlayer()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_live_stream, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeExoplayerFullscreenHelper()
    }

    override fun display() {
        when (content.liveStream?.status) {
            "Running" -> {
                displayPlayerViewWithChat()
            }
            "Not Started" -> displayNotStartedNotice()
            "Completed" -> displayEndedNotice()
        }
    }

    private fun isFermionProvider(): Boolean {
        return content.liveStream?.provider == FERMION_PROVIDER
    }

    private fun initializeExoplayerFullscreenHelper() {
        exoplayerFullscreenHelper = ExoplayerFullscreenHelper(activity)
        exoplayerFullscreenHelper.initializeOrientationListener()
    }

    private fun displayPlayerViewWithChat(){
        if (isFermionProvider()) {
            requestCameraAndMicPermissions()
        } else {
            displayExoPlayerWithChat()
        }
    }

    private fun displayExoPlayerWithChat() {
        initializePlayerView()
        initializeExoPlayer()
        setupChatWebView()
        viewModel.createContentAttempt(contentId)
        swipeRefresh.isEnabled = false
        hideBottomNavigationBar()
    }

    private fun requestCameraAndMicPermissions() {
        val permissions = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(Manifest.permission.CAMERA)
        }
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(Manifest.permission.RECORD_AUDIO)
        }
        if (permissions.isEmpty()) {
            showFermionPlayer()
        } else {
            pendingFermionLaunch = true
            requestPermissionsLauncher.launch(permissions.toTypedArray())
        }
    }

    private fun showFermionPlayer() {
        setupFermionPlayer()
        setupChatWebView()
        viewModel.createContentAttempt(contentId)
        swipeRefresh.isEnabled = false
        hideBottomNavigationBar()
    }

    private fun setupFermionPlayer() {
        val streamUrl = content.liveStream?.streamUrl ?: return
        val container = view?.findViewById<ViewGroup>(R.id.fermion_player_container) ?: return
        container.visibility = View.VISIBLE

        val fermionFragment = FermionLiveStreamFragment()
        fermionFragment.arguments = Bundle().apply {
            putString(ARG_STREAM_URL, streamUrl)
            putString(ARG_TITLE, content.title ?: "")
        }
        childFragmentManager.beginTransaction()
            .replace(R.id.fermion_player_container, fermionFragment)
            .commitAllowingStateLoss()
    }

    private fun displayNotStartedNotice(){
        emptyViewFragment.setEmptyText(
            R.string.waiting_for_live_stream_title,
            R.string.waiting_for_live_stream_desc,
            null)
    }

    private fun displayEndedNotice(){
        val description = if (content.liveStream?.showRecordedVideo == true) {
            R.string.live_stream_concluded_desc
        } else {
            R.string.live_stream_concluded_no_recording_desc
        }
        emptyViewFragment.setEmptyText(R.string.live_stream_finished, description, null)
        emptyViewFragment.showOrHideButton(show = false)
    }

    private fun initializePlayerView() {
        exoPlayerView = view!!.findViewById(R.id.exo_player_main_frame)
        exoPlayerView.visibility = View.VISIBLE
        exoPlayerView.setAspectRatio(16f / 9f)
        exoPlayerView.findViewById<TextView>(R.id.exo_duration).visibility = View.GONE
        exoPlayerView.findViewById<RelativeLayout>(R.id.live_label).visibility = View.VISIBLE
    }

    private fun initializeExoPlayer() {
        val streamUrl = content.liveStream?.streamUrl
        exoPlayerUtil = ExoPlayerUtil(activity, exoPlayerView, streamUrl, 0F, this)
        exoPlayerUtil?.setContent(content.getGreenDaoContent(requireContext()))
        exoplayerFullscreenHelper.setExoplayerUtil(exoPlayerUtil)
        exoPlayerUtil?.initializePlayer()
    }

    private fun setupChatWebView() {
        content.liveStream?.chatEmbedUrl?.let { embedUrl ->
            val chatView = view?.findViewById<View>(R.id.chat_view_fragment)
            chatView?.visibility = View.VISIBLE

            val webViewFragment = WebViewFragment()
            val bundle = Bundle().apply {
                this.putString(URL_TO_OPEN,embedUrl)
                this.putBoolean(IS_AUTHENTICATION_REQUIRED,true)
            }
            webViewFragment.arguments = bundle
            childFragmentManager.beginTransaction()
                .replace(R.id.chat_view_fragment, webViewFragment)
                .commit()
        }
    }

    private fun hideBottomNavigationBar(){
       val bottomNavigationBar =  view?.findViewById<View>(R.id.bottom_navigation_fragment)
        bottomNavigationBar?.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        exoPlayerUtil?.onResume()
    }

    override fun onStart() {
        super.onStart()
        exoPlayerUtil?.onStart()
    }

    override fun onStop() {
        super.onStop()
        exoPlayerUtil?.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        exoplayerFullscreenHelper?.disableOrientationListener()
    }

    override fun onUrlNullOrEmpty() {
        val handler = Handler(Looper.getMainLooper())
        val reloadContent = Runnable {
            if(this.isAdded){
                forceReloadContent()
            }
        }
        handler.postDelayed(reloadContent, 15000)
    }

    override fun onUrlReturnError(url: String) {
        coroutineScope.launch {
            requestWithRetry(url, 5000)
        }
    }

    private suspend fun requestWithRetry(url: String, delay: Long) {
        while (coroutineScope.isActive) {
            try {
                val responseCode = makeHeadRequest(url)
                if (responseCode == 200) {
                    withContext(Dispatchers.Main) {
                        forceReloadContent()
                    }
                    return
                }
            } catch (e: Exception) {
                println("Request failed: ${e.message}")
            }
            delay(delay)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        coroutineScope.cancel()
    }

    companion object {
        private const val FERMION_PROVIDER = "Fermion"
    }
}

interface LiveStreamCallbackListener {
    fun onUrlNullOrEmpty()
    fun onUrlReturnError(url: String)
}