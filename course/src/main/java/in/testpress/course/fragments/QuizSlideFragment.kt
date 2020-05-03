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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.quiz_slide_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewPager = view.findViewById(R.id.pager)
        viewPager.adapter = QuizSlideAdapter(this)
        viewPager.isUserInputEnabled = false
        examId = requireArguments().getLong("EXAM_ID", -1)
    }

    class QuizSlideAdapter(val fragment: QuizSlideFragment) : FragmentStateAdapter(fragment) {

        override fun getItemCount(): Int = 10

        override fun createFragment(position: Int): Fragment {
            val questionFragment = RootQuizFragment()
            questionFragment.nextQuizHandler = fragment as NextQuizHandler
            val bundle = Bundle().apply {
                putInt("POSITION", position)
                putLong("EXAM_ID", fragment.examId)
            }
            questionFragment.arguments = bundle
            return questionFragment
        }
    }

    override fun showNext() {
        viewPager.setCurrentItem(viewPager.currentItem + 1, true)
    }
}