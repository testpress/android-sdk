package `in`.testpress.course.ui;

import `in`.testpress.course.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import us.zoom.sdk.InMeetingChatMessage


class MessageListAdapter(private val currentUserId: Long) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val VIEW_TYPE_MESSAGE_SENT = 1
    private val VIEW_TYPE_MESSAGE_RECEIVED = 2
    private var messages = mutableListOf<InMeetingChatMessage>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            SentMessageHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.my_message, parent, false))
        } else {
            ReceivedMessageHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.received_message, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        val previousMessage = messages.getOrNull(position - 1)

        when (holder.itemViewType) {
            VIEW_TYPE_MESSAGE_SENT -> (holder as SentMessageHolder).bind(message)
            VIEW_TYPE_MESSAGE_RECEIVED -> (holder as ReceivedMessageHolder).bind(message, previousMessage)
        }
    }

    override fun getItemCount() = messages.count()

    override fun getItemId(position: Int) = position.toLong()

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderUserId == currentUserId) {
            VIEW_TYPE_MESSAGE_SENT
        } else {
            VIEW_TYPE_MESSAGE_RECEIVED
        }
    }

    fun addMessage(message: InMeetingChatMessage?){
        if (message != null) {
            this.messages.add(message)
            this.notifyItemInserted(messages.lastIndex)
        }
    }


    private class ReceivedMessageHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var messageText: TextView
        var nameText: TextView

        init {
            nameText = itemView.findViewById(R.id.sender)
            messageText = itemView.findViewById(R.id.message)
        }

        fun bind(message: InMeetingChatMessage, previousMessage: InMeetingChatMessage?) {
            messageText.text = message.content
            nameText.text = message.senderDisplayName
            if(previousMessage?.senderUserId == message.senderUserId){
                nameText.visibility = View.GONE
            }else{
                nameText.visibility = View.VISIBLE
            }
        }
    }

    private class SentMessageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var messageText: TextView

        init {
            messageText = itemView.findViewById(R.id.my_chat_message)
        }

        fun bind(message: InMeetingChatMessage) {
            messageText.text = message.content
        }
    }
}
