package `in`.testpress.course.fragments

import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.R
import `in`.testpress.course.TestpressCourse
import `in`.testpress.course.di.InjectorUtils
import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.domain.getGreenDaoContent
import `in`.testpress.course.enums.Status
import `in`.testpress.course.ui.ContentActivity.CONTENT_ID
import `in`.testpress.course.util.ExoPlayerUtil
import `in`.testpress.course.util.ExoplayerFullscreenHelper
import `in`.testpress.course.viewmodels.ContentViewModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class NativeVideoWidgetFragment : Fragment() {
    private lateinit var exoPlayerMainFrame: FrameLayout
    private var exoPlayerUtil: ExoPlayerUtil? = null

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    lateinit var viewModel: ContentViewModel
    private val session by lazy { TestpressSdk.getTestpressSession(requireActivity()) }
    private val exoplayerFullscreenHelper: ExoplayerFullscreenHelper by lazy {
        ExoplayerFullscreenHelper(activity)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val contentType = requireArguments().getString(TestpressCourse.CONTENT_TYPE)
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ContentViewModel(
                    InjectorUtils.getContentRepository(contentType!!, context!!)
                ) as T
            }
        }).get(ContentViewModel::class.java)
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
            }
        })
    }

    fun bindViews(view: View) {
        exoPlayerMainFrame = view.findViewById(R.id.exo_player_main_frame)
        exoPlayerMainFrame.visibility = View.VISIBLE
        exoplayerFullscreenHelper.initializeOrientationListener()
    }

    private fun createAttemptAndInitializeExoplayer(content: DomainContent) {
        val greenDaoContent = content.getGreenDaoContent(requireContext())
        val video = content.video
        viewModel.createContentAttempt(content.id)
            .observe(viewLifecycleOwner, Observer { resource ->
                val videoAttempt = resource.data!!
                exoPlayerUtil = ExoPlayerUtil(activity, exoPlayerMainFrame, video?.hlsUrl(), 0F)
                exoPlayerUtil?.setVideoAttemptParameters(videoAttempt.id, greenDaoContent!!)
                exoPlayerUtil?.initializePlayer()
                exoplayerFullscreenHelper.setExoplayerUtil(exoPlayerUtil)
            })
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