package `in`.testpress.course.ui;

import `in`.testpress.course.databinding.FragmentChatBinding
import `in`.testpress.course.domain.zoom.callbacks.MeetingChatCallback
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import us.zoom.sdk.*


class ChatFragment : Fragment(), MeetingChatCallback.ChatEvent{
    private var currentUserId: Long = 0
    private lateinit var inMeetingChatController: InMeetingChatController
    private lateinit var messageAdapter: MessageListAdapter
    private lateinit var fragmentChatBinding: FragmentChatBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inMeetingChatController = ZoomSDK.getInstance().inMeetingService.inMeetingChatController
        currentUserId = ZoomSDK.getInstance().inMeetingService.myUserID
        MeetingChatCallback.addListener(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        fragmentChatBinding = FragmentChatBinding.inflate(inflater, container, false)
        return fragmentChatBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpSend()
        setUpRecycleView()
    }

    private fun setUpSend(){
        val inputBox = fragmentChatBinding.inputBox
        val recyclerView = fragmentChatBinding.recyclerChat

        fragmentChatBinding.inputBar.setEndIconOnClickListener {
            if(inputBox.text?.isNotBlank() == true){
                inMeetingChatController.sendChatToGroup(InMeetingChatController.MobileRTCChatGroup.MobileRTCChatGroup_All,
                    inputBox.text.toString()
                )
                inputBox.text!!.clear()
                recyclerView.post { recyclerView.smoothScrollToPosition(messageAdapter.itemCount - 1) }
            }
        }
    }

    private fun setUpRecycleView(){
        messageAdapter = MessageListAdapter(currentUserId)
        fragmentChatBinding.recyclerChat.layoutManager = LinearLayoutManager(activity)
        fragmentChatBinding.recyclerChat.adapter = messageAdapter
    }

    override fun onChatMessageReceived(message: InMeetingChatMessage) {
        messageAdapter.addMessage(message)
        if (! fragmentChatBinding.recyclerChat.canScrollVertically(1)) {
            fragmentChatBinding.recyclerChat.smoothScrollToPosition(messageAdapter.itemCount - 1)
        }
    }

    override fun onChatMsgDeleteNotification(msgID: String, deleteBy: ChatMessageDeleteType) {}
}