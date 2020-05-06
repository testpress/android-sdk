package `in`.testpress.course.fragments

import `in`.testpress.course.R
import `in`.testpress.course.domain.DomainContent
import `in`.testpress.util.InternetConnectivityChecker
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment

class QuizContentFragment: BaseContentDetailFragment(), ExamRefreshListener {
    private lateinit var titleLayout: LinearLayout
    private lateinit var titleView: TextView

    override var isBookmarkEnabled: Boolean
        get() = false
        set(value) {}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.quiz_content_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        titleView = view.findViewById(R.id.title)
        titleLayout = view.findViewById(R.id.title_layout)
    }

    override fun display() {
        titleView.text = content.title
        titleLayout.visibility = View.VISIBLE
        initQuizWidget(content)
    }

    private fun initQuizWidget(content: DomainContent) {
        val startQuizFragment = StartQuizFragment()
        startQuizFragment.arguments = arguments
        val fragmentTransaction = childFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.start_exam_fragment, startQuizFragment)
        fragmentTransaction.commit()

        val quizWidgetFragment = QuizWidgetFactory.getWidget(content, requireContext())
        quizWidgetFragment?.let {
            it.arguments = arguments
            val transaction = childFragmentManager.beginTransaction()
            transaction.replace(R.id.exam_widget_fragment, it)
            transaction.commit()
        }
    }

    override fun showOrHideRefresh(isRefreshing: Boolean) {
        // swipeRefresh.isRefreshing = isRefreshing
    }
}

class QuizWidgetFactory {
    companion object {
        fun getWidget(content: DomainContent, context: Context): Fragment? {
            if (content.attemptsCount!! > 0 && InternetConnectivityChecker.isConnected(context)){
                return QuizAttemptsList()
            }
            return null
        }
    }
}