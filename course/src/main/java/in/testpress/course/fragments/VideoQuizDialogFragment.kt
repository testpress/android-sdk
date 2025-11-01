package `in`.testpress.course.fragments

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.DialogFragment
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayout
import com.google.gson.Gson
import `in`.testpress.course.R
import `in`.testpress.course.network.NetworkAnswer
import `in`.testpress.course.network.NetworkVideoQuestion
import java.util.regex.Pattern

class VideoQuizDialogFragment : DialogFragment() {

    private lateinit var question: NetworkVideoQuestion
    private var listener: OnQuizCompleteListener? = null

    private var answerViews = mutableListOf<View>()
    private var selectedAnswerIds = mutableListOf<Long>()
    private var isCorrect = false
    private var gapFillResults = mutableListOf<Boolean>()
    private var checkButton: Button? = null

    interface OnQuizCompleteListener {
        fun onQuizCompleted(questionId: Long)
    }

    companion object {
        private const val ARG_QUESTION = "ARG_QUESTION"

        fun newInstance(question: NetworkVideoQuestion): VideoQuizDialogFragment {
            val fragment = VideoQuizDialogFragment()
            val args = Bundle()
            args.putString(ARG_QUESTION, Gson().toJson(question))
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        question = Gson().fromJson(
            arguments?.getString(ARG_QUESTION),
            NetworkVideoQuestion::class.java
        )
        if (parentFragment is OnQuizCompleteListener) {
            listener = parentFragment as OnQuizCompleteListener
        } else {
            throw RuntimeException("$parentFragment must implement OnQuizCompleteListener")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity(), R.style.TestpressAppCompatAlertDialogStyle)
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.fragment_quiz_dialog, null)

        val questionText: TextView = view.findViewById(R.id.quiz_question_text)
        val optionsContainer: LinearLayout = view.findViewById(R.id.quiz_options_container)
        checkButton = view.findViewById(R.id.quiz_check_button)
        val continueButton: Button = view.findViewById(R.id.quiz_continue_button)
        val feedbackText: TextView = view.findViewById(R.id.quiz_feedback_text)

        if (question.question.type == "G") {
            questionText.visibility = View.GONE
        } else {
            questionText.text = HtmlCompat.fromHtml(
                question.question.questionHtml,
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
        }

        answerViews.clear()
        buildQuestionUI(inflater.context, optionsContainer, question)
        
        checkButton?.isEnabled = false
        checkButton?.setOnClickListener {
            isCorrect = checkAnswers()
            showFeedback(feedbackText, isCorrect)
            feedbackText.visibility = View.VISIBLE
            disableOptions()
            checkButton?.visibility = View.GONE
            continueButton.visibility = View.VISIBLE
        }

        continueButton.setOnClickListener {
            listener?.onQuizCompleted(question.id)
            dismiss()
        }

        builder.setView(view)
        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    private fun buildQuestionUI(
        context: Context,
        container: LinearLayout,
        question: NetworkVideoQuestion
    ) {
        val inflater = LayoutInflater.from(context)
        when (question.question.type) {
            "R" -> {
                val radioGroup = RadioGroup(context)
                radioGroup.orientation = LinearLayout.VERTICAL
                radioGroup.id = View.generateViewId()

                val radioButtons = mutableListOf<RadioButton>()

                question.question.answers?.forEach { answer ->
                    val optionView = inflater.inflate(R.layout.list_item_quiz_radio, radioGroup, false)
                    val radioButton = optionView.findViewById<RadioButton>(R.id.quiz_radio_button)
                    
                    radioButton.tag = answer
                    radioButton.text = HtmlCompat.fromHtml(
                        answer.textHtml,
                        HtmlCompat.FROM_HTML_MODE_LEGACY
                    )
                    
                    radioButton.setOnCheckedChangeListener { buttonView, isChecked ->
                        if (isChecked) {
                            radioButtons.forEach { rb ->
                                if (rb != buttonView) {
                                    rb.isChecked = false
                                }
                            }
                        }
                        checkButton?.isEnabled = radioButtons.any { it.isChecked }
                    }
                    
                    radioButtons.add(radioButton)
                    radioGroup.addView(optionView)
                    answerViews.add(optionView)
                }
                container.addView(radioGroup)
                answerViews.add(radioGroup)
            }
            "C" -> {
                question.question.answers?.forEach { answer ->
                    val optionView = inflater.inflate(R.layout.list_item_quiz_checkbox, container, false)
                    val checkBox = optionView.findViewById<CheckBox>(R.id.quiz_check_box)
                    
                    checkBox.tag = answer
                    checkBox.text = HtmlCompat.fromHtml(
                        answer.textHtml,
                        HtmlCompat.FROM_HTML_MODE_LEGACY
                    )
                    
                    checkBox.setOnCheckedChangeListener { _, _ ->
                        val hasSelection = answerViews.any { view ->
                            view.findViewById<CheckBox>(R.id.quiz_check_box)?.isChecked == true
                        }
                        checkButton?.isEnabled = hasSelection
                    }
                    
                    container.addView(optionView)
                    answerViews.add(optionView)
                }
            }
            "G" -> {
                val flexboxLayout = FlexboxLayout(context)
                flexboxLayout.flexWrap = FlexWrap.WRAP
                flexboxLayout.alignItems = com.google.android.flexbox.AlignItems.CENTER

                val html = question.question.questionHtml
                val cleanText = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY).toString().trim()

                val pattern = Pattern.compile("\\[(.*?)\\]")
                val matcher = pattern.matcher(cleanText)

                val gapFillEditTexts = mutableListOf<EditText>()
                var lastEnd = 0
                while (matcher.find()) {
                    val textBefore = cleanText.substring(lastEnd, matcher.start())
                    if (textBefore.isNotEmpty()) {
                        val textView = TextView(context)
                        textView.text = textBefore
                        textView.textSize = 18f
                        textView.setTextColor(ContextCompat.getColor(context, R.color.testpress_table_text))
                        flexboxLayout.addView(textView)
                    }

                    val editText = EditText(context)
                    editText.setSingleLine(true)
                    editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                    editText.minEms = 3
                    editText.setTextColor(ContextCompat.getColor(context, R.color.testpress_table_text))
                    editText.setBackgroundResource(R.drawable.quiz_gap_border_normal)
                    
                    gapFillEditTexts.add(editText)
                    editText.addTextChangedListener(object : android.text.TextWatcher {
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                        override fun afterTextChanged(s: android.text.Editable?) {
                            val allFilled = gapFillEditTexts.all { it.text.toString().trim().isNotEmpty() }
                            checkButton?.isEnabled = allFilled
                        }
                    })
                    
                    flexboxLayout.addView(editText)
                    answerViews.add(editText)

                    lastEnd = matcher.end()
                }

                val textAfter = cleanText.substring(lastEnd)
                if (textAfter.isNotEmpty()) {
                    val textView = TextView(context)
                    textView.text = textAfter
                    textView.textSize = 18f
                    textView.setTextColor(ContextCompat.getColor(context, R.color.testpress_table_text))
                    flexboxLayout.addView(textView)
                }

                container.addView(flexboxLayout)
            }
        }
    }

    private fun checkAnswers(): Boolean {
        selectedAnswerIds.clear()
        gapFillResults.clear()

        val correctAnswers = question.question.answers
            ?.filter { it.isCorrect }
            ?.map { it.id }
            ?.toSet() ?: emptySet()


        when (question.question.type) {
            "R" -> {
                answerViews.forEach { view ->
                    val radioButton = view.findViewById<RadioButton>(R.id.quiz_radio_button)
                    if (radioButton != null && radioButton.isChecked) {
                        val selectedAnswer = radioButton.tag as NetworkAnswer
                    selectedAnswerIds.add(selectedAnswer.id)
                    }
                }
            }
            "C" -> {
                answerViews.forEach { view ->
                    val checkBox = view.findViewById<CheckBox>(R.id.quiz_check_box)
                    if (checkBox != null && checkBox.isChecked) {
                        val selectedAnswer = checkBox.tag as NetworkAnswer
                        selectedAnswerIds.add(selectedAnswer.id)
                    }
                }
            }
            "G" -> {
                val key = question.question.answers?.map { it.textHtml.trim() } ?: emptyList()
                val userAnswers = answerViews.filterIsInstance<EditText>().map { it.text.toString().trim() }

                var allCorrect = true
                for (i in key.indices) {
                    val isBoxCorrect = key[i].equals(userAnswers.getOrNull(i), ignoreCase = true)
                    gapFillResults.add(isBoxCorrect)
                    if (!isBoxCorrect) {
                        allCorrect = false
                    }
                }
                return allCorrect
            }
        }

        val isMatch = correctAnswers.isNotEmpty() && correctAnswers == selectedAnswerIds.toSet()
        return isMatch
    }

    private fun disableOptions() {
        answerViews.forEach { view ->
            view.findViewById<RadioButton>(R.id.quiz_radio_button)?.isEnabled = false
            view.findViewById<CheckBox>(R.id.quiz_check_box)?.isEnabled = false
            if (view is EditText) {
            view.isEnabled = false
            }
            if (view is RadioGroup) {
                view.isEnabled = false
            }
            view.findViewById<LinearLayout>(R.id.option_root)?.isEnabled = false
        }
    }

    private fun showFeedback(feedbackText: TextView, isCorrect: Boolean) {
        val context = feedbackText.context
        if (isCorrect) {
            feedbackText.text = "Correct!"
            feedbackText.setTextColor(ContextCompat.getColor(context, R.color.testpress_green))
        } else {
            feedbackText.text = "Incorrect."
            feedbackText.setTextColor(ContextCompat.getColor(context, R.color.testpress_red_incorrect))
        }

        when (question.question.type) {
            "R", "C" -> {
                answerViews.forEach { view ->
                    val optionRoot = view.findViewById<LinearLayout>(R.id.option_root)
                    val icon = view.findViewById<ImageView>(R.id.quiz_icon)
                    
                    val button = view.findViewById<RadioButton>(R.id.quiz_radio_button)
                                 ?: view.findViewById<CheckBox>(R.id.quiz_check_box)
                    
                    if (button == null || optionRoot == null) return@forEach
                    
                    val answer = button.tag as? NetworkAnswer ?: return@forEach

                    if (answer.isCorrect) {
                        optionRoot.setBackgroundResource(R.drawable.quiz_option_border_correct)
                        icon.setImageResource(R.drawable.ic_quiz_check_green)
                        icon.visibility = View.VISIBLE
                    } else if (selectedAnswerIds.contains(answer.id)) {
                        optionRoot.setBackgroundResource(R.drawable.quiz_option_border_incorrect)
                        icon.setImageResource(R.drawable.ic_quiz_cancel_red)
                        icon.visibility = View.VISIBLE
                    }
                }
            }
            "G" -> {
                val editTexts = answerViews.filterIsInstance<EditText>()
                editTexts.forEachIndexed { index, editText ->
                    val isBoxCorrect = gapFillResults.getOrNull(index) ?: false
                    if (isBoxCorrect) {
                        editText.setBackgroundResource(R.drawable.quiz_gap_correct_border)
                    } else {
                        editText.setBackgroundResource(R.drawable.quiz_gap_incorrect_border)
                    }
                }

                val correctAnswerString = "Correct answer is: " +
                    (question.question.answers?.joinToString(" , ") { it.textHtml } ?: "")
                feedbackText.append("\n" + correctAnswerString)
            }
        }
    }
}
