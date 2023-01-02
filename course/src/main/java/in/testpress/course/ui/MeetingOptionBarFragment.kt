package `in`.testpress.course.ui

import `in`.testpress.course.R
import `in`.testpress.course.databinding.MeetingOptionBarBinding
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment


class MeetingOptionBarFragment : Fragment(), View.OnClickListener {
    private lateinit var binding: MeetingOptionBarBinding
    private var callback: MeetingOptionBarCallback? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = MeetingOptionBarBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.hand.setOnClickListener(this)
        binding.showSidebar.setOnClickListener(this)
        binding.speaker.setOnClickListener(this)
        binding.exit.setOnClickListener(this)
    }

    fun setCallback(callback: MeetingOptionBarCallback){
        this.callback = callback
    }

    override fun onClick(view: View?) {
        if (this.callback == null){
            return
        }

       val _view = view as ImageButton
        when (_view.id) {
            binding.showSidebar.id -> {
                callback!!.onClickChats()
            }
            binding.speaker.id -> {
                callback!!.onClickSpeaker()
            }
            binding.hand.id -> {
                callback!!.onClickHand()
            }
            binding.exit.id -> {
                activity?.onBackPressed()
            }
        }
    }

    fun changeHandIconColor(active: Boolean){
        if (active) {
            binding.hand.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(context!!, R.color.testpress_color_primary)
            )
        }
        else {
            binding.hand.backgroundTintList = ColorStateList.valueOf(
                Color.parseColor("#aaaaaa")
            )
        }
    }

    fun changeSpeakerIconColor(active: Boolean) {
        if (active) {
            binding.speaker.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(context!!, R.color.testpress_color_primary)
            )
        } else {
            binding.speaker.backgroundTintList = ColorStateList.valueOf(
                Color.parseColor("#aaaaaa")
            )
        }
    }

    fun changeChatIconColor(active: Boolean){
        if (active) {
            binding.showSidebar.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(context!!, R.color.testpress_color_primary)
            )
        }
        else {
            binding.showSidebar.backgroundTintList = ColorStateList.valueOf(
                Color.parseColor("#aaaaaa")
            )
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