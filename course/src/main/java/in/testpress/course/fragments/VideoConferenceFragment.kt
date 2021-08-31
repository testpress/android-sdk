package `in`.testpress.course.fragments

import `in`.testpress.core.TestpressSdk
import `in`.testpress.core.TestpressUserDetails
import `in`.testpress.course.R
import `in`.testpress.course.domain.DomainVideoConferenceContent
import `in`.testpress.course.ui.WebViewActivity
import `in`.testpress.course.util.VideoConferenceHandler
import `in`.testpress.models.ProfileDetails
import `in`.testpress.util.ViewUtils
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import br.com.simplepass.loadingbutton.customViews.CircularProgressButton

class VideoConferenceFragment : BaseContentDetailFragment() {
    private lateinit var titleView: TextView
    private lateinit var titleLayout: LinearLayout
    private lateinit var startButton: CircularProgressButton
    private lateinit var duration: TextView
    private lateinit var startTime: TextView
    private var videoConferenceHandler: VideoConferenceHandler? = null
    private var profileDetails: ProfileDetails? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        profileDetails = TestpressUserDetails.getInstance().profileDetails
        if (profileDetails == null) {
            TestpressUserDetails.getInstance().load(requireContext())
        }
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
        ViewUtils.setTypeface(
            arrayOf(titleView, startTime, duration),
            TestpressSdk.getRubikMediumFont(activity!!)
        )
    }

    override fun display() {
        val videoConference = content.videoConference
        initializeVideoConferenceHandler(videoConference)

        titleView.text = content.title
        titleLayout.visibility = View.VISIBLE
        videoConference?.let {
            duration.text = it.duration.toString()
            startTime.text = it.formattedStartDate()
        }
        startButton.setOnClickListener {
            startButton.startAnimation()
            joinMeeting()
        }
    }

    override fun onResume() {
        super.onResume()
        startButton.revertAnimation()
        startButton.setBackgroundResource(R.drawable.testpress_curved_blue_background)
    }

    private fun initializeVideoConferenceHandler(videoConference: DomainVideoConferenceContent?) {
        try {
            videoConferenceHandler =  VideoConferenceHandler(requireContext(), videoConference!!, profileDetails)
            videoConferenceHandler?.init()
        }
        catch(e: NoClassDefFoundError){
            Toast.makeText(context, "Zoom integration is not enabled in the app, please contact admin", Toast.LENGTH_LONG).show()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        videoConferenceHandler?.destroy()
    }

    private fun joinMeeting() {
        videoConferenceHandler?.joinMeet()
    }
}