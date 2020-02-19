package `in`.testpress.course.ui.fragments.content_fragments

import `in`.testpress.core.TestpressSDKDatabase
import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.R
import `in`.testpress.course.enums.Status
import `in`.testpress.course.ui.ContentAttemptListAdapter
import `in`.testpress.exam.TestpressExam
import `in`.testpress.exam.network.TestpressExamApiClient.STATE_PAUSED
import `in`.testpress.exam.util.MultiLanguagesUtil
import `in`.testpress.exam.util.RetakeExamUtil
import `in`.testpress.models.greendao.*
import `in`.testpress.util.ViewUtils
import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView


class ExamContentFragment : BaseContentDetailFragment() {
    private lateinit var examContentLayout: LinearLayout
    private lateinit var examDetailsLayout: LinearLayout
    private lateinit var titleLayout: LinearLayout
    private lateinit var attemptList: RecyclerView
    private lateinit var titleView: TextView
    private lateinit var startButton: Button
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

    private lateinit var examDao: ExamDao
    private lateinit var attemptDao: AttemptDao
    private lateinit var courseAttemptDao: CourseAttemptDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        examDao = TestpressSDKDatabase.getExamDao(getActivity());
        courseAttemptDao = TestpressSDKDatabase.getCourseAttemptDao(getActivity());
        attemptDao = TestpressSDKDatabase.getAttemptDao(getActivity());
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.exam_content_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
    }

    private fun bindViews(view: View) {
        examContentLayout = view.findViewById(R.id.exam_content_layout)
        examDetailsLayout = view.findViewById(R.id.exam_details_layout)
        attemptList = view.findViewById(R.id.attempt_list)
        titleView = view.findViewById(R.id.title)
        titleLayout = view.findViewById(R.id.title_layout)
        startButton = view.findViewById(R.id.start_exam)
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

        ViewUtils.setTypeface(arrayOf(numberOfQuestions, examDuration, markPerQuestion, negativeMarks, date), TestpressSdk.getRubikMediumFont(activity!!))
        ViewUtils.setTypeface(
                arrayOf(descriptionContent, questionsLabel, languageLabel, durationLabel, markLabel, negativeMarkLabel, dateLabel),
                TestpressSdk.getRubikRegularFont(activity!!)
        )
    }

    override fun onUpdateContent(content: Content) {
        val exam = content.rawExam
        exam?.let {
            exam.saveLanguages(context)
            examDao.insertOrReplaceInTx(exam)
            content.examId = exam.id
        }
        contentDao.insertOrReplace(content)
    }

    override fun loadContent() {
        titleView.text = content.title
        titleLayout.visibility = View.VISIBLE

        if (content.rawExam == null || content.attemptsUrl == null) {
            swipeRefresh.isRefreshing = true
            updateContent()
            return
        }

        if (content.attemptsCount > 0) {
            showExamStartOrAttemptsList()

            viewModel.loadAttempts().observe(this, Observer {
                showExamStartOrAttemptsList()
            })
        } else {
            fetchLanguages()
        }
    }

    private fun showExamStartOrAttemptsList() {
        if (viewModel.getAttemptsFromDB().isNotEmpty()) {
            val attempts = viewModel.getAttemptsFromDB()
            if (attempts.size == 1 && attempts[0].assessment.state == STATE_PAUSED) {
                fetchLanguages()
            } else {
                displayAttemptsList()
            }
        }
    }

    private fun displayAttemptsList() {
        val attempts = viewModel.getAttemptsFromDB()
        attemptList.isNestedScrollingEnabled = false
        attemptList.setHasFixedSize(true)
        attemptList.layoutManager = LinearLayoutManager(activity)
        attemptList.adapter = ContentAttemptListAdapter(activity, content, attempts)
        attemptList.visibility = View.VISIBLE
        examDetailsLayout.visibility = View.GONE
        examContentLayout.visibility = View.VISIBLE
        updateStartButton()
    }

    private fun fetchLanguages() {
        swipeRefresh.isRefreshing = true
        viewModel.fetchLanguages().observe(this, Observer {
            swipeRefresh.isRefreshing = false
            when (it?.status) {
                Status.SUCCESS -> displayExamScreen()
                Status.ERROR -> handleError(it.exception!!)
            }
        })
    }

    private fun displayExamScreen() {
        val exam = content.rawExam
        markPerQuestion.text = exam.markPerQuestion
        negativeMarks.text = exam.negativeMarks
        numberOfQuestions.text = exam.numberOfQuestions.toString()
        attemptList.visibility = View.GONE
        examDetailsLayout.visibility = View.VISIBLE
        examContentLayout.visibility = View.VISIBLE
        showExamDuration(exam)
        showOrHideExamDate(exam)
        showExamDescription(exam)
        updateStartButton()
    }

    private fun showExamDescription(exam: Exam) {
        if (!exam.description.isNullOrEmpty()) {
            description.visibility = View.VISIBLE
            descriptionContent.text = exam.description
        }
    }

    private fun showOrHideExamDate(exam: Exam) {
        if (exam.formattedStartDate.equals("forever")) {
            dateLayout.visibility = View.GONE
        } else {
            date.text = "${exam.formattedStartDate} -\n ${exam.formattedEndDate}"
            dateLayout.visibility = View.VISIBLE
        }
    }

    private fun showExamDuration(exam: Exam) {
        val attempts = viewModel.getAttemptsFromDB()

        if (attempts.size == 1 && attempts[0].assessment.state.equals(STATE_PAUSED)) {
            val pausedAttempt = attempts[0]
            durationLabel.text = exam.duration
            examDuration.text = pausedAttempt.assessment.remainingTime
        } else {
            examDuration.text = exam.duration
        }
    }

    private fun updateStartButton() {
        val attempts = viewModel.getAttemptsFromDB()
        val exam = content.rawExam
        var pausedAttempt: CourseAttempt? = null

        for (attempt in attempts) {
            if (attempt.assessment.state.equals(STATE_PAUSED)) {
                pausedAttempt = attempt
                break
            }
        }

        if (pausedAttempt == null && exam.canBeAttempted()) {

            if (attempts.isEmpty()) {
                startButton.text = getString(R.string.testpress_start)
            } else {
                startButton.text = getString(R.string.testpress_retake)
            }

            initStartForFreshExam(exam)
            startButton.visibility = View.VISIBLE
        } else if (pausedAttempt != null && !exam.isWebOnly) {
            startButton.text = getString(R.string.testpress_resume)
            initStartForResumeExam(exam, pausedAttempt)
            startButton.visibility = View.VISIBLE
        } else {
            startButton.visibility = View.GONE
        }
    }

    private fun initStartForFreshExam(exam: Exam) {
        if (exam.hasMultipleLanguages()) {
            MultiLanguagesUtil.supportMultiLanguage(activity, exam, startButton) {
                startCourseExam(true, isPartial = false)
            }
        } else {
            startButton.setOnClickListener {
                RetakeExamUtil.showRetakeOptions(context) { isPartial ->
                    startCourseExam(false, isPartial)
                }
            }
        }
    }

    private fun initStartForResumeExam(exam: Exam, pausedAttempt: CourseAttempt) {
        if (exam.hasMultipleLanguages()) {
            MultiLanguagesUtil.supportMultiLanguage(activity, exam, startButton) {
                resumeCourseExam(true, pausedAttempt)
            }
        } else {
            startButton.setOnClickListener { resumeCourseExam(false, pausedAttempt) }
        }
    }

    private fun startCourseExam(hasMultipleLanguages: Boolean, isPartial: Boolean) {
        TestpressExam.startCourseExam(activity!!, content, hasMultipleLanguages, isPartial,
                TestpressSdk.getTestpressSession(activity!!)!!)
    }

    private fun resumeCourseExam(hasMultipleLanguages: Boolean, pausedCourseAttempt: CourseAttempt) {
        TestpressExam.resumeCourseAttempt(activity!!, content, pausedCourseAttempt, hasMultipleLanguages,
                TestpressSdk.getTestpressSession(activity!!)!!)
    }
}