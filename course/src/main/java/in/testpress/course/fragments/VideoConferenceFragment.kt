package `in`.testpress.course.fragments

import `in`.testpress.core.TestpressSdk
import `in`.testpress.core.TestpressUserDetails
import `in`.testpress.course.R
import `in`.testpress.course.domain.DomainVideoConferenceContent
import `in`.testpress.course.util.VideoConferenceHandler
import `in`.testpress.course.util.VideoConferenceInitializeListener
import `in`.testpress.models.ProfileDetails
import `in`.testpress.util.ViewUtils
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.auth0.android.jwt.JWT
import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import io.sentry.Sentry

class VideoConferenceFragment : BaseContentDetailFragment() {
    private lateinit var titleView: TextView
    private lateinit var titleLayout: LinearLayout
    private lateinit var startButton: Button
    private lateinit var duration: TextView
    private lateinit var startTime: TextView
    private lateinit var endMessageTitle:TextView
    private lateinit var endMessageDescription: TextView
    private var videoConferenceHandler: VideoConferenceHandler? = null
    private var profileDetails: ProfileDetails? = null
    private var reloadContent = 0
    private val maxReloadContent = 3
    private var isRetryingAfterFailure = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        profileDetails = TestpressUserDetails.getInstance().profileDetails
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.video_conference_content_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
    }

    private fun bindViews(view: View) {
        titleView = view.findViewById(R.id.title)
        titleLayout = view.findViewById(R.id.title_layout)
        startButton = view.findViewById(R.id.start_button)
        duration = view.findViewById(R.id.duration)
        startTime = view.findViewById(R.id.start_time)
        endMessageTitle = view.findViewById(R.id.video_conference_title)
        endMessageDescription = view.findViewById(R.id.video_conference_description)
        ViewUtils.setTypeface(
            arrayOf(titleView, startTime, duration, endMessageTitle, endMessageDescription),
            TestpressSdk.getRubikMediumFont(requireActivity())
        )
    }

    override fun display() {
        val videoConference = content.videoConference

        titleView.text = content.title
        titleLayout.visibility = View.VISIBLE
        videoConference?.let {
            duration.text = it.duration.toString()
            startTime.text = it.formattedStartDate()
        }

        if (videoConference?.isEnded() == true) {
            view?.findViewById<LinearLayout>(R.id.video_conference_ended_layout)?.isVisible = true
            return
        }

        videoConference?.accessToken?.let { token ->
            try {
                if (JWT(token).isExpired(10) && reloadContent < maxReloadContent) {
                    reloadContent++
                    forceReloadContent()
                    return
                }
            } catch (e: Exception) {
                Sentry.captureException(e)
                return
            }
        }

        initializeVideoConferenceHandler(videoConference)

        startButton.visibility = View.VISIBLE
        startButton.setOnClickListener {
            showLoadingAndDisableStartButton()
            joinMeeting()
        }
    }

    private fun hideLoadingAndEnableStartButton() {
        if (!isVisible) return
        startButton.isEnabled = true
        startButton.text = "START"
        startButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.testpress_color_primary))
    }

    private fun showLoadingAndDisableStartButton(loadingText: String="LOADING") {
        startButton.isEnabled = false
        startButton.text = loadingText
        startButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.testpress_text_gray))
    }

    private fun initializeVideoConferenceHandler(videoConference: DomainVideoConferenceContent?) {
        showLoadingAndDisableStartButton()
        profileDetails?.let {
            initVideoConferenceHandler(videoConference, it)
        } ?: loadProfileAndInitializeConference(videoConference)
    }

    private fun loadProfileAndInitializeConference(videoConference: DomainVideoConferenceContent?) {
        TestpressUserDetails.getInstance().load(requireContext(),object : TestpressCallback<ProfileDetails>(){
            override fun onSuccess(result: ProfileDetails?) {
                profileDetails = result
                profileDetails?.let {
                    initVideoConferenceHandler(videoConference, it)
                }
            }

            override fun onException(exception: TestpressException) {
                emptyViewFragment.displayError(exception)
            }
        })
    }

    private fun initVideoConferenceHandler(
        videoConference: DomainVideoConferenceContent?, 
        profileDetails: ProfileDetails,
        onInitComplete: (() -> Unit)? = null
    ) {
        try {
            videoConferenceHandler = VideoConferenceHandler(requireContext(), videoConference!!, profileDetails)
            videoConferenceHandler?.init(object : VideoConferenceInitializeListener {
                override fun onSuccess() {
                    if (!isRetryingAfterFailure) {
                        hideLoadingAndEnableStartButton()
                    }
                    onInitComplete?.invoke()
                }

                override fun onFailure() {
                    if (!isRetryingAfterFailure) {
                        hideLoadingAndEnableStartButton()
                    }
                    onInitComplete?.invoke()
                }
            })
        } catch (e: NoClassDefFoundError) {
            if (!isRetryingAfterFailure) {
                hideLoadingAndEnableStartButton()
            }
            Toast.makeText(context, "Zoom integration is not enabled in the app, please contact admin", Toast.LENGTH_LONG).show()
            onInitComplete?.invoke()
        } catch (e: NullPointerException) {
            if (!isRetryingAfterFailure) {
                hideLoadingAndEnableStartButton()
            }
            Sentry.captureException(e) { scope ->
                scope.setTag("user_name", profileDetails?.username?:"")
                scope.setContexts(
                    "Video Conference Error",
                    object : HashMap<String?, Any?>() {
                        init {
                            put("Content Id", contentId)
                            put("Content Title", content.title)
                        }
                    }
                )
            }
            onInitComplete?.invoke()
        }
    }

    override fun onResume() {
        super.onResume()
        hideLoadingAndEnableStartButton()
    }

    override fun onDestroy() {
        super.onDestroy()
        videoConferenceHandler?.destroy()
    }

    private fun joinMeeting() {
        val videoConference = content.videoConference
        
        if (needsDataRefresh(videoConference)) {
            forceReloadContent {
                val refreshedConference = content.videoConference
                if (refreshedConference != null && refreshedConference.conferenceId != null && refreshedConference.password != null) {
                    videoConferenceHandler?.destroy()
                    profileDetails?.let {
                        initVideoConferenceHandler(refreshedConference, it) {
                            attemptJoin()
                        }
                    } ?: run {
                        hideLoadingAndEnableStartButton()
                        Toast.makeText(context, "Unable to join meeting. Please try again.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    hideLoadingAndEnableStartButton()
                    val message = if (videoConference?.conferenceId == null || videoConference?.password == null) {
                        "Meeting has not started yet. Please try again after the meeting starts."
                    } else {
                        "Unable to join meeting. Please try again."
                    }
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                }
            }
        } else {
            attemptJoin()
        }
    }
    
    private fun needsDataRefresh(videoConference: DomainVideoConferenceContent?): Boolean {
        if (videoConference?.conferenceId == null || videoConference.password == null) {
            return true
        }
        
        return videoConference.accessToken?.let { token ->
            try {
                JWT(token).isExpired(10)
            } catch (e: Exception) {
                true
            }
        } ?: true
    }
    
    private fun attemptJoin() {
        if (videoConferenceHandler == null) {
            hideLoadingAndEnableStartButton()
            Toast.makeText(context, "Unable to join meeting. Please try again.", Toast.LENGTH_SHORT).show()
            return
        }
        
        videoConferenceHandler?.joinMeet(object: VideoConferenceInitializeListener {
            override fun onSuccess() {
                isRetryingAfterFailure = false
                viewModel.createContentAttempt(contentId)
                hideLoadingAndEnableStartButton()
            }

            override fun onFailure() {
                if (!isRetryingAfterFailure) {
                    isRetryingAfterFailure = true
                    forceReloadContent {
                        val videoConference = content.videoConference
                        profileDetails?.let {
                            if (videoConference != null && videoConference.conferenceId != null && videoConference.password != null) {
                                videoConferenceHandler?.destroy()
                                initVideoConferenceHandler(videoConference, it) {
                                    videoConferenceHandler?.joinMeet(object: VideoConferenceInitializeListener {
                                        override fun onSuccess() {
                                            isRetryingAfterFailure = false
                                            viewModel.createContentAttempt(contentId)
                                            hideLoadingAndEnableStartButton()
                                        }

                                        override fun onFailure() {
                                            isRetryingAfterFailure = false
                                            hideLoadingAndEnableStartButton()
                                            Toast.makeText(context, "Could not join meeting. Please refresh the page and try again.", Toast.LENGTH_LONG).show()
                                        }
                                    })
                                }
                            } else {
                                isRetryingAfterFailure = false
                                hideLoadingAndEnableStartButton()
                                val message = if (videoConference?.conferenceId == null || videoConference?.password == null) {
                                    "Meeting has not started yet. Please try again after the meeting starts."
                                } else {
                                    "Could not join meeting. Please refresh the page and try again."
                                }
                                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                            }
                        } ?: run {
                            isRetryingAfterFailure = false
                            hideLoadingAndEnableStartButton()
                            Toast.makeText(context, "Unable to join meeting. Please try again.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    isRetryingAfterFailure = false
                    hideLoadingAndEnableStartButton()
                    Toast.makeText(context, "Could not join meeting. Please refresh the page and try again.", Toast.LENGTH_LONG).show()
                }
            }
        })
    }
}