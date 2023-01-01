package `in`.testpress.course.ui

import `in`.testpress.course.databinding.MeetingOptionBarBinding
import android.os.Bundle
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
                callback!!.onClickSpeaker()
            }
            binding.hand.id ->{
                callback!!.onClickHand()
            }

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