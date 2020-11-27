package `in`.testpress.course.fragments

import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.R
import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.domain.getGreenDaoContent
import `in`.testpress.enums.Status
import `in`.testpress.course.ui.ContentActivity.CONTENT_ID
import `in`.testpress.course.util.DoubleClickListener
import `in`.testpress.course.util.ExoPlayerUtil
import `in`.testpress.course.util.ExoplayerFullscreenHelper
import android.os.Bundle
import android.view.*
import androidx.lifecycle.Observer
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import kotlinx.android.synthetic.main.custom_exoplayer_video_control.*

class NativeVideoWidgetFragment : BaseVideoWidgetFragment() {
    private lateinit var exoPlayerMainFrame: AspectRatioFrameLayout
    private var exoPlayerUtil: ExoPlayerUtil? = null

    private val session by lazy { TestpressSdk.getTestpressSession(requireActivity()) }
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
        setOnClickListener()
        val contentId = requireArguments().getLong(CONTENT_ID)
        viewModel.getContent(contentId).observe(viewLifecycleOwner, Observer {
            when (it?.status) {
                Status.SUCCESS -> {
                    createAttemptAndInitializeExoplayer(it.data!!)
                }
            }
        })
    }

    private fun setOnClickListener() {
        fastForward.setOnClickListener(DoubleClickListener(
                callback = object : DoubleClickListener.Callback {
                    override fun doubleClicked() {
                        fastForward(10000)
                    }
                }))
        backward.setOnClickListener(DoubleClickListener(
                callback = object : DoubleClickListener.Callback {
                    override fun doubleClicked() {
                        backward(10000)
                    }
                }))
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
        exoPlayerUtil = ExoPlayerUtil(activity, exoPlayerMainFrame, video?.hlsUrl(), 0F)
        exoplayerFullscreenHelper.setExoplayerUtil(exoPlayerUtil)

        viewModel.createContentAttempt(content.id)
            .observe(viewLifecycleOwner, Observer { resource ->
                when(resource.status) {
                    Status.SUCCESS -> {
                        val contentAttempt = resource.data!!
                        val videoStartPosition = contentAttempt.video?.lastPosition?.toFloat() ?: 0F
                        exoPlayerUtil?.setStartPosition(videoStartPosition)
                        exoPlayerUtil?.setVideoAttemptParameters(contentAttempt.objectId!!.toLong(), greenDaoContent!!)
                        exoPlayerUtil?.initializePlayer()
                    }
                    else -> exoPlayerUtil?.initializePlayer()
                }

            })
    }

    override fun seekTo(milliSeconds: Long?) {
        exoPlayerUtil?.seekTo(milliSeconds)
    }

    override fun fastForward(milliSeconds: Long?) {
        exoPlayerUtil?.fastForward(milliSeconds)
    }

    override fun backward(milliSeconds: Long?) {
        exoPlayerUtil?.backward(milliSeconds)
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