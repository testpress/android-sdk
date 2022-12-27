package `in`.testpress.course.ui

import `in`.testpress.course.R
import `in`.testpress.course.databinding.MeetingNoticeScreenBinding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import us.zoom.sdk.MeetingStatus


class NoticeScreenFragment(
    private var meetingStatus: MeetingStatus,
    private var conferenceTitle: String
) : Fragment() {
    private lateinit var noticeScreenBinding: MeetingNoticeScreenBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        noticeScreenBinding = MeetingNoticeScreenBinding.inflate(inflater, container, false)
        this.setUpToolbar()
        this.setNoticeMessage()
        return noticeScreenBinding.root
    }

    private fun setUpToolbar(){
        this.setupBackButton()
        this.setTitle()
    }

    private fun setupBackButton(){
        noticeScreenBinding.toolbar.setNavigationIcon(R.drawable.ic_back)
        noticeScreenBinding.toolbar.setNavigationOnClickListener { activity!!.onBackPressed() }
    }

    private fun setTitle(){
        noticeScreenBinding.toolbarTitle.text = conferenceTitle
    }

    private fun setNoticeMessage(){
        noticeScreenBinding.noticeMessage.text = getMessage()
    }

    private fun getMessage(): String{
        return when (meetingStatus){
            MeetingStatus.MEETING_STATUS_CONNECTING -> "Connecting to the class"
            MeetingStatus.MEETING_STATUS_IN_WAITING_ROOM -> "Please wait, the meeting host will let you in soon."
            MeetingStatus.MEETING_STATUS_WAITINGFORHOST -> "Wait for host to start this meeting."
            else -> ""
        }
    }
}
