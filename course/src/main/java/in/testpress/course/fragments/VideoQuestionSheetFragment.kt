package `in`.testpress.course.fragments

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextUtils
import android.text.TextWatcher
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayout
import com.google.android.flexbox.FlexboxLayout.LayoutParams
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.gson.Gson
import `in`.testpress.course.R
import `in`.testpress.course.network.NetworkAnswer
import `in`.testpress.course.network.NetworkVideoQuestion
import java.util.regex.Pattern

class VideoQuestionSheetFragment : BottomSheetDialogFragment() {

    private lateinit var question: NetworkVideoQuestion
    private var listener: OnQuestionCompleteListener? = null

    private var answerViews = mutableListOf<View>()
    private var actionButton: TextView? = null
    private var feedbackText: TextView? = null
    private var radioGroup: RadioGroup? = null

    private var selectedAnswerIds = mutableListOf<Long>()
    private var isCorrect = false
    private var gapFillResults = mutableListOf<Boolean>()
    private var isAnswerChecked = false

    interface OnQuestionCompleteListener {
        fun onQuestionCompleted(questionId: Long)
    }

    companion object {
        private const val ARG_QUESTION = "ARG_QUESTION"
        fun newInstance(question: NetworkVideoQuestion): VideoQuestionSheetFragment {
            val fragment = VideoQuestionSheetFragment()
            val args = Bundle()
            args.putString(ARG_QUESTION, Gson().toJson(question))
            fragment.arguments = args
            return fragment
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (parentFragment is OnQuestionCompleteListener) {
            listener = parentFragment as OnQuestionCompleteListener
        } else {
            throw RuntimeException("$parentFragment must implement OnQuestionCompleteListener")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        question = Gson().fromJson(
            arguments?.getString(ARG_QUESTION),
            NetworkVideoQuestion::class.java
        )
        isCancelable = false
        setStyle(STYLE_NORMAL, com.google.android.material.R.style.Theme_MaterialComponents_BottomSheetDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_video_question_sheet, container, false)
    }
    
    override fun onStart() {
        super.onStart()
        dialog?.let {
            it.setOnKeyListener { _, keyCode, event ->
                if (keyCode == android.view.KeyEvent.KEYCODE_BACK && event.action == android.view.KeyEvent.ACTION_UP) {
                    true
                } else {
                    false
                }
            }
            
            val bottomSheet = it.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            if (bottomSheet != null) {
                val behavior = BottomSheetBehavior.from(bottomSheet)
                behavior.peekHeight = 0
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.isHideable = false
                behavior.isDraggable = false
                behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        if (newState != BottomSheetBehavior.STATE_EXPANDED) {
                            behavior.state = BottomSheetBehavior.STATE_EXPANDED
                        }
                    }
                    override fun onSlide(bottomSheet: View, slideOffset: Float) {
                        // Prevent sliding - keep it at expanded position
                        if (slideOffset < 0f) {
                            behavior.state = BottomSheetBehavior.STATE_EXPANDED
                        }
                    }
                })
            }
            
            // Fullscreen support: Ensure dialog appears above fullscreen video dialogs
            val window = it.window
            window?.let { w ->
                w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                w.setFlags(
                    android.view.WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    android.view.WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                )
                w.clearFlags(android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
                w.clearFlags(android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }
        }
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val questionContainer: FrameLayout = view.findViewById(R.id.question_container)
        val optionsContainer: LinearLayout = view.findViewById(R.id.question_options_container)
        actionButton = view.findViewById(R.id.question_action_button)
        feedbackText = view.findViewById(R.id.question_feedback_text)

        answerViews.clear()
        buildQuestionUI(layoutInflater, questionContainer, optionsContainer, question)

        actionButton?.setOnClickListener {
            if (!isAnswerChecked) {
                isCorrect = checkAnswers()
                showFeedback(isCorrect)
                disableOptions()
                actionButton?.text = getString(R.string.question_button_continue)
                isAnswerChecked = true
            } else {
                listener?.onQuestionCompleted(question.id)
                dismiss()
            }
        }
    }

    private fun buildQuestionUI(
        inflater: LayoutInflater,
        questionContainer: FrameLayout,
        optionsContainer: LinearLayout,
        question: NetworkVideoQuestion
    ) {
        val context = inflater.context
        when (question.question.type) {
            "R" -> {
                val questionTextView = TextView(context).apply {
                    val htmlText = HtmlCompat.fromHtml(question.question.questionHtml, HtmlCompat.FROM_HTML_MODE_LEGACY)
                    text = htmlText.subSequence(0, TextUtils.getTrimmedLength(htmlText))
                    setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.testpress_text_size_large))
                    setTypeface(null, Typeface.BOLD)
                    setTextColor(ContextCompat.getColor(context, R.color.testpress_black))
                }
                questionContainer.addView(questionTextView)
                
                radioGroup = RadioGroup(context).apply {
                    orientation = LinearLayout.VERTICAL
                    id = View.generateViewId()
                }

                val radioButtons = mutableListOf<RadioButton>()
                var isUpdatingRadioButtons = false

                question.question.answers?.forEach { answer ->
                    val optionView = inflater.inflate(R.layout.list_item_question_option, radioGroup, false)
                    val choiceContainer = optionView.findViewById<FrameLayout>(R.id.choice_container)
                    
                    val radioButton = MaterialRadioButton(context).apply {
                        text = HtmlCompat.fromHtml(answer.textHtml, HtmlCompat.FROM_HTML_MODE_LEGACY)
                        tag = answer
                        id = View.generateViewId()
                        setTextColor(ContextCompat.getColor(context, R.color.testpress_table_text))
                        setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.testpress_text_size_xmedium))
                    }
                    
                    radioButton.setOnCheckedChangeListener { buttonView, isChecked ->
                        if (isUpdatingRadioButtons) return@setOnCheckedChangeListener
                        
                        if (isChecked) {
                            isUpdatingRadioButtons = true
                            radioButtons.forEach { rb ->
                                if (rb != buttonView && rb.isChecked) {
                                    rb.isChecked = false
                                }
                            }
                            isUpdatingRadioButtons = false
                            actionButton?.isEnabled = true
                        }
                    }
                    
                    radioButtons.add(radioButton)
                    choiceContainer.addView(radioButton)
                    optionView.tag = radioButton
                    radioGroup!!.addView(optionView)
                    answerViews.add(optionView)
                }
                optionsContainer.addView(radioGroup)
            }
            "C" -> {
                val questionTextView = TextView(context).apply {
                    val htmlText = HtmlCompat.fromHtml(question.question.questionHtml, HtmlCompat.FROM_HTML_MODE_LEGACY)
                    text = htmlText.subSequence(0, TextUtils.getTrimmedLength(htmlText))
                    setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.testpress_text_size_large))
                    setTypeface(null, Typeface.BOLD)
                    setTextColor(ContextCompat.getColor(context, R.color.testpress_black))
                }
                questionContainer.addView(questionTextView)

                question.question.answers?.forEach { answer ->
                    val optionView = inflater.inflate(R.layout.list_item_question_option, optionsContainer, false)
                    val choiceContainer = optionView.findViewById<FrameLayout>(R.id.choice_container)

                    val checkBox = MaterialCheckBox(context).apply {
                        text = HtmlCompat.fromHtml(answer.textHtml, HtmlCompat.FROM_HTML_MODE_LEGACY)
                        tag = answer
                        id = View.generateViewId()
                        setTextColor(ContextCompat.getColor(context, R.color.testpress_table_text))
                        setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.testpress_text_size_xmedium))
                        val states = arrayOf(
                            intArrayOf(android.R.attr.state_checked),
                            intArrayOf(-android.R.attr.state_checked)
                        )
                        val colors = intArrayOf(
                            ContextCompat.getColor(context, R.color.testpress_green),
                            ContextCompat.getColor(context, android.R.color.darker_gray)
                        )
                        this.buttonTintList = ColorStateList(states, colors)
                    }

                    checkBox.setOnCheckedChangeListener { _, _ ->
                        val hasSelection = answerViews.any { view ->
                            (view.tag as? CheckBox)?.isChecked == true
                        }
                        actionButton?.isEnabled = hasSelection
                    }
                    
                    choiceContainer.addView(checkBox)
                    optionView.tag = checkBox
                    optionsContainer.addView(optionView)
                    answerViews.add(optionView)
                }
            }
            "G" -> {
                val flexboxLayout = FlexboxLayout(context)
                flexboxLayout.flexWrap = FlexWrap.WRAP
                flexboxLayout.alignItems = AlignItems.CENTER

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
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.testpress_text_size_large))
                        textView.setTextColor(ContextCompat.getColor(context, R.color.testpress_black))
                        flexboxLayout.addView(textView)
                    }

                    val editText = EditText(context).apply {
                        minEms = 4
                        inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                        setTextColor(ContextCompat.getColor(context, R.color.testpress_table_text))
                        background = ContextCompat.getDrawable(context, R.drawable.testpress_gray_border)
                        val padding = resources.getDimensionPixelSize(R.dimen.testpress_horizontal_margin)
                        setPadding(padding, padding, padding, padding)
                    }
                    
                    gapFillEditTexts.add(editText)
                    editText.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                        override fun afterTextChanged(s: Editable?) {
                            val allFilled = gapFillEditTexts.all { it.text.toString().trim().isNotEmpty() }
                            actionButton?.isEnabled = allFilled
                        }
                    })
                    
                    val layoutParams = LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        topMargin = resources.getDimensionPixelSize(R.dimen.testpress_horizontal_margin)
                    }
                    flexboxLayout.addView(editText, layoutParams)
                    answerViews.add(editText)
                    lastEnd = matcher.end()
                }

                val textAfter = cleanText.substring(lastEnd)
                if (textAfter.isNotEmpty()) {
                    val textView = TextView(context)
                    textView.text = textAfter
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.testpress_text_size_large))
                    textView.setTextColor(ContextCompat.getColor(context, R.color.testpress_black))
                    flexboxLayout.addView(textView)
                }
                questionContainer.addView(flexboxLayout)
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
                    val radioButton = view.tag as? RadioButton
                    if (radioButton != null && radioButton.isChecked) {
                        (radioButton.tag as? NetworkAnswer)?.let { selectedAnswer ->
                            selectedAnswerIds.add(selectedAnswer.id)
                        }
                    }
                }
            }
            "C" -> {
                answerViews.forEach { view ->
                    val checkBox = view.tag as? CheckBox
                    if (checkBox != null && checkBox.isChecked) {
                        (checkBox.tag as? NetworkAnswer)?.let { selectedAnswer ->
                            selectedAnswerIds.add(selectedAnswer.id)
                        }
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
        return correctAnswers.isNotEmpty() && correctAnswers == selectedAnswerIds.toSet()
    }

    private fun disableOptions() {
        answerViews.forEach { view ->
            if (view is EditText) {
                view.isEnabled = false
            } else {
                val button = view.tag as? CompoundButton
                button?.isEnabled = false
            }
            view.findViewById<LinearLayout>(R.id.option_root)?.isEnabled = false
        }
        radioGroup?.isEnabled = false
    }

    private fun showFeedback(isCorrect: Boolean) {
        val context = context ?: return
        
        when (question.question.type) {
            "R", "C" -> {
                feedbackText?.visibility = View.GONE
                
                answerViews.forEach { view ->
                    val optionRoot = view.findViewById<LinearLayout>(R.id.option_root)
                    val icon = view.findViewById<ImageView>(R.id.question_icon)
                    val button = view.tag as? CompoundButton
                    
                    if (button == null || optionRoot == null) return@forEach
                    
                    val answer = button.tag as? NetworkAnswer ?: return@forEach
                    
                    val isSelected = button.isChecked

                    if (answer.isCorrect) {
                        optionRoot.setBackgroundResource(R.drawable.question_border_correct)
                        icon.setImageResource(R.drawable.ic_baseline_done_24)
                        icon.setColorFilter(ContextCompat.getColor(context, R.color.testpress_green))
                        icon.visibility = View.VISIBLE
                    } 
                    else if (isSelected) {
                        optionRoot.setBackgroundResource(R.drawable.question_border_incorrect)
                        icon.setImageResource(R.drawable.ic_baseline_error_24)
                        icon.setColorFilter(ContextCompat.getColor(context, R.color.testpress_red_incorrect))
                        icon.visibility = View.VISIBLE
                    } else {
                        icon.visibility = View.INVISIBLE
                    }
                }
            }
            "G" -> {
                val editTexts = answerViews.filterIsInstance<EditText>()
                editTexts.forEachIndexed { index, editText ->
                    val isBoxCorrect = gapFillResults.getOrNull(index) ?: false
                    
                    if (isBoxCorrect) {
                        editText.setBackgroundResource(R.drawable.question_border_correct)
                    } else {
                        editText.setBackgroundResource(R.drawable.question_border_incorrect)
                    }
                }
                
                if (!isCorrect) {
                    feedbackText?.visibility = View.VISIBLE
                    val correctAnswers = question.question.answers?.joinToString(", ") { it.textHtml } ?: ""
                    feedbackText?.text = getString(R.string.question_correct_answer, correctAnswers)
                    feedbackText?.setTextColor(ContextCompat.getColor(context, R.color.testpress_red_incorrect))
                } else {
                    feedbackText?.visibility = View.GONE
                }
            }
        }
    }
}