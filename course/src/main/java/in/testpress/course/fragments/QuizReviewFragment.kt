package `in`.testpress.course.fragments

import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.R
import `in`.testpress.enums.Status
import `in`.testpress.course.repository.QuizQuestionsRepository
import `in`.testpress.course.viewmodels.QuizViewModel
import `in`.testpress.exam.domain.DomainQuestion
import `in`.testpress.exam.domain.DomainUserSelectedAnswer
import `in`.testpress.exam.ui.view.WebView
import `in`.testpress.exam.util.Watermark
import `in`.testpress.models.InstituteSettings
import `in`.testpress.util.ViewUtils
import `in`.testpress.util.WebViewUtils
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.jsoup.Jsoup
import org.jsoup.safety.Whitelist
import kotlin.math.roundToInt

class QuizReviewFragment: Fragment() {
    private lateinit var questionsView: WebView
    lateinit var difficultyTitle: TextView
    lateinit var difficultyPercentageText: TextView
    lateinit var difficultyLevelContainer: LinearLayout
    lateinit var usersAnsweredRight: TextView
    lateinit var imageView1: ImageView
    lateinit var imageView2: ImageView
    lateinit var imageView3: ImageView
    lateinit var imageView4: ImageView
    lateinit var imageView5: ImageView

    private lateinit var webViewUtils: WebViewUtils
    lateinit var viewModel: QuizViewModel
    private lateinit var userSelectedAnswer: DomainUserSelectedAnswer
    lateinit var nextQuizHandler: NextQuizHandler
    private lateinit var instituteSettings: InstituteSettings

    private var examId: Long = -1
    private var attemptId: Long = -1
    private var position: Int = 0

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
        difficultyLevelContainer = view.findViewById(R.id.difficulty_layout)
        difficultyTitle = view.findViewById(R.id.difficulty_title)
        difficultyPercentageText = view.findViewById(R.id.difficulty_percentage)
        usersAnsweredRight = view.findViewById(R.id.users_answered_right)
        imageView1 = view.findViewById(R.id.difficulty1)
        imageView2 = view.findViewById(R.id.difficulty2)
        imageView3 = view.findViewById(R.id.difficulty3)
        imageView4 = view.findViewById(R.id.difficulty4)
        imageView5 = view.findViewById(R.id.difficulty5)

        ViewUtils.setTypeface(
            arrayOf(difficultyTitle, difficultyPercentageText),
            TestpressSdk.getRubikMediumFont(context!!)
        )
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
    }

    private fun initWebview() {
        webViewUtils = object : WebViewUtils(questionsView) {
            override fun getHeader(): String? {
                return questionsHeader + getBookmarkHandlerScript()
            }

            override fun onLoadFinished() {
                super.onLoadFinished()
                if (context == null) return

                setDifficulty()

                if (instituteSettings.isGrowthHackEnabled || instituteSettings.baseUrl.contains("edzorb")) {
                    webViewUtils.addWatermark(instituteSettings.appToolbarLogo)
                }
            }
        }
        webViewUtils.initWebView(getHtml(), requireActivity())
    }

    private fun setDifficulty() {
        val percentageCorrect = getPercentageGotCorrect() ?: return
        difficultyPercentageText.text = "${percentageCorrect.roundToInt()}%"
        difficultyLevelContainer.visibility = View.VISIBLE

        if (percentageCorrect >= 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                imageView1.background =
                    resources.getDrawable(R.drawable.testpress_difficulty_left_on)
            } else {
                imageView1.setBackgroundColor(resources.getColor(R.color.testpress_difficulty_level_1))
            }
        }
        if (percentageCorrect > 20) {
            imageView2.setBackgroundColor(
                ContextCompat.getColor(
                    context!!,
                    R.color.testpress_difficulty_level_2
                )
            )
        }
        if (percentageCorrect > 40) {
            imageView3.setBackgroundColor(
                ContextCompat.getColor(
                    context!!,
                    R.color.testpress_difficulty_level_3
                )
            )
        }
        if (percentageCorrect > 60) {
            imageView4.setBackgroundColor(
                ContextCompat.getColor(
                    context!!,
                    R.color.testpress_difficulty_level_4
                )
            )
        }
        if (percentageCorrect > 80) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                imageView5.background =
                    resources.getDrawable(R.drawable.testpress_difficulty_right_on)
            } else {
                imageView5.setBackgroundColor(resources.getColor(R.color.testpress_difficulty_level_5))
            }
        }
    }

    private fun getPercentageGotCorrect(): Float? {
        return userSelectedAnswer.question?.percentageGotCorrect?.toFloatOrNull()
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
            htmlContent += getEmailWaterMarkHtml()
            htmlContent += "<div class='review-explanation'>${it}</div>"
        }

        return htmlContent
    }

    private fun getEmailWaterMarkHtml(): String {
        return """<div class ='watermark'>© ${getString(R.string.testpress_app_name)}
                        ${Watermark().get(activity)} </div>"""
    }
}

interface NextQuizHandler {
    fun showNext()
}