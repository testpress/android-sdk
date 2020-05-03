package `in`.testpress.course.fragments

import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.R
import `in`.testpress.course.domain.DomainExamContent
import `in`.testpress.course.ui.ContentActivity.CONTENT_ID
import `in`.testpress.course.ui.QuizActivity
import `in`.testpress.util.ViewUtils
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class QuizDetailFragment: BaseContentDetailFragment() {
    private lateinit var numberOfQuestions: TextView
    private lateinit var date: TextView
    private lateinit var description: LinearLayout
    private lateinit var dateLayout: LinearLayout
    private lateinit var marksPerQuestionLayout: LinearLayout
    private lateinit var negativeMarksLayout: LinearLayout
    private lateinit var durationLayout: LinearLayout
    private lateinit var descriptionContent: TextView
    private lateinit var questionsLabel: TextView
    private lateinit var dateLabel: TextView
    private lateinit var startButton: Button
    private lateinit var titleLayout: LinearLayout
    private lateinit var titleView: TextView

    private var questionsUrl = "api/v2.4/exams/117/questions/"

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
        bindViews(view)
    }

    fun bindViews(view: View) {
        titleView = view.findViewById(R.id.title)
        titleLayout = view.findViewById(R.id.title_layout)
        numberOfQuestions = view.findViewById(R.id.number_of_questions)
        date = view.findViewById(R.id.date)
        description = view.findViewById(R.id.description)
        dateLayout = view.findViewById(R.id.date_layout)
        durationLayout = view.findViewById(R.id.duration_layout)
        marksPerQuestionLayout = view.findViewById(R.id.mark_per_question_layout)
        negativeMarksLayout = view.findViewById(R.id.negative_marks_layout)
        descriptionContent = view.findViewById(R.id.descriptionContent)
        questionsLabel = view.findViewById(R.id.questions_label)
        dateLabel = view.findViewById(R.id.date_label)
        startButton = view.findViewById(R.id.start_exam)
        startButton.visibility = View.VISIBLE
        marksPerQuestionLayout.visibility = View.GONE
        negativeMarksLayout.visibility = View.GONE
        durationLayout.visibility = View.GONE

        ViewUtils.setTypeface(
            arrayOf(
                numberOfQuestions,
                date
            ), TestpressSdk.getRubikMediumFont(requireActivity())
        )
        ViewUtils.setTypeface(
            arrayOf(
                descriptionContent,
                questionsLabel,
                dateLabel
            ),
            TestpressSdk.getRubikRegularFont(requireActivity())
        )
    }

    override fun display() {
        bindData()
        initializeListeners()
    }

    private fun bindData() {
        val exam = content.exam!!
        titleView.text = content.title
        titleLayout.visibility = View.VISIBLE
        numberOfQuestions.text = exam.numberOfQuestions.toString()
        descriptionContent.text = exam.description
        showOrHideExamDate(exam)
    }

    private fun initializeListeners() {
        val exam = content.exam!!

        startButton.setOnClickListener {
            val intent = Intent(requireContext(), QuizActivity::class.java).apply {
                putExtra(CONTENT_ID, content.id)
                putExtra("EXAM_ID", exam.id)
                putExtra("QUESTIONS_URL", questionsUrl)
                putExtra("ATTEMPT_URL", exam.attemptsUrl)
            }
            requireActivity().startActivity(intent)
        }
    }

    private fun showOrHideExamDate(exam: DomainExamContent) {
        if (exam.formattedStartDate() == "forever") {
            dateLayout.visibility = View.GONE
        } else {
            date.text = "${exam.formattedStartDate()} -\n ${exam.formattedEndData()}"
            dateLayout.visibility = View.VISIBLE
        }
    }
}