package `in`.testpress.course.fragments

import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.R
import `in`.testpress.enums.Status
import `in`.testpress.course.repository.QuizQuestionsRepository
import `in`.testpress.course.viewmodels.QuizViewModel
import `in`.testpress.exam.domain.DomainAnswer
import `in`.testpress.exam.domain.DomainUserSelectedAnswer
import `in`.testpress.exam.ui.view.WebView
import `in`.testpress.models.InstituteSettings
import `in`.testpress.util.WebViewUtils
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.util.ArrayList

class QuizQuestionFragment : Fragment() {
    private lateinit var questionsView: WebView

    private lateinit var webViewUtils: WebViewUtils
    lateinit var viewModel: QuizViewModel
    private lateinit var userSelectedAnswer: DomainUserSelectedAnswer
    lateinit var quizFragmentHandler: QuizFragmentHandler
    private lateinit var instituteSettings: InstituteSettings

    private var examId: Long = -1
    private var attemptId: Long = -1
    private var position: Int = 0
    private var selectedOptions: ArrayList<Int> = arrayListOf()

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
                htmlContent += if (question.isSingleMCQType) {
                    "\n" + WebViewUtils.getRadioButtonOptionWithTags(
                        answer.textHtml, answer.id.toInt()
                    )
                } else {
                    "\n" + WebViewUtils.getCheckBoxOptionWithTags(
                        answer.textHtml, answer.id.toInt()
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
        </div>
    """
    }

    private fun get5050Options(answers: List<DomainAnswer>?): String {
        return """
        <div style="display: flex; flex-direction: column; justify-content: space-between;">
            <img src="https://static.testpress.in/static/img/5050.svg" alt="Image 1" style="width: 75px !important; height: 75px !important;">
            <button class='helpline-button' onclick='hideHalfOptions()'>50/50</button>
            <script>
                ${getHideHalfOptionsScript(answers)}
            </script>
        </div>
    """
    }

    private fun getHideHalfOptionsScript(answers: List<DomainAnswer>?): String {
        val incorrectIndices = getIncorrectIndices(answers)
        return """
        |function hideHalfOptions() {
        |    var optionsTable = document.getElementById('optionsTable');
        |    var optionsRows = optionsTable.getElementsByTagName('tr');
        |    ${incorrectIndices.joinToString("\n    ") { "optionsRows[$it].style.display = 'none';" }}
        |}
    """.trimMargin()
    }

    private fun getIncorrectIndices(answers: List<DomainAnswer>?): List<Int> {
        val halfOptions = answers?.size!! / 2
        return answers.indices
            .filter { answers[it].isCorrect == false }
            .shuffled()
            .take(halfOptions)
            .sortedDescending()
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
    }
}

interface QuizFragmentHandler {
    fun changeFragment()
}