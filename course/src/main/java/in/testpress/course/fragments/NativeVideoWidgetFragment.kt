package `in`.testpress.course.fragments

import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.R
import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.domain.getGreenDaoContent
import `in`.testpress.enums.Status
import `in`.testpress.course.ui.ContentActivity.CONTENT_ID
import `in`.testpress.course.util.ExoPlayerUtil
import `in`.testpress.course.util.ExoplayerFullscreenHelper
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout

class NativeVideoWidgetFragment : BaseVideoWidgetFragment() {
    private lateinit var exoPlayerMainFrame: AspectRatioFrameLayout
    private var exoPlayerUtil: ExoPlayerUtil? = null

    private val exoplayerFullscreenHelper: ExoplayerFullscreenHelper by lazy {
        ExoplayerFullscreenHelper(activity)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.native_video_widget, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        val contentId = requireArguments().getLong(CONTENT_ID)
        viewModel.getContent(contentId).observe(viewLifecycleOwner, Observer {
            when (it?.status) {
                Status.SUCCESS -> {
                    createAttemptAndInitializeExoplayer(it.data!!)
                }
                else -> {}
            }
        })
    }

    fun bindViews(view: View) {
        exoPlayerMainFrame = view.findViewById(R.id.exo_player_main_frame)
        exoPlayerMainFrame.visibility = View.VISIBLE
        exoPlayerMainFrame.setAspectRatio(16f/9f)
        exoplayerFullscreenHelper.initializeOrientationListener()
    }

    private fun createAttemptAndInitializeExoplayer(content: DomainContent) {
        val greenDaoContent = content.getGreenDaoContent(requireContext())
        val video = content.video
        exoPlayerUtil = ExoPlayerUtil(activity, exoPlayerMainFrame, video?.getPlaybackURL(), 0F)
        exoPlayerUtil?.setContent(greenDaoContent!!)
        exoplayerFullscreenHelper.setExoplayerUtil(exoPlayerUtil)

        viewModel.createContentAttempt(content.id)
            .observe(viewLifecycleOwner, Observer { resource ->
                when(resource.status) {
                    Status.SUCCESS -> {
                        val contentAttempt = resource.data!!
                        val videoStartPosition = contentAttempt.video?.lastPosition?.toFloat() ?: 0F
                        val contentAttemptObjectId = contentAttempt.objectId?.toLong()
                        exoPlayerUtil?.setStartPosition(videoStartPosition)
                        exoPlayerUtil?.setVideoAttemptParameters(contentAttemptObjectId?:-1, greenDaoContent!!)
                        exoPlayerUtil?.initializePlayer()
                    }
                    else -> {
                        exoPlayerUtil?.setVideoAttemptParameters(-1, greenDaoContent!!)
                        exoPlayerUtil?.initializePlayer()
                    }
                }

            })
    }

    override fun seekTo(milliSeconds: Long?) {
        exoPlayerUtil?.seekTo(milliSeconds)
    }

    override fun onPause() {
        super.onPause()
        exoPlayerUtil?.onPause()
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