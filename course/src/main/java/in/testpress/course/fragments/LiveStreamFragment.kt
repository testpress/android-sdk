package `in`.testpress.course.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import `in`.testpress.course.R
import `in`.testpress.course.domain.getGreenDaoContent
import `in`.testpress.course.util.ExoPlayerUtil
import `in`.testpress.course.util.ExoplayerFullscreenHelper
import `in`.testpress.fragments.WebViewFragment
import `in`.testpress.fragments.WebViewFragment.Companion.IS_AUTHENTICATION_REQUIRED
import `in`.testpress.fragments.WebViewFragment.Companion.URL_TO_OPEN
import android.widget.RelativeLayout
import android.widget.TextView
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout


class LiveStreamFragment : BaseContentDetailFragment() {
    override var isBookmarkEnabled: Boolean = false
    private lateinit var exoPlayerView: AspectRatioFrameLayout
    private lateinit var exoplayerFullscreenHelper: ExoplayerFullscreenHelper
    private var exoPlayerUtil: ExoPlayerUtil? = null

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
                if (content.liveStream?.streamUrl.isNullOrEmpty()) {
                    displayNotStartedNotice()
                } else {
                    displayPlayerViewWithChat()
                }
            }
            "Not Started" -> displayNotStartedNotice()
            "Completed" -> displayEndedNotice()
        }
    }

    private fun initializeExoplayerFullscreenHelper() {
        exoplayerFullscreenHelper = ExoplayerFullscreenHelper(activity)
        exoplayerFullscreenHelper.initializeOrientationListener()
    }

    private fun displayPlayerViewWithChat(){
        initializePlayerView()
        initializeExoPlayer()
        setupChatWebView()

        // Disabling swipe to refresh because  it prevents users from scrolling the chat
        // and temporarily hiding the bottom navigation as it hides the chat's send button..
        swipeRefresh.isEnabled = false
        hideBottomNavigationBar()
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
        exoPlayerUtil = ExoPlayerUtil(activity, exoPlayerView, streamUrl, 0F)
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
                this.putBoolean(IS_AUTHENTICATION_REQUIRED,false)
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
}