package `in`.testpress.course.ui;

import `in`.testpress.course.R
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import us.zoom.sdk.InMeetingChatMessage


class MessageListAdapter(private val currentUserId: Long) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var messages = mutableListOf<InMeetingChatMessage>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MessageHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.message, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        val previousMessage = messages.getOrNull(position - 1)

        (holder as MessageHolder).bind(message, previousMessage, isCurrentUserMessage(message))
    }

    private fun isCurrentUserMessage(message: InMeetingChatMessage) = run { currentUserId == message.senderUserId }

    override fun getItemCount() = messages.count()
    override fun getItemId(position: Int) = position.toLong()

    fun addMessage(message: InMeetingChatMessage?){
        if (message != null) {
            this.messages.add(message)
            this.notifyItemInserted(messages.lastIndex)
        }
    }


    private class MessageHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var messageCard: CardView
        var messageText: TextView
        var nameText: TextView

        init {
            nameText = itemView.findViewById(R.id.sender)
            messageText = itemView.findViewById(R.id.message)
            messageCard = itemView.findViewById(R.id.message_card)
        }

        fun bind(message: InMeetingChatMessage, previousMessage: InMeetingChatMessage?, isCurrentUserMessage: Boolean) {
            messageText.text = message.content
            nameText.text = if (isCurrentUserMessage) "Me" else message.senderDisplayName
            nameText.visibility = if (previousMessage?.senderUserId == message.senderUserId) View.GONE else View.VISIBLE
            setColorOnMessageCard(isCurrentUserMessage)
        }

        fun setColorOnMessageCard(isCurrentUserMessage: Boolean){
            val color = if (isCurrentUserMessage){
                Color.parseColor("#dff0ff")
            }else{
                Color.parseColor("#eef1f6")
            }
            messageCard.setCardBackgroundColor(color)
        }
    }
}
