package `in`.testpress.course.ui

import `in`.testpress.course.R
import `in`.testpress.course.databinding.MeetingOptionBarBinding
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton


class MeetingOptionBarFragment : Fragment(), View.OnClickListener {
    private lateinit var binding: MeetingOptionBarBinding
    private var callback: MeetingOptionBarCallback? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = MeetingOptionBarBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.hand.setOnClickListener(this)
        binding.showSidebar.setOnClickListener(this)
        binding.speaker.setOnClickListener(this)
    }

    fun setCallback(callback: MeetingOptionBarCallback){
        this.callback = callback
    }

    override fun onClick(view: View?) {
        if (this.callback == null){
            return
        }

       val _view = view as ImageButton
        when (_view.id){
            binding.showSidebar.id ->{
                callback!!.onClickChats()
            }
            binding.speaker.id ->{
                Log.d("TAG", "onClick: ")
                callback!!.onClickSpeaker()
            }
            binding.hand.id ->{
                Log.d("TAG", "onClick: ")
                callback!!.onClickHand()
            }

        }
    }

    fun refreshHandIcon(active: Boolean){
        if (active){
            binding.speaker.setBackgroundColor(Color.parseColor("ffc940"))
        }
        else{
            binding.speaker.setBackgroundColor(Color.parseColor("aaa"))
        }
    }

    fun refreshspeakerIcon(active: Boolean){
        if (active){
            binding.speaker.setBackgroundColor(Color.parseColor("aaa"))
        }
        else{
            binding.speaker.background.invalidateSelf()
        }
    }

    fun refreshChatIcon(active: Boolean){
        if (active){
            binding.speaker.background.invalidateSelf()
        }
        else{
            binding.speaker.setBackgroundColor(Color.parseColor("aaa"))
        }
    }

    companion object {
        interface MeetingOptionBarCallback{
            fun onClickChats()
            fun onClickSpeaker()
            fun onClickHand()
        }
    }
}