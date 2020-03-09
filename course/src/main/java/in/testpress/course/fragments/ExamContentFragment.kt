package `in`.testpress.course.fragments

import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.R
import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.domain.DomainExamContent
import `in`.testpress.course.domain.DomainLanguage
import `in`.testpress.course.domain.asGreenDaoModel
import `in`.testpress.course.enums.Status
import `in`.testpress.course.network.NetworkContentAttempt
import `in`.testpress.course.network.Resource
import `in`.testpress.course.network.asGreenDaoModel
import `in`.testpress.exam.TestpressExam
import `in`.testpress.exam.api.TestpressExamApiClient.STATE_PAUSED
import `in`.testpress.exam.util.MultiLanguagesUtil
import `in`.testpress.exam.util.RetakeExamUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer

class ExamContentFragment: BaseContentDetailFragment() {
    private lateinit var titleLayout: LinearLayout
    private lateinit var titleView: TextView
    private lateinit var contentAttempts: ArrayList<NetworkContentAttempt>

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

    override fun onResume() {
        super.onResume()
        if (contentId != -1L) {
            display()
        }
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