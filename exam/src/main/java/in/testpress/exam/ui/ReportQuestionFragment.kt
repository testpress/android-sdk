package `in`.testpress.exam.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.core.TestpressSdk
import `in`.testpress.enums.Status
import `in`.testpress.exam.R
import `in`.testpress.exam.api.TestpressExamApiClient
import `in`.testpress.exam.databinding.ReportQuestionFragmentBinding
import `in`.testpress.exam.models.ReportQuestionResponse
import `in`.testpress.exam.repository.ReportQuestionRepository
import `in`.testpress.exam.ui.viewmodel.ReportQuestionViewModel
import `in`.testpress.network.Resource

class ReportQuestionFragment : Fragment() {

    private var questionId: String = ""
    private var examId: String = ""
    private lateinit var apiClient: TestpressExamApiClient
    private lateinit var binding: ReportQuestionFragmentBinding
    private lateinit var viewModel: ReportQuestionViewModel
    private var position = -1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        apiClient = TestpressExamApiClient(requireContext())
        parseArguments()
        initializeViewModel()
    }

    private fun parseArguments() {
        questionId = arguments?.getString("question_id")!!
        examId = arguments?.getString("exam_id")!!
    }

    private fun initializeViewModel() {
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ReportQuestionViewModel(
                    ReportQuestionRepository(apiClient),
                    questionId
                ) as T
            }
        }).get(ReportQuestionViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ReportQuestionFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeClickListener()
        initializeViewModelObserves()
        assignCustomFontToText()
    }

    private fun initializeClickListener() {
        binding.submitButton.setOnClickListener {
            submit()
        }
        binding.retryButton.setOnClickListener {
            retry()
        }
        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            onChange(checkedId)
        }
    }

    private fun submit() {
        if (position == -1) {
            binding.radioButtonError.isVisible = true
            return
        }
        viewModel.submitReportQuestion(
            binding.discriptionInput.text.toString(),
            examId,
            (++position).toString()
        )
        hideReportQuestionPage()
    }

    private fun retry() {
        viewModel.retry()
        binding.errorLayout.isVisible = false
        showOrHideLoading(true)
    }

    private fun onChange(checkedId: Int) {
        position = binding.radioGroup.indexOfChild(view?.findViewById<RadioButton>(checkedId))
    }

    private fun initializeViewModelObserves() {
        viewModel.questionReport.observe(this) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    showOrHideLoading(true)
                }
                Status.SUCCESS -> {
                    showOrHideLoading(false)
                    if (resource.data != null) {
                        showPageBasedOnResponse(resource.data!!)
                    } else {
                        showNetworkErrorMessage(false)
                    }
                }
                Status.ERROR -> {
                    showOrHideLoading(false)
                    showNetworkErrorMessage(true)
                }
            }
        }

        viewModel.submitReport.observe(this) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    showOrHideLoading(true)
                }
                Status.SUCCESS -> {
                    showOrHideLoading(false)
                    if (resource.data != null) {
                        showSucessMessage(resource.data!!)
                    } else {
                        showNetworkErrorMessage(false)
                    }
                }
                Status.ERROR -> {
                    showOrHideLoading(false)
                }
            }
        }
    }

    private fun assignCustomFontToText() {
        binding.also {
            //Title
            it.reportSucessfullyTitle.typeface =
                TestpressSdk.getRubikMediumFont(binding.root.context)
            it.reportQuestionTitle.typeface = TestpressSdk.getRubikMediumFont(binding.root.context)
            // Radio button
            it.errorInQuestion.typeface = TestpressSdk.getRubikRegularFont(binding.root.context)
            it.incompleteSolution.typeface = TestpressSdk.getRubikRegularFont(binding.root.context)
            it.incorrectSolution.typeface = TestpressSdk.getRubikRegularFont(binding.root.context)
            it.noSolution.typeface = TestpressSdk.getRubikRegularFont(binding.root.context)
            it.others.typeface = TestpressSdk.getRubikRegularFont(binding.root.context)
            //Others
            it.reportQuestionTitle2.typeface =
                TestpressSdk.getRubikRegularFont(binding.root.context)
            it.reportedDescription.typeface = TestpressSdk.getRubikRegularFont(binding.root.context)
            it.reportedReason.typeface = TestpressSdk.getRubikRegularFont(binding.root.context)
            it.reportedTime.typeface = TestpressSdk.getRubikRegularFont(binding.root.context)
            it.thankyouText.typeface = TestpressSdk.getRubikRegularFont(binding.root.context)
            it.submitButton.typeface = TestpressSdk.getRubikRegularFont(binding.root.context)
            it.resolverText.typeface = TestpressSdk.getRubikRegularFont(binding.root.context)
        }
    }

    private fun showOrHideLoading(show: Boolean) {
        binding.pbLoading.isVisible = show
    }

    private fun showPageBasedOnResponse(result: ReportQuestionResponse) {
        if (result.isReportResolved == true) {
            showReportResolvedPage()
            return
        }
        if (!result.results.isNullOrEmpty()) {
            showSucessMessage(result.results[0])
            return
        }
        showReportQuestionPage()
    }

    private fun showReportQuestionPage() {
        binding.reportQuestionLayout.isVisible = true
    }

    private fun showSucessMessage(result: ReportQuestionResponse.ReportQuestion) {
        binding.reportedReason.text = "Reason: %s".format(result.typeDisplay)
        binding.reportedTime.text =
            "Time: %s".format(result.getFormattedDate(requireContext()) ?: "NA")
        binding.reportedDescription.text =
            "Description: %s".format(result.getFormattedDescription().ifEmpty { "NA" })
        binding.sucessMessageLayout.visibility = View.VISIBLE
    }

    private fun hideReportQuestionPage() {
        binding.reportQuestionLayout.isVisible = false
    }

    private fun showReportResolvedPage() {
        binding.resolvedLayout.visibility = View.VISIBLE
    }

    private fun showNetworkErrorMessage(showRetryButton: Boolean) {
        binding.apply {
            errorTitle.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_error_outline_black_18dp,
                0,
                0,
                0
            )
            errorLayout.isVisible = showRetryButton
        }
    }

    companion object {
        fun show(
            fragmentActivity: FragmentActivity,
            fragmentView: Int,
            questionId: String,
            examId: String,
        ) {
            val bundle = Bundle()
            bundle.putString("question_id", questionId)
            bundle.putString("exam_id", examId)
            val fragment = ReportQuestionFragment()
            fragment.arguments = bundle
            fragmentActivity.supportFragmentManager.beginTransaction()
                .replace(fragmentView, fragment)
                .commitAllowingStateLoss()
        }
    }
}