package `in`.testpress.course.fragments

import `in`.testpress.course.R
import `in`.testpress.course.enums.Status
import `in`.testpress.course.repository.UserSelectedAnswersRepository
import `in`.testpress.course.viewmodels.QuizViewModel
import `in`.testpress.exam.domain.DomainUserSelectedAnswer
import `in`.testpress.exam.ui.view.WebView
import `in`.testpress.util.WebViewUtils
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import java.util.ArrayList

class QuizQuestionFragment : Fragment() {
    private lateinit var questionsView: WebView
    private lateinit var submitButton: MaterialButton

    private lateinit var webViewUtils: WebViewUtils
    lateinit var viewModel: QuizViewModel
    private lateinit var userSelectedAnswer: DomainUserSelectedAnswer
    lateinit var quizFragmentHandler: QuizFragmentHandler

    private var examId: Long = -1
    private var position: Int = 0
    private var selectedOptions: ArrayList<Int> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return QuizViewModel(
                    UserSelectedAnswersRepository(requireContext())
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
    }

    private fun bindViews(view: View) {
        questionsView = view.findViewById(R.id.question)
        questionsView.addJavascriptInterface(OptionsSelectionListener(), "OptionsSelectionListener")
        submitButton = view.findViewById(R.id.submit_button)
    }

    private fun parseArguments() {
        examId = requireArguments().getLong("EXAM_ID", -1)
        position = requireArguments().getInt("POSITION", 0)
    }

    private fun initializeListeners() {
        submitButton.setOnClickListener {
            submitButton.isEnabled = false
            submitButton.background.setColorFilter(resources.getColor(R.color.testpress_text_gray_medium), PorterDuff.Mode.SRC_ATOP)
            submitButton.text = "Checking"

            viewModel.submitAnswer(userSelectedAnswer.id!!).observe(viewLifecycleOwner, Observer {
                submitButton.isEnabled = true
                when(it?.status) {
                    Status.SUCCESS -> quizFragmentHandler.changeFragment()
                    else -> {
                        Toast.makeText(requireContext(), "Failed to save answer. Please try again", Toast.LENGTH_SHORT).show()
                        submitButton.isEnabled = true
                        submitButton.text = "Check"
                        submitButton.background.setColorFilter(resources.getColor(R.color.testpress_color_primary), PorterDuff.Mode.SRC_ATOP)
                    }
                }
            })
        }

        viewModel.getUserSelectedAnswers(examId).observe(viewLifecycleOwner, Observer {
            when(it.status) {
                Status.SUCCESS -> {
                    userSelectedAnswer = it.data?.get(position)!!
                    initWebview()
                }
            }
        })
    }

    private fun initWebview() {
        webViewUtils = object : WebViewUtils(questionsView) {
            override fun getHeader(): String {
                return questionsHeader + getTestEngineHeader()
            }

            override fun onLoadFinished() {
                super.onLoadFinished()
                submitButton.visibility = View.VISIBLE
            }

        }
        webViewUtils.initWebView(getHtml(), requireActivity())
    }

    private fun getHtml(): String {
        val question = userSelectedAnswer.question!!
        var htmlContent = "<div class='quiz_question_container'>"

        // Add index
        htmlContent += "<div style='padding-right:10px; padding-left:10px;'>" +
            "<div class='question-index'>${position + 1}</div>"

        question.directionHtml?.let {
            htmlContent += "<div class='question' style='padding-bottom: 0px;'>${it}</div>"
        }

        // Add question
        htmlContent += "<div class='question' style='padding-bottom: 10px;'> ${question.questionHtml} </div></div>"
        if (question.type == "R" || question.type == "C") {
            // Add options
            htmlContent += "<table width='100%' style='margin-top:0px; margin-bottom:20px;'>"
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

        return htmlContent
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