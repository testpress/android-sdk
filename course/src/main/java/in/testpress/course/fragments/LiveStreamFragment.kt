package `in`.testpress.course.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import `in`.testpress.course.R
import `in`.testpress.course.domain.getGreenDaoContent
import `in`.testpress.course.util.ExoPlayerUtil
import `in`.testpress.course.util.ExoplayerFullscreenHelper
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
        initializePlayerView()
        initializeExoPlayer()
    }

    private fun initializeExoplayerFullscreenHelper() {
        exoplayerFullscreenHelper = ExoplayerFullscreenHelper(activity)
        exoplayerFullscreenHelper.initializeOrientationListener()
    }

    private fun initializePlayerView() {
        exoPlayerView = view!!.findViewById(R.id.exo_player_main_frame)
        exoPlayerView.visibility = View.VISIBLE
        exoPlayerView.setAspectRatio(16f / 9f)
    }

    private fun initializeExoPlayer() {
        val streamUrl = content.liveStream?.streamUrl
        exoPlayerUtil = ExoPlayerUtil(activity, exoPlayerView, streamUrl, 0F)
        exoPlayerUtil?.setContent(content.getGreenDaoContent(requireContext()))
        exoplayerFullscreenHelper.setExoplayerUtil(exoPlayerUtil)
        exoPlayerUtil?.initializePlayer()
    }
}