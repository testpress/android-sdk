package `in`.testpress.course.fragments

import `in`.testpress.course.R
import `in`.testpress.course.domain.DomainVideoContent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment

class VideoContentFragment : BaseContentDetailFragment() {
    private lateinit var titleView: TextView
    private lateinit var titleLayout: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.video_content_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        titleView = view.findViewById(R.id.title)
        titleLayout = view.findViewById(R.id.title_layout)
    }

    override fun display() {
        titleView.text = content.title
        val videoWidgetFragment = VideoWidgetFragmentFactory.getWidget(video)
        videoWidgetFragment.arguments = arguments
        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.video_widget_fragment, videoWidgetFragment)
        transaction.commit()
    }
}

class VideoWidgetFragmentFactory {
    companion object {
        fun getWidget(video: DomainVideoContent): Fragment {
            return when {
                video.isDomainRestricted!! -> DomainRestrictedVideoFragment()
                video.embedCode.isNullOrEmpty() -> NativeVideoWidgetFragment()
                else -> WebViewVideoFragment()
            }
        }
    }
}