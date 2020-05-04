package `in`.testpress.course.fragments

import `in`.testpress.course.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2


class QuizSlideFragment: Fragment(), NextQuizHandler {
    private lateinit var viewPager: ViewPager2
    var examId: Long = -1
    var attemptId: Long = -1
    var totalNoOfQuestions: Int = 1
    lateinit var endHanlder: ExamEndHanlder

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.quiz_slide_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        parseArguments()
        initializeViewPager(view)
    }

    private fun initializeViewPager(view: View) {
        viewPager = view.findViewById(R.id.pager)
        viewPager.adapter = QuizSlideAdapter(this)
        viewPager.isUserInputEnabled = false
    }

    private fun parseArguments() {
        examId = requireArguments().getLong("EXAM_ID", -1)
        attemptId = requireArguments().getLong("ATTEMPT_ID", -1)
        totalNoOfQuestions = requireArguments().getInt("NO_OF_QUESTIONS", 1)
    }

    override fun showNext() {
        if (totalNoOfQuestions == viewPager.currentItem + 1) {
            endHanlder.endExam()
            return
        }
        viewPager.setCurrentItem(viewPager.currentItem + 1, true)
    }

    class QuizSlideAdapter(val fragment: QuizSlideFragment) : FragmentStateAdapter(fragment) {

        override fun getItemCount(): Int = fragment.totalNoOfQuestions

        override fun createFragment(position: Int): Fragment {
            val questionFragment = RootQuizFragment()
            questionFragment.nextQuizHandler = fragment as NextQuizHandler
            val bundle = Bundle().apply {
                putInt("POSITION", position)
                putLong("EXAM_ID", fragment.examId)
                putLong("ATTEMPT_ID", fragment.attemptId)
            }
            questionFragment.arguments = bundle
            return questionFragment
        }
    }
}

interface ExamEndHanlder {
    fun endExam()
}