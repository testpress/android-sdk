package `in`.testpress.course.fragments

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.R
import `in`.testpress.enums.Status
import `in`.testpress.course.repository.QuizQuestionsRepository
import `in`.testpress.course.viewmodels.QuizViewModel
import `in`.testpress.exam.api.TestpressExamApiClient
import `in`.testpress.exam.domain.DomainAnswer
import `in`.testpress.exam.domain.DomainUserSelectedAnswer
import `in`.testpress.exam.models.AudiencePollResponse
import `in`.testpress.exam.ui.view.WebView
import `in`.testpress.exam.util.GraphAxisLabelFormatter
import `in`.testpress.models.InstituteSettings
import `in`.testpress.util.UIUtils
import `in`.testpress.util.WebViewUtils
import android.app.ProgressDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.testpress.mikephil.charting.charts.HorizontalBarChart
import com.github.testpress.mikephil.charting.components.XAxis
import com.github.testpress.mikephil.charting.data.BarData
import com.github.testpress.mikephil.charting.data.BarDataSet
import com.github.testpress.mikephil.charting.data.BarEntry
import com.github.testpress.mikephil.charting.formatter.PercentFormatter
import com.github.testpress.mikephil.charting.interfaces.datasets.IBarDataSet

class QuizQuestionFragment : Fragment() {
    private lateinit var questionsView: WebView

    private lateinit var webViewUtils: WebViewUtils
    lateinit var viewModel: QuizViewModel
    private lateinit var userSelectedAnswer: DomainUserSelectedAnswer
    lateinit var quizOperationsCallback: QuizOperationsCallback
    private lateinit var instituteSettings: InstituteSettings

    private var examId: Long = -1
    private var attemptId: Long = -1
    private var position: Int = 0
    private var selectedOptions: ArrayList<Int> = arrayListOf()
    private lateinit var audiencePollProgressDialog : ProgressDialog
    private var audiencePollResponse: AudiencePollResponse? = null
    private lateinit var chart: HorizontalBarChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return QuizViewModel(
                    QuizQuestionsRepository(requireContext())
                ) as T
            }
        }).get(QuizViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.quiz_question_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        parseArguments()
        initializeListeners()
        instituteSettings = TestpressSdk.getTestpressSession(requireContext())!!.instituteSettings;
    }

    private fun bindViews(view: View) {
        questionsView = view.findViewById(R.id.question)
        questionsView.addJavascriptInterface(OptionsSelectionListener(), "OptionsSelectionListener")
    }

    private fun parseArguments() {
        examId = requireArguments().getLong("EXAM_ID", -1)
        attemptId = requireArguments().getLong("ATTEMPT_ID", -1)
        position = requireArguments().getInt("POSITION", 0)
    }

    private fun initializeListeners() {
        viewModel.getUserSelectedAnswers(attemptId).observe(viewLifecycleOwner, Observer {
            when(it.status) {
                Status.SUCCESS -> {
                    userSelectedAnswer = it.data?.sortedBy { it.order }?.get(position)!!
                    initWebview()
                }
            }
        })
    }

    fun submitAnswer() {
        viewModel.submitAnswer(userSelectedAnswer.id!!)
    }

    private fun initWebview() {
        webViewUtils = object : WebViewUtils(questionsView) {
            override fun getHeader(): String {
                return questionsHeader + getTestEngineHeader()
            }

            override fun onLoadFinished() {
                super.onLoadFinished()
                if (instituteSettings.appToolbarLogo != null) {
                    webViewUtils.addWatermark(instituteSettings.appToolbarLogo)
                }
            }
        }
        webViewUtils.initWebView(getHtml(), requireActivity())
    }

    private fun getHtml(): String {
        val question = userSelectedAnswer.question!!
        var htmlContent = "<div class='quiz_question_container' style='font-size:calc(12px + 1.5vw);'>"

        question.directionHtml?.let {
            htmlContent += "<div class='question' style='padding-bottom: 0px;'>${it}</div>"
        }

        // Add question
        htmlContent += "<div class='question' style='padding-bottom: 10px;'> ${question.questionHtml} </div></div>"
        if (question.type == "R" || question.type == "C") {
            // Add options
            htmlContent += "<table id='optionsTable' width='100%' style='margin-top:0px; margin-bottom:20px; font-size:calc(12px + 1.5vw);'>"
            for (answer in question.answers ?: listOf()) {
                val optionLabels = ('A'.code + question.answers?.indexOf(answer)!!).toChar().toString()
                val modifiedText = answer.textHtml?.replace(Regex("<p>(.*?)</p>")) {
                    val originalContent = it.groupValues[1]
                    "<p>$optionLabels. $originalContent</p>"
                }
                htmlContent += if (question.isSingleMCQType) {
                    "\n" + WebViewUtils.getRadioButtonOptionWithTags(
                        modifiedText, answer.id.toInt()
                    )
                } else {
                    "\n" + WebViewUtils.getCheckBoxOptionWithTags(
                        modifiedText, answer.id.toInt()
                    )
                }
            }
            htmlContent += "</table>"
        } else {
            val numberType = question.type == "N"
            questionsView.setNumberType(numberType)
            htmlContent += "<input class='edit_box' type='text' onpaste='return false'" +
                "value='' oninput='onValueChange(this)' placeholder='YOUR ANSWER'>"
        }
        htmlContent += "</div>"

        // Add Helpline options
        htmlContent += getHelplineOptions(question.answers)

        return htmlContent
    }

    private fun getHelplineOptions(answers: List<DomainAnswer>?): String {
        return """
        <div style="display: flex; flex-direction: row; align-items: center; justify-content: space-around;">
            ${get5050Options(answers)}
            ${getAudienceOption()}
            ${getSkipOptions()}
        </div>
    """
    }

    private fun get5050Options(answers: List<DomainAnswer>?): String {
        val incorrectAnswers= getIncorrectAnswerIndices(answers)
        return """
        <div style="display: flex; flex-direction: column; justify-content: space-between;">
            <img class="no-click-listener" src="https://static.testpress.in/static/img/5050.svg" alt="Image 1" style="width: 75px !important; height: 75px !important;">
            <button class='helpline-button' onclick='hideHalfOptions()'>50/50</button>
            <script>
                function hideHalfOptions() {
                    var optionsTable = document.getElementById('optionsTable');
                    var optionsRows = optionsTable.getElementsByTagName('tr');
                    ${incorrectAnswers.joinToString("\n    ") { "optionsRows[$it].style.display = 'none';" }}
                }
            </script>
        </div>
    """
    }

    private fun getIncorrectAnswerIndices(answers: List<DomainAnswer>?): List<Int> {
        val halfOptions = answers?.size!! / 2
        return answers.indices
            .filter { answers[it].isCorrect == false }
            .shuffled()
            .take(halfOptions)
            .sortedDescending()
    }

    private fun getSkipOptions(): String {
        return """
        <div style="display: flex; flex-direction: column; justify-content: space-between;">
            <img class="no-click-listener" src="https://static.testpress.in/static/img/skip.svg" alt="Image 1" style="width: 75px !important; height: 75px !important;">
            <button class='helpline-button' onclick='skipOptions()'>SKIP</button>
            <script>
                function skipOptions() {
                    OptionsSelectionListener.onSkip()
                }
            </script>
        </div>
    """
    }

    private fun getAudienceOption(): String {
        return """
        <div style="display: flex; flex-direction: column; justify-content: space-between;">
            <img class="no-click-listener" src="https://static.testpress.in/static/img/bar-chart.svg" alt="Image 1" style="width: 75px !important; height: 75px !important;">
            <button class='helpline-button' onclick='audienceOptions()'>AUDIENCE</button>
            <script>
                function audienceOptions() {
                    OptionsSelectionListener.onAudienceOptions()
                }
            </script>
        </div>
    """
    }

    private fun getAudiencePollResponse() {
        if (audiencePollResponse != null) {
            showAudiencePollDialog(audiencePollResponse!!)
            return
        }
        showProgressDialog()
        fetchAudiencePoll()
    }

    private fun showProgressDialog() {
        audiencePollProgressDialog = ProgressDialog(requireContext())
        audiencePollProgressDialog.setMessage("Please wait...")
        audiencePollProgressDialog.setCancelable(false)
        audiencePollProgressDialog.setIndeterminate(true)
        UIUtils.setIndeterminateDrawable(requireContext(), audiencePollProgressDialog, 4)
        audiencePollProgressDialog.show()
    }

    private fun fetchAudiencePoll() {
        val apiClient = TestpressExamApiClient(requireContext())
        apiClient.getAudiencePoll("api/v2.5/attempts/${attemptId}/questions/${userSelectedAnswer.questionId}/audience_poll/")
            .enqueue(object : TestpressCallback<AudiencePollResponse>() {
                override fun onSuccess(result: AudiencePollResponse?) {
                    result?.let {
                        audiencePollResponse = result
                        showAudiencePollDialog(audiencePollResponse!!)
                    }
                    audiencePollProgressDialog.dismiss()
                }

                override fun onException(exception: TestpressException) {
                    audiencePollProgressDialog.dismiss()
                    Toast.makeText(
                        requireContext(),
                        "Audience poll not available for this question",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun showAudiencePollDialog(audiencePollResponse: AudiencePollResponse) {
        val builder = AlertDialog.Builder(requireContext(), R.style.TestpressAppCompatAlertDialogStyle)
        val dialogView = View.inflate(requireContext(),R.layout.audience_poll_dialog_layout,null)
        builder.setView(dialogView)
            builder.setNegativeButton(
                "Close"
            ) { dialogInterface, i -> dialogInterface.dismiss() }
        chart = dialogView.findViewById(R.id.chart)
        populateChartValues(audiencePollResponse,chart)
        val dialog = builder.create()
        dialog.show()
    }

    private fun populateChartValues(audiencePollResponse: AudiencePollResponse, chart: HorizontalBarChart) {
        val yValues = extractPollPercent(audiencePollResponse).reversed()
        val xValues = (1..yValues.size).map { it.toFloat() }
        val optionLabels = List(xValues.size) { index -> ('A'.code + index).toChar().toString() }.reversed()
        val entries = xValues.mapIndexed { index, xValue -> BarEntry(xValue, yValues[index]) }
        val barDataSet = BarDataSet(entries, "BarDataSet")
        val iBarDataSets = ArrayList<IBarDataSet>()
        iBarDataSets.add(barDataSet)
        populateChart(chart,iBarDataSets,optionLabels)
    }

    private fun extractPollPercent(audiencePollResponse: AudiencePollResponse): List<Float> {
        return audiencePollResponse.audience_poll?.map { it?.poll_percent?.toFloat()!! }!!
    }

    private fun populateChart(
        chart: HorizontalBarChart,
        sets: ArrayList<IBarDataSet>?,
        optionLabels: List<String>
    ) {
        val data = BarData(sets)
        val xAxis = chart.xAxis
        val labels = ArrayList<String>()

        // Customize X-axis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setLabelCount(optionLabels.size + 2, true)
        xAxis.setDrawGridLines(false)
        xAxis.textSize = 13f
        xAxis.typeface = TestpressSdk.getRubikMediumFont(context!!)
        xAxis.setAvoidFirstLastClipping(true)
        xAxis.xOffset = 10f
        xAxis.textColor = ContextCompat.getColor(activity!!, R.color.testpress_black)
        xAxis.valueFormatter = GraphAxisLabelFormatter(labels, 1)
        xAxis.setAxisMinValue(0f)
        xAxis.setAxisMaxValue((optionLabels.size + 1).toFloat())
        xAxis.axisLineColor = Color.parseColor("#cccccc")

        // Populate labels
        labels.add("")
        labels.addAll(optionLabels)
        labels.add("")

        // Customize chart
        chart.minimumHeight = UIUtils.getPixelFromDp(
            requireContext(),
            Math.max(200, (optionLabels.size + 2) * 50).toFloat()
        ).toInt()
        chart.setDrawValueAboveBar(true)
        chart.setExtraOffsets(0f, 0f, 50f, 0f)
        chart.setDescription("")
        chart.setFitBars(true)
        chart.setTouchEnabled(false)
        chart.legend.isEnabled = false

        // Customize data
        data.barWidth = 0.4f
        data.setValueTextSize(12f)
        data.setValueFormatter(PercentFormatter())
        data.setValueTypeface(TestpressSdk.getRubikMediumFont(context!!))

        // Customize left axis
        val leftAxis = chart.axisLeft
        leftAxis.setDrawAxisLine(false)
        leftAxis.setDrawLabels(false)
        leftAxis.setDrawGridLines(false)
        leftAxis.setAxisMinValue(0f)
        leftAxis.setAxisMaxValue(100f)
        leftAxis.spaceTop = 15f

        // Customize right axis
        val rightAxis = chart.axisRight
        rightAxis.setAxisMinValue(0f)
        rightAxis.setAxisMaxValue(100f)
        rightAxis.setLabelCount(5, false)
        rightAxis.textSize = 10f
        rightAxis.setDrawLabels(true)
        rightAxis.setDrawAxisLine(true)
        rightAxis.setDrawGridLines(true)
        rightAxis.typeface = TestpressSdk.getRubikRegularFont(context!!)
        rightAxis.textColor = ContextCompat.getColor(activity!!, R.color.testpress_text_gray)
        rightAxis.gridColor = Color.parseColor("#cccccc")

        // Set data and animate chart
        chart.data = data
        chart.animateY(500)
        chart.invalidate()
    }

    inner class OptionsSelectionListener {
        @JavascriptInterface
        fun onCheckedChange(id: String, checked: Boolean, radioOption: Boolean) {
            if (checked) {
                if (radioOption) {
                    selectedOptions.clear()
                }
                selectedOptions.add(id.toInt())
            } else {
                selectedOptions.remove(id.toInt())
            }
            viewModel.setAnswer(userSelectedAnswer.id!!, selectedOptions)
        }

        @JavascriptInterface
        fun onSkip() {
            quizOperationsCallback.onSkip()
        }

        @JavascriptInterface
        fun onAudienceOptions() {
            getAudiencePollResponse()
        }

    }
}

interface QuizOperationsCallback {
    fun onSkip()
}