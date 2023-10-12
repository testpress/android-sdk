package `in`.testpress.course.fragments


import `in`.testpress.course.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class RootQuizFragment: Fragment() {
    private lateinit var questionFragment: QuizQuestionFragment
    private lateinit var reviewFragment: QuizReviewFragment

    lateinit var nextQuizHandler: NextQuizHandler
    private var position: Int = 0
    private var examId: Long = -1
    private var attemptId: Long = -1
    var isQuestionFragment: Boolean = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.quiz_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        parseArguments()
        showQuestion()
    }

    private fun parseArguments() {
        examId = requireArguments().getLong("EXAM_ID", -1)
        attemptId = requireArguments().getLong("ATTEMPT_ID", -1)
        position = requireArguments().getInt("POSITION", 0)
    }

    fun submitAnswer() {
     questionFragment.submitAnswer()
    }

    private fun showQuestion() {
        isQuestionFragment = true
        questionFragment = QuizQuestionFragment()
        questionFragment.arguments = arguments

        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.root_layout, questionFragment)
        transaction.commit()
    }

    private fun showReviewFragment() {
        isQuestionFragment = false
        reviewFragment = QuizReviewFragment()
        reviewFragment.arguments = arguments
        reviewFragment.arguments?.apply {
            putInt("POSITION", position)
        }
        reviewFragment.nextQuizHandler = nextQuizHandler

        val transaction = childFragmentManager.beginTransaction()
        transaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
        transaction.replace(R.id.root_layout, reviewFragment)
        transaction.commit()
    }

    fun changeFragment() {
        showReviewFragment()
    }
}