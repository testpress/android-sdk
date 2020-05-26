package `in`.testpress.course.fragments

import `in`.testpress.course.R
import `in`.testpress.course.domain.DomainVideoContent
import `in`.testpress.course.util.PatternEditableBuilder
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import java.util.regex.Pattern

class VideoContentFragment : BaseContentDetailFragment() {
    private lateinit var titleView: TextView
    private lateinit var description: TextView
    private lateinit var titleLayout: LinearLayout
    private lateinit var descriptionLayout: LinearLayout
    private lateinit var videoWidgetFragment: BaseVideoWidgetFragment

    override var isBookmarkEnabled: Boolean
        get() = false
        set(value) {}

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
        description = view.findViewById(R.id.description)
        titleLayout = view.findViewById(R.id.title_layout)
        descriptionLayout = view.findViewById(R.id.description_layout)
        initializeListeners()
    }

    private fun initializeListeners() {
        titleLayout.setOnClickListener {
            val isDescriptionVisible = description.visibility == View.VISIBLE
            toggleDescription(!isDescriptionVisible)
        }
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode)
        if(isInPictureInPictureMode) {
            showOrhideBottomNav(false)
            titleLayout.visibility = View.GONE
            descriptionLayout.visibility = View.GONE
        } else {
            showOrhideBottomNav(true)
            titleLayout.visibility = View.VISIBLE
            descriptionLayout.visibility = View.VISIBLE
        }
    }

    override fun onStop() {
        super.onStop()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity?.finishAndRemoveTask()
        }
    }

    private fun toggleDescription(show: Boolean) {
        if (show) {
            description.visibility = View.VISIBLE
            titleView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_up_chevron, 0)
            content.description?.let { swipeRefresh.isEnabled = false }
        } else {
            description.visibility = View.INVISIBLE
            swipeRefresh.isEnabled = true
            titleView.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.drawable.ic_down_chevron,
                0
            )
        }
    }

    override fun display() {
        titleView.text = content.title
        videoWidgetFragment = VideoWidgetFragmentFactory.getWidget(content.video!!)
        videoWidgetFragment.arguments = arguments
        parseVideoDescription()
        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.video_widget_fragment, videoWidgetFragment)
        transaction.commit()
    }

    private fun parseVideoDescription() {
        content.description?.let {
            description.text = HtmlCompat.fromHtml(it, HtmlCompat.FROM_HTML_MODE_LEGACY)
            val pattern: Pattern = Pattern.compile("\\d\\d:\\d\\d:\\d\\d")
            PatternEditableBuilder().addPattern(
                pattern,
                Color.parseColor("#2D9BE8"),
                object : PatternEditableBuilder.SpannableClickedListener {
                    override fun onSpanClicked(text: String) {
                        val dateFormat: DateFormat = SimpleDateFormat("HH:mm:ss")
                        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
                        try {
                            val date: Date = dateFormat.parse(text)
                            videoWidgetFragment.seekTo(date.time)
                        } catch (ignore: ParseException) {
                        }
                    }
                }).into(description)
            toggleDescription(true)
        }
    }
}

class VideoWidgetFragmentFactory {
    companion object {
        fun getWidget(video: DomainVideoContent): BaseVideoWidgetFragment {
            return when {
                video.isDomainRestricted!! -> DomainRestrictedVideoFragment()
                video.hasEmbedCode() -> WebViewVideoFragment()
                else -> NativeVideoWidgetFragment()
            }
        }
    }
}