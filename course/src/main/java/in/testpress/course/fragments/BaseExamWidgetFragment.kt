package `in`.testpress.course.fragments

import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.R
import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.domain.DomainContentAttempt
import `in`.testpress.exam.domain.DomainExamContent
import `in`.testpress.exam.domain.DomainLanguage
import `in`.testpress.exam.domain.ExamTemplateType.IELTS_TEMPLATE
import `in`.testpress.exam.domain.asGreenDaoModel
import `in`.testpress.course.domain.getGreenDaoContent
import `in`.testpress.course.domain.getGreenDaoContentAttempt
import `in`.testpress.exam.domain.toGreenDaoModels
import `in`.testpress.enums.Status
import `in`.testpress.network.Resource
import `in`.testpress.course.repository.ExamContentRepository
import `in`.testpress.course.ui.ContentActivity
import `in`.testpress.course.ui.QuizActivity
import `in`.testpress.course.ui.WebViewWithSSO
import `in`.testpress.course.viewmodels.ExamContentViewModel
import `in`.testpress.exam.TestpressExam
import `in`.testpress.exam.api.TestpressExamApiClient
import `in`.testpress.exam.util.MultiLanguagesUtil
import `in`.testpress.exam.util.RetakeExamUtil
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

open class BaseExamWidgetFragment : Fragment() {
    lateinit var startButton: Button
    protected lateinit var viewModel: ExamContentViewModel
    protected lateinit var content: DomainContent
    protected var contentId: Long = -1
    lateinit var contentAttempts: ArrayList<DomainContentAttempt>
    protected lateinit var examRefreshListener: ExamRefreshListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ExamContentViewModel(
                    ExamContentRepository(context!!)
                ) as T
            }
        }).get(ExamContentViewModel::class.java)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (parentFragment != null) {
            examRefreshListener = parentFragment as ExamRefreshListener
        } else {
            examRefreshListener = context as ExamRefreshListener
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startButton = view.findViewById(R.id.start_exam)
        contentId = requireArguments().getLong(ContentActivity.CONTENT_ID)

        viewModel.getContent(contentId).observe(viewLifecycleOwner, Observer {
            when (it?.status) {
                Status.SUCCESS -> {
                    content = it.data!!
                    loadAttemptsAndUpdateStartButton()
                }
                else -> {}
            }
        })
    }

    fun loadAttemptsAndUpdateStartButton() {
        val observer = Observer<Resource<List<DomainLanguage>>> { resource ->
            examRefreshListener.showOrHideRefresh(false)
            when(resource.status) {
                Status.SUCCESS -> {
                    var exam = content.exam!!
                    exam.languages = resource.data!!
                    content.exam = exam
                    updateStartButton(contentAttempts)
                }
                else -> {
                    var exam = content.exam!!
                    content.exam = exam
                    updateStartButton(contentAttempts)
                }
            }
        }

        examRefreshListener.showOrHideRefresh(true)
        var url = content.attemptsUrl ?: "/api/v2.3/contents/${content.id}/attempts/"
        viewModel.loadContentAttempts(url, contentId)
            .observe(viewLifecycleOwner, Observer { resource ->
                examRefreshListener.showOrHideRefresh(false)
                when (resource.status) {
                    Status.SUCCESS -> {
                        contentAttempts = resource.data!!
                        display()
                        val exam = content.exam!!
                        examRefreshListener.showOrHideRefresh(true)
                        viewModel.getLanguages(exam.slug!!, exam.id)
                            .observe(viewLifecycleOwner, observer)
                    }
                    else -> {}
                }
            })
    }

    open fun updateStartButton(contentAttempts: ArrayList<DomainContentAttempt>) {
        val exam = content.exam!!
        var pausedAttempt: DomainContentAttempt? = null

        for (attempt in contentAttempts) {
            val greendaoContentAttempt = attempt.getGreenDaoContentAttempt(requireContext())
            if (greendaoContentAttempt?.assessment?.state == TestpressExamApiClient.STATE_PAUSED) {
                pausedAttempt = attempt
                break
            }
        }

        updateStartButtonTextAndVisibility(exam, pausedAttempt)
        updateStartButtonListener(exam, pausedAttempt)
    }

    private fun updateStartButtonTextAndVisibility(exam: DomainExamContent, pausedAttempt: DomainContentAttempt?) {
        if (pausedAttempt == null && content.canAttemptExam()) {
            if (contentAttempts.isEmpty()) {
                startButton.text = getString(R.string.testpress_start)
            } else {
                startButton.text = getString(R.string.testpress_retake)
            }
            startButton.visibility = View.VISIBLE
        } else if (pausedAttempt != null && !exam.isWebOnly()) {
            startButton.text = getString(R.string.testpress_resume)
            startButton.visibility = View.VISIBLE
        } else {
            startButton.visibility = View.GONE
        }
    }

    private fun updateStartButtonListener(exam: DomainExamContent, pausedAttempt: DomainContentAttempt?) {
        if (content.contentType.equals("Quiz", ignoreCase = true)) {
            startButton.setOnClickListener { startQuizActivity(exam) }
            return
        }

        if (pausedAttempt == null && content.canAttemptExam()) {
            if (contentAttempts.isEmpty()) {
                startButton.text = getString(R.string.testpress_start)
            } else {
                startButton.text = getString(R.string.testpress_retake)
            }
            initStartForFreshExam(exam)
            startButton.visibility = View.VISIBLE
        } else if (pausedAttempt != null && !exam.isWebOnly()) {
            startButton.text = getString(R.string.testpress_resume)
            initStartForResumeExam(exam, pausedAttempt)
            startButton.visibility = View.VISIBLE
        } else {
            startButton.visibility = View.GONE
        }
    }

    private fun initStartForFreshExam(exam: DomainExamContent) {
        if (exam.templateType == IELTS_TEMPLATE) {
            startButton.setOnClickListener {startExamInWebview(content)}
        } else if (contentAttempts.isEmpty()) {
            MultiLanguagesUtil.supportMultiLanguage(requireActivity(), exam.asGreenDaoModel(), startButton) {
                showExamModesOrStartExam(exam, shouldShowExamDetails(exam), isPartial = false)
            }
        } else {
            startButton.setOnClickListener {
                RetakeExamUtil.showRetakeOptions(context) { isPartial ->
                    showExamModesOrStartExam(exam, discardExamDetails = false,isPartial)
                }
            }
        }
    }

    private fun shouldShowExamDetails(exam: DomainExamContent): Boolean {
        // If 'isAttemptResumeDisabled' is true, we return false to display the Exam Detail page.
        // Otherwise, we return false to start the exam without displaying the Exam Detail page.
        return !exam.isAttemptResumeDisabled()
    }

    private fun showExamModesOrStartExam(
        exam: DomainExamContent,
        discardExamDetails: Boolean,
        isPartial: Boolean
    ) {
        if (exam.isQuizModeEnabled()) {
            showExamModeDialog(exam) { (startCourseExam(discardExamDetails,isPartial)) }
        } else {
            startCourseExam(discardExamDetails,isPartial)
        }
    }

    private fun showExamModeDialog(exam: DomainExamContent, action: () -> Unit) {
        val options = arrayOf("Regular Mode", "Quiz Mode")
        var selectedOption = 0
        val builder =
            AlertDialog.Builder(requireContext(), R.style.TestpressAppCompatAlertDialogStyle)
        builder.setTitle("Select Exam Mode")
        builder.setSingleChoiceItems(options, selectedOption) { _, which ->
            selectedOption = which
        }
        builder.setPositiveButton("OK") { dialog: DialogInterface?, which: Int ->
            when(selectedOption){
                0 -> action.invoke()
                1 -> startQuizActivity(exam)
            }
        }
        builder.setNegativeButton("Cancel") { _, _ -> }
        builder.create().show()
    }

    private fun startExamInWebview(content: DomainContent) {
            content.examStartUrl?.let {
                startActivity(WebViewWithSSO.createIntent(requireContext(), content.examStartUrl!!, content.title
                        ?: ""))
            }
    }

    private fun initStartForResumeExam(
        exam: DomainExamContent,
        pausedAttempt: DomainContentAttempt
    ) {
        if (exam.isAttemptResumeDisabled()){
            startButton.setOnClickListener {
                endExam(pausedAttempt)
            }
            return
        }
        if (exam.templateType == IELTS_TEMPLATE || exam.hasAudioQuestions == true) {
            startButton.setOnClickListener { startExamInWebview(content) }
        } else if (contentAttempts.isEmpty()) {
            MultiLanguagesUtil.supportMultiLanguage(activity, exam.asGreenDaoModel(), startButton) {
                resumeExamBasedOnAttemptType(exam, true, pausedAttempt)
            }
        } else {
            startButton.setOnClickListener {
                resumeExamBasedOnAttemptType(exam, false, pausedAttempt)
            }
        }
    }

    private fun endExam(pausedCourseAttempt: DomainContentAttempt) {
        val greenDaoContent = content.getGreenDaoContent(requireContext())
        greenDaoContent?.exam?.refresh()
        TestpressExam.endCourseAttempt(
            requireActivity(),
            greenDaoContent!!,
            pausedCourseAttempt.getGreenDaoContentAttempt(requireContext())!!,
            TestpressSdk.getTestpressSession(requireActivity())!!
        )
    }

    private fun resumeExamBasedOnAttemptType(
        exam: DomainExamContent,
        hasMultipleLanguages: Boolean,
        pausedAttempt: DomainContentAttempt
    ) {
        if (exam.isQuizModeEnabled()) {
            if (pausedAttempt.assessment?.isAttemptTypeQuiz() == true){
                startQuizActivity(exam)
            } else {
                resumeCourseExam(hasMultipleLanguages, pausedAttempt)
            }
        } else {
            resumeCourseExam(hasMultipleLanguages, pausedAttempt)
        }
    }

    private fun startCourseExam(discardExamDetails: Boolean, isPartial: Boolean) {
        val greenDaoContent = content.getGreenDaoContent(requireContext())
        greenDaoContent?.exam?.refresh()
        val languages = content.exam?.languages
        greenDaoContent?.exam?.languages = languages?.toGreenDaoModels()
        TestpressExam.startCourseExam(
            requireActivity(), greenDaoContent!!, discardExamDetails, isPartial,
            TestpressSdk.getTestpressSession(requireActivity())!!
        )
    }

    private fun resumeCourseExam(
        hasMultipleLanguages: Boolean,
        pausedCourseAttempt: DomainContentAttempt
    ) {
        val greenDaoContent = content.getGreenDaoContent(requireContext())
        TestpressExam.resumeCourseAttempt(
            requireActivity(),
            greenDaoContent!!,
            pausedCourseAttempt.getGreenDaoContentAttempt(requireContext())!!,
            hasMultipleLanguages,
            TestpressSdk.getTestpressSession(requireActivity())!!
        )
    }

    private fun startQuizActivity(exam: DomainExamContent) {
        val intent = Intent(requireContext(), QuizActivity::class.java).apply {
            putExtra(ContentActivity.CONTENT_ID, content.id)
            putExtra("EXAM_ID", exam.id)
            putExtra("ATTEMPT_URL", exam.attemptsUrl)
        }
        requireActivity().startActivity(intent)
    }

    open fun display() {}
}