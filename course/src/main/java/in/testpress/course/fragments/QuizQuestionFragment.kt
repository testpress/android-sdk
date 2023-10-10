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

        // Add 50-50 helpline button
        htmlContent += "<button id='changeOptionsButton' onclick='hideExtraOptions()'>Change Options</button>" +
                "<script>\n" +
                "    function hideExtraOptions() {\n" +
                "        var optionsTable = document.getElementById('optionsTable');\n" +
                "        var optionsRows = optionsTable.getElementsByTagName('tr');\n" +
                get5050Options(question.answers) +
                "    }\n" +
                "</script>"

        htmlContent += "    <table id=\"helplinesTable\" border=\"0\" style=\"width: 100%; height: 100px; border-collapse: collapse;\">\n" +
                "        <tr>\n" +
                "            <td>\n" +
                "                <div style=\"text-align: center;\">\n" +
                "                    <img src=\"https://static.testpress.in/static/img/5050.svg\" width=\"50\" height=\"50\">\n" +
                "                    <br>\n" +
                "                    <button style=\"display: block; margin: 0 auto;\">50/50</button>\n" +
                "                </div>\n" +
                "            </td>\n" +
                "            <td>\n" +
                "                <div style=\"text-align: center;\">\n" +
                "                    <img src=\"https://static.testpress.in/static/img/5050.svg\" width=\"50\" height=\"50\">\n" +
                "                    <br>\n" +
                "                    <button style=\"display: block; margin: 0 auto;\">50/50</button>\n" +
                "                </div>\n" +
                "            </td>\n" +
                "            <td>\n" +
                "                <div style=\"text-align: center;\">\n" +
                "                    <img src=\"https://static.testpress.in/static/img/5050.svg\" width=\"50\" height=\"50\">\n" +
                "                    <br>\n" +
                "                    <button style=\"display: block; margin: 0 auto;\">50/50</button>\n" +
                "                </div>\n" +
                "            </td>\n" +
                "        </tr>\n" +
                "    </table>\n"

        htmlContent += "</div>"

        return htmlContent
    }

    private fun get5050Options(answers: List<DomainAnswer>?) :String {
        val halfOptions = answers?.size!! / 2
        val incorrectIndices = answers.indices
            .filter { answers[it].isCorrect == false }
            .shuffled()
            .take(halfOptions)
            .sortedDescending()
        var hideWrongOptionScript = ""
        for (index in incorrectIndices){
            hideWrongOptionScript += "optionsRows[$index].style.display = 'none';"
        }
        return hideWrongOptionScript
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