package `in`.testpress.course.fragments

import `in`.testpress.course.R
import `in`.testpress.course.domain.DomainContentAttempt
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class QuizAttemptsList : AttemptsListFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.quiz_attempts_list_view, container, false)
    }

    override fun updateStartButton(contentAttempts: ArrayList<DomainContentAttempt>) {
        startButton.visibility = View.GONE
    }

}