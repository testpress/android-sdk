package `in`.testpress.course.fragments

import `in`.testpress.course.R
import `in`.testpress.course.enums.Status
import `in`.testpress.course.repository.UserSelectedAnswersRepository
import `in`.testpress.course.viewmodels.QuizViewModel
import `in`.testpress.exam.domain.DomainQuestion
import `in`.testpress.exam.domain.DomainUserSelectedAnswer
import `in`.testpress.exam.ui.view.WebView
import `in`.testpress.util.WebViewUtils
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import org.jsoup.Jsoup
import org.jsoup.safety.Whitelist

class QuizReviewFragment: Fragment() {
    private lateinit var questionsView: WebView
    private lateinit var nextButton: MaterialButton

    private lateinit var webViewUtils: WebViewUtils
    lateinit var viewModel: QuizViewModel
    private lateinit var userSelectedAnswer: DomainUserSelectedAnswer
    lateinit var nextQuizHandler: NextQuizHandler

    private var examId: Long = -1
    private var attemptId: Long = -1
    private var position: Int = 0

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
        nextButton = view.findViewById(R.id.submit_button)
        nextButton.background.setColorFilter(ContextCompat.getColor(requireContext(),  R.color.testpress_color_primary), PorterDuff.Mode.SRC_ATOP)
        nextButton.text = "Continue"
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
                    userSelectedAnswer = it.data?.get(position)!!
                    initWebview()
                }
            }
        })

        nextButton.setOnClickListener{
            nextQuizHandler.showNext()
        }
    }

    private fun initWebview() {
        webViewUtils = object : WebViewUtils(questionsView) {
            override fun onLoadFinished() {
                super.onLoadFinished()
                nextButton.visibility = View.VISIBLE
            }

            override fun getHeader(): String? {
                return questionsHeader
            }
        }
        webViewUtils.initWebView(getHtml(), requireActivity())
    }

    private fun getHtml(): String {
        val question = userSelectedAnswer.question!!
        var htmlContent = "<div style='padding-left: 2px; padding-right: 4px;'>"
        htmlContent += getQuestionHtml(question)
        htmlContent += getAnswerHtml(userSelectedAnswer)
        htmlContent += getExplanationHtml(userSelectedAnswer)
        htmlContent += "</div>"
        return htmlContent
    }

    private fun getQuestionHtml(question: DomainQuestion): String {
        var htmlContent = ""
        htmlContent += "<div><div class='review-question-index'>${position + 1}</div>"

        question.directionHtml?.let {
            htmlContent += "<div class='question' style='padding-bottom: 0px;'>${it}</div>"
        }
        htmlContent += "<div class='question'>${question.questionHtml}</div>"
        return htmlContent
    }

    private fun getAnswerHtml(userSelectedAnswer: DomainUserSelectedAnswer): String {
        val question = userSelectedAnswer.question!!
        val selectedAnswers = userSelectedAnswer.selectedAnswers

        var htmlContent = ""
        var correctAnswerHtml = ""
        question.answers?.forEachIndexed { index, answer ->
            var isCorrect = false
            if(question.isSingleMCQType || question.isMultipleMCQType) {
                var optionColor = android.R.color.white
                if (selectedAnswers?.contains(answer.id.toInt()) == true) {
                    optionColor = if (userSelectedAnswer.correctAnswers?.contains(answer.id.toInt()) == true) {
                        R.color.testpress_green
                    } else {
                        R.color.testpress_red
                    }
                }

                if (userSelectedAnswer.correctAnswers?.contains(answer.id.toInt()) == true) {
                    isCorrect = true
                    optionColor = R.color.testpress_green
                    correctAnswerHtml += WebViewUtils.getCorrectAnswerIndexWithTags(index)
                }

                val wl = Whitelist.relaxed()
                val doc = Jsoup.clean(answer.textHtml, wl)

                htmlContent += "\n" + WebViewUtils.getOptionWithTags(
                    doc, index, optionColor, context, isCorrect
                )
            } else if (question.isNumericalType) {
                correctAnswerHtml = answer.textHtml ?: ""
            } else {
                if(index == 0) {
                    htmlContent += "<table width='100%' style='margin-top:0px; margin-bottom:15px;'>${WebViewUtils.getShortAnswerHeadersWithTags()}"
                }
                htmlContent += WebViewUtils.getShortAnswersWithTags(answer.textHtml, answer.marks)
                if (index == question.answers!!.size - 1) {
                    htmlContent += "</table>"
                }
            }

        }

        if (question.isShortAnswerType || question.isNumericalType) {
            htmlContent += "<div style='display:box; display:-webkit-box; margin-bottom:10px;'>" +
                WebViewUtils.getHeadingTags(getString(R.string.testpress_your_answer)) +
                userSelectedAnswer.shortText +
                "</div>"
        }

        if (question.isSingleMCQType || question.isMultipleMCQType || question.isNumericalType) {
            // Add correct answer
            htmlContent += "<div style='display:box; display:-webkit-box; margin-bottom:10px;'>" +
                WebViewUtils.getHeadingTags(getString(R.string.testpress_correct_answer)) +
                correctAnswerHtml +
                "</div>"
        }

        return htmlContent
    }

    private fun getExplanationHtml(userSelectedAnswer: DomainUserSelectedAnswer): String {
        var htmlContent = ""
        userSelectedAnswer.explanationHtml?.let {
            htmlContent +=  WebViewUtils.getHeadingTags(getString(R.string.testpress_explanation))
            htmlContent += "<div class='review-explanation'>${it}</div>"
        }

        return htmlContent
    }
}

interface NextQuizHandler {
    fun showNext()
}