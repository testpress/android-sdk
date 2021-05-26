package `in`.testpress.course.fragments

import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.R
import `in`.testpress.course.domain.DomainExamContent
import `in`.testpress.course.domain.getGreenDaoContentAttempt
import `in`.testpress.course.ui.ContentActivity
import `in`.testpress.exam.api.TestpressExamApiClient.STATE_PAUSED
import `in`.testpress.util.ViewUtils
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView

class ExamStartScreenFragment : BaseExamWidgetFragment() {
    private lateinit var numberOfQuestions: TextView
    private lateinit var examDuration: TextView
    private lateinit var markPerQuestion: TextView
    private lateinit var negativeMarks: TextView
    private lateinit var date: TextView
    private lateinit var description: LinearLayout
    private lateinit var dateLayout: LinearLayout
    private lateinit var descriptionContent: TextView
    private lateinit var questionsLabel: TextView
    private lateinit var durationLabel: TextView
    private lateinit var markLabel: TextView
    private lateinit var negativeMarkLabel: TextView
    private lateinit var dateLabel: TextView
    private lateinit var languageLabel: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.exam_start_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contentId = requireArguments().getLong(ContentActivity.CONTENT_ID)
        bindViews(view)
    }

    private fun bindViews(view: View) {
        numberOfQuestions = view.findViewById(R.id.number_of_questions)
        examDuration = view.findViewById(R.id.exam_duration)
        markPerQuestion = view.findViewById(R.id.mark_per_question)
        negativeMarks = view.findViewById(R.id.negative_marks)
        date = view.findViewById(R.id.date)
        description = view.findViewById(R.id.description)
        dateLayout = view.findViewById(R.id.date_layout)
        descriptionContent = view.findViewById(R.id.descriptionContent)
        questionsLabel = view.findViewById(R.id.questions_label)
        durationLabel = view.findViewById(R.id.duration_label)
        markLabel = view.findViewById(R.id.mark_per_question_label)
        negativeMarkLabel = view.findViewById(R.id.negative_marks_label)
        dateLabel = view.findViewById(R.id.date_label)
        languageLabel = view.findViewById(R.id.language_label)

        ViewUtils.setTypeface(
            arrayOf(
                numberOfQuestions,
                examDuration,
                markPerQuestion,
                negativeMarks,
                date
            ), TestpressSdk.getRubikMediumFont(requireActivity())
        )
        ViewUtils.setTypeface(
            arrayOf(
                descriptionContent,
                questionsLabel,
                languageLabel,
                durationLabel,
                markLabel,
                negativeMarkLabel,
                dateLabel
            ),
            TestpressSdk.getRubikRegularFont(requireActivity())
        )
    }

    override fun display() {
        val exam = content.exam!!
        markPerQuestion.text = exam.markPerQuestion
        negativeMarks.text = exam.negativeMarks
        numberOfQuestions.text = exam.numberOfQuestions.toString()
        showDescription()
        showOrHideExamDate(exam)
        showExamDuration(exam)
    }

    private fun showDescription() {
        if (!content.description.isNullOrEmpty()) {
            description.visibility = View.VISIBLE
            descriptionContent.text = Html.fromHtml(content.description)
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

    private fun showExamDuration(exam: DomainExamContent) {
        examDuration.text = exam.duration
        if (contentAttempts.size == 1) {
            val greendaoContentAttempt = contentAttempts[0].getGreenDaoContentAttempt(requireContext())
            if (greendaoContentAttempt?.assessment?.state == STATE_PAUSED) {
                examDuration.text = exam.duration
                examDuration.text = greendaoContentAttempt.assessment.remainingTime
            }
        }
    }
}