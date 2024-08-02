package `in`.testpress.course.fragments

import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.R
import `in`.testpress.course.domain.getGreenDaoContent
import `in`.testpress.course.domain.getGreenDaoContentAttempts
import `in`.testpress.course.ui.ContentActivity
import `in`.testpress.course.ui.ContentAttemptListAdapter
import `in`.testpress.util.ViewUtils
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

open class AttemptsListFragment : BaseExamWidgetFragment() {
    private lateinit var attemptList: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.attempts_list_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        attemptList = view.findViewById(R.id.attempt_list)
        ViewUtils.setTypeface(
            arrayOf(attemptSyncText),
            TestpressSdk.getRubikRegularFont(requireActivity())
        )
    }

    override fun display() {
        initializeObserversForOfflineDownload()
        val greenDaoContent = content.getGreenDaoContent(requireContext())
        val attempts = content.getGreenDaoContentAttempts(requireContext())
        attemptList.isNestedScrollingEnabled = false
        attemptList.setHasFixedSize(true)
        attemptList.layoutManager = LinearLayoutManager(activity)
        attemptList.adapter = ContentAttemptListAdapter(activity, greenDaoContent, attempts.reversed())
        attemptList.visibility = View.VISIBLE
    }
}