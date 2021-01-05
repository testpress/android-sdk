package `in`.testpress.course.fragments

import `in`.testpress.course.R
import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.network.NetworkContentAttempt
import `in`.testpress.course.ui.ContentActivity.FORCE_REFRESH
import `in`.testpress.course.ui.ContentActivity.TESTPRESS_CONTENT_SHARED_PREFS
import `in`.testpress.exam.ui.CarouselFragment.TEST_TAKEN_REQUEST_CODE
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment

class ExamContentFragment: BaseContentDetailFragment(), ExamRefreshListener {
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == TEST_TAKEN_REQUEST_CODE && resultCode == RESULT_OK) {
            if (requireActivity().callingActivity == null) {
                val prefs: SharedPreferences = requireActivity().getSharedPreferences(
                    TESTPRESS_CONTENT_SHARED_PREFS,
                    Context.MODE_PRIVATE
                )
                prefs.edit().putBoolean(FORCE_REFRESH, true).apply()
            }
            forceReloadContent()
            refreshContentsAndBottomNavigation()
        }
    }

    override fun showOrHideRefresh(isRefreshing: Boolean) {
        swipeRefresh.isRefreshing = isRefreshing
    }
}

interface ExamRefreshListener {
    fun showOrHideRefresh(isRefreshing: Boolean)
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