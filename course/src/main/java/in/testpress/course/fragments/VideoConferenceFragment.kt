package `in`.testpress.course.fragments

import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.R
import `in`.testpress.course.ui.WebViewActivity
import `in`.testpress.util.ViewUtils
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class VideoConferenceFragment : BaseContentDetailFragment() {
    private lateinit var titleView: TextView
    private lateinit var titleLayout: LinearLayout
    private lateinit var startButton: Button
    private lateinit var duration: TextView
    private lateinit var startTime: TextView
    private lateinit var provider: TextView

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
        provider = view.findViewById(R.id.provider)
        ViewUtils.setTypeface(
            arrayOf(titleView, provider, startTime, duration),
            TestpressSdk.getRubikMediumFont(activity!!)
        )
    }

    override fun display() {
        val videoConference = content.videoConference

        titleView.text = content.title
        titleLayout.visibility = View.VISIBLE
        videoConference?.let {
            duration.text = it.duration.toString()
            provider.text = it.provider?.toUpperCase()
            startTime.text = it.formattedStartDate()
        }
        startButton.setOnClickListener {
            openVideoConferenceInWebView()
        }
    }

    private fun openVideoConferenceInWebView() {
        val videoConference = content.videoConference
        val intent = Intent(activity, WebViewActivity::class.java)
        val session = TestpressSdk.getTestpressSession(requireContext())
        val token = session?.token
        intent.putExtra("URL", videoConference?.joinUrl)
        intent.putExtra("JWT_TOKEN", token)
        intent.putExtra("TITLE", videoConference?.title)
        activity!!.startActivity(intent)
    }
}