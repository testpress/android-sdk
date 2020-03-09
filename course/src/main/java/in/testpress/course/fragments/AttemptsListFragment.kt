package `in`.testpress.course.fragments

import `in`.testpress.course.R
import `in`.testpress.course.ui.ContentActivity
import `in`.testpress.course.ui.ContentAttemptListAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AttemptsListFragment : BaseExamWidgetFragment() {
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
        display()
    }

    fun display() {
        val contentId = requireArguments().getLong(ContentActivity.CONTENT_ID)
        val greenDaoContent = viewModel.getContentFromDB(contentId)
        val attempts = viewModel.getContentAttemptsFromDB(contentId)
        attemptList.isNestedScrollingEnabled = false
        attemptList.setHasFixedSize(true)
        attemptList.layoutManager = LinearLayoutManager(activity)
        attemptList.adapter = ContentAttemptListAdapter(activity, greenDaoContent, attempts)
        attemptList.visibility = View.VISIBLE
    }
}