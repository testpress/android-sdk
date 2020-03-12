package `in`.testpress.course.fragments

import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.R
import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.domain.DomainContentAttempt
import `in`.testpress.course.domain.DomainExamContent
import `in`.testpress.course.domain.DomainLanguage
import `in`.testpress.course.domain.asGreenDaoModel
import `in`.testpress.course.domain.getGreenDaoContent
import `in`.testpress.course.domain.getGreenDaoContentAttempt
import `in`.testpress.course.enums.Status
import `in`.testpress.course.network.Resource
import `in`.testpress.course.repository.ExamContentRepository
import `in`.testpress.course.ui.ContentActivity
import `in`.testpress.course.viewmodels.ExamContentViewModel
import `in`.testpress.exam.TestpressExam
import `in`.testpress.exam.api.TestpressExamApiClient
import `in`.testpress.exam.util.MultiLanguagesUtil
import `in`.testpress.exam.util.RetakeExamUtil
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

open class BaseExamWidgetFragment : Fragment() {
    private lateinit var startButton: Button
    protected lateinit var viewModel: ExamContentViewModel
    protected lateinit var content: DomainContent
    protected var contentId: Long = -1
    lateinit var contentAttempts: ArrayList<DomainContentAttempt>

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
            }
        })
    }

    private fun loadAttemptsAndUpdateStartButton() {
        val observer = Observer<Resource<List<DomainLanguage>>> {
            var exam = content.exam!!
            exam.languages = it.data!!
            content.exam = exam
            updateStartButton(contentAttempts)
        }

        viewModel.loadContentAttempts(content.attemptsUrl!!, contentId)
            .observe(viewLifecycleOwner, Observer { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        contentAttempts = resource.data!!
                        display()
                        val exam = content.exam!!
                        viewModel.getLanguages(exam.slug!!, exam.id)
                            .observe(viewLifecycleOwner, observer)
                    }
                }
            })
    }

    private fun updateStartButton(contentAttempts: ArrayList<DomainContentAttempt>) {
        val exam = content.exam!!
        var pausedAttempt: DomainContentAttempt? = null

        for (attempt in contentAttempts) {
            val greendaoContentAttempt = attempt.getGreenDaoContentAttempt(requireContext())
            if (greendaoContentAttempt?.assessment?.state == TestpressExamApiClient.STATE_PAUSED) {
                pausedAttempt = attempt
                break
            }
        }

        if (pausedAttempt == null && exam.canBeAttempted()) {
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
        if (exam.hasMultipleLanguages()) {
            MultiLanguagesUtil.supportMultiLanguage(activity, exam.asGreenDaoModel(), startButton) {
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

    private fun initStartForResumeExam(
        exam: DomainExamContent,
        pausedAttempt: DomainContentAttempt
    ) {
        if (exam.hasMultipleLanguages()) {
            MultiLanguagesUtil.supportMultiLanguage(activity, exam.asGreenDaoModel(), startButton) {
                resumeCourseExam(true, pausedAttempt)
            }
        } else {
            startButton.setOnClickListener { resumeCourseExam(false, pausedAttempt) }
        }
    }

    private fun startCourseExam(hasMultipleLanguages: Boolean, isPartial: Boolean) {
        val greenDaoContent = content.getGreenDaoContent(requireContext())
        TestpressExam.startCourseExam(
            requireActivity(), greenDaoContent!!, hasMultipleLanguages, isPartial,
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

    open fun display() {}
}