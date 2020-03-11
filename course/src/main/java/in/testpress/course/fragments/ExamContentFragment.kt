package `in`.testpress.course.fragments

import `in`.testpress.course.R
import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.network.NetworkContentAttempt
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment

class ExamContentFragment: BaseContentDetailFragment() {
    private lateinit var titleLayout: LinearLayout
    private lateinit var titleView: TextView
    private lateinit var contentAttempts: ArrayList<NetworkContentAttempt>
    override var isBookmarkEnabled: Boolean
        get() = false
        set(value) {}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.exam_content_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        titleView = view.findViewById(R.id.title)
        titleLayout = view.findViewById(R.id.title_layout)
    }

    override fun display() {
        titleView.text = content.title
        titleLayout.visibility = View.VISIBLE
        initExamWidget(content)
    }

    private fun initExamWidget(content: DomainContent) {
        val examWidgetFragment = ExamWidgetFactory.getWidget(content)
        examWidgetFragment.arguments = arguments
        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.exam_widget_fragment, examWidgetFragment)
        transaction.commit()
    }
}

class ExamWidgetFactory {
    companion object {
        fun getWidget(content: DomainContent): Fragment {
            if (content.attemptsCount!! > 0){
                return AttemptsListFragment()
            }
            return ExamStartScreenFragment()
        }
    }
}