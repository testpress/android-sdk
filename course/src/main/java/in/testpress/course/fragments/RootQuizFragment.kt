package `in`.testpress.course.fragments


import `in`.testpress.course.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class RootQuizFragment: Fragment(), QuizFragmentHandler {
    lateinit var nextQuizHandler: NextQuizHandler
    private var position: Int = 0
    private var examId: Long = -1

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
        position = requireArguments().getInt("POSITION", 0)
    }

    private fun showQuestion() {
        val questionFragment = QuestionFragment()
        questionFragment.arguments = arguments
        questionFragment.quizFragmentHandler = this

        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.root_layout, questionFragment)
        transaction.commit()
    }

    private fun showReviewFragment() {
        val reviewFragment = QuizReviewFragment()
        reviewFragment.arguments = arguments
        reviewFragment.arguments?.apply {
            putInt("POSITION", position)
        }
        reviewFragment.nextQuizHandler = nextQuizHandler

        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.root_layout, reviewFragment)
        transaction.commit()
    }

    override fun changeFragment() {
        showReviewFragment()
    }
}