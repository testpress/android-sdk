package `in`.testpress.course.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import `in`.testpress.course.R
import android.util.Log


class LiveStreamFragment : BaseContentDetailFragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_live_stream, container, false)
    }

    override fun display() {
        Log.d("TAG", "display: ")
    }
}