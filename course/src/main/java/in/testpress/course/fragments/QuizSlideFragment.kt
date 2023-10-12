package `in`.testpress.course.fragments

import `in`.testpress.course.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton

class QuizSlideFragment: Fragment(), NextQuizHandler, QuizSkipHandler {
    private lateinit var submitButton: MaterialButton

    private lateinit var viewPager: ViewPager2
    var examId: Long = -1
    var attemptId: Long = -1
    var startIndex: Int = 0
    var totalNoOfQuestions: Int = 1
    lateinit var endHanlder: ExamEndHanlder
    var questionNumberHandler: QuestionNumberHandler? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.quiz_slide_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        parseArguments()
        initializeViewPager(view)
        submitButton = view.findViewById(R.id.submit_button)
        submitButton.visibility = View.VISIBLE

        submitButton.setOnClickListener {
            updateSolution()
        }
    }

    private fun updateSolution() {
        val fragment =
            childFragmentManager.findFragmentByTag("f" + viewPager.currentItem) as RootQuizFragment
        if (fragment.isQuestionFragment) {
            fragment.submitAnswer()
            fragment.changeFragment()
            submitButton.text = "Continue"
        } else {
            showNext()
            submitButton.text = "Check"
        }
    }

    private fun initializeViewPager(view: View) {
        viewPager = view.findViewById(R.id.pager)
        viewPager.adapter = QuizSlideAdapter(this)
        viewPager.isUserInputEnabled = false
        viewPager.post { viewPager.setCurrentItem(startIndex, true) }
        questionNumberHandler?.changeQuestionNumber(startIndex + 1, totalNoOfQuestions)
    }

    private fun parseArguments() {
        examId = requireArguments().getLong("EXAM_ID", -1)
        attemptId = requireArguments().getLong("ATTEMPT_ID", -1)
        startIndex = requireArguments().getInt("START_INDEX", 0)
        totalNoOfQuestions = requireArguments().getInt("NO_OF_QUESTIONS", 1)
    }

    override fun showNext() {
        if (totalNoOfQuestions == viewPager.currentItem + 1) {
            endHanlder.endExam()
            return
        }
        questionNumberHandler?.changeQuestionNumber(viewPager.currentItem + 2, totalNoOfQuestions)
        viewPager.setCurrentItem(viewPager.currentItem + 1, true)
    }

    class QuizSlideAdapter(val fragment: QuizSlideFragment) : FragmentStateAdapter(fragment) {

        override fun getItemCount(): Int = fragment.totalNoOfQuestions

        override fun createFragment(position: Int): Fragment {
            val questionFragment = RootQuizFragment()
            questionFragment.nextQuizHandler = fragment as NextQuizHandler
            questionFragment.quizSkipHandler = fragment
            val bundle = Bundle().apply {
                putInt("POSITION", position)
                putLong("EXAM_ID", fragment.examId)
                putLong("ATTEMPT_ID", fragment.attemptId)
            }
            questionFragment.arguments = bundle
            return questionFragment
        }
    }

    override fun onSkip() {
        updateSolution()
    }
}

interface ExamEndHanlder {
    fun endExam()
}

interface QuestionNumberHandler {
    fun changeQuestionNumber(number: Int, total: Int)
}