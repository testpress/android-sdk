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
            preventBackButtonDismissal(it)
            configureBottomSheetBehavior(it)
            configureWindowForFullscreen(it)
        }
    }

    private fun preventBackButtonDismissal(dialog: android.app.Dialog) {
        dialog.setOnKeyListener { _, keyCode, event ->
            if (keyCode == android.view.KeyEvent.KEYCODE_BACK && event.action == android.view.KeyEvent.ACTION_UP) {
                true
            } else {
                false
            }
        }
    }

    private fun configureBottomSheetBehavior(dialog: android.app.Dialog) {
        val bottomSheet = dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
        if (bottomSheet != null) {
            val behavior = BottomSheetBehavior.from(bottomSheet)
            behavior.peekHeight = 0
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.isHideable = false
            behavior.isDraggable = false
            behavior.addBottomSheetCallback(createBottomSheetCallback(behavior))
        }
    }

    private fun createBottomSheetCallback(behavior: BottomSheetBehavior<FrameLayout>): BottomSheetBehavior.BottomSheetCallback {
        return object : BottomSheetBehavior.BottomSheetCallback() {
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
        }
    }

    private fun configureWindowForFullscreen(dialog: android.app.Dialog) {
        val window = dialog.window
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
                addQuestionTextView(context, questionContainer, question.question.questionHtml)
                setupRadioButtonQuestion(inflater, context, questionContainer, optionsContainer, question)
            }
            "C" -> {
                addQuestionTextView(context, questionContainer, question.question.questionHtml)
                setupCheckBoxQuestion(inflater, context, optionsContainer, question)
            }
            "G" -> {
                setupGapFillQuestion(context, questionContainer, question)
            }
        }
    }

    private fun addQuestionTextView(context: Context, questionContainer: FrameLayout, questionHtml: String) {
        val questionTextView = TextView(context).apply {
            val htmlText = HtmlCompat.fromHtml(questionHtml, HtmlCompat.FROM_HTML_MODE_LEGACY)
            text = htmlText.subSequence(0, TextUtils.getTrimmedLength(htmlText))
            setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.testpress_text_size_large))
            setTypeface(null, Typeface.BOLD)
            setTextColor(ContextCompat.getColor(context, R.color.testpress_black))
        }
        questionContainer.addView(questionTextView)
    }

    private fun setupRadioButtonQuestion(
        inflater: LayoutInflater,
        context: Context,
        questionContainer: FrameLayout,
        optionsContainer: LinearLayout,
        question: NetworkVideoQuestion
    ) {
        radioGroup = createRadioGroup(context)
        val radioButtons = mutableListOf<RadioButton>()
        val isUpdatingFlag = booleanArrayOf(false)

        question.question.answers?.forEach { answer ->
            val optionView = inflater.inflate(R.layout.list_item_question_option, radioGroup, false)
            val choiceContainer = optionView.findViewById<FrameLayout>(R.id.choice_container)
            
            val radioButton = createRadioButton(context, answer, radioButtons, isUpdatingFlag)
            
            radioButtons.add(radioButton)
            choiceContainer.addView(radioButton)
            optionView.tag = radioButton
            radioGroup!!.addView(optionView)
            answerViews.add(optionView)
        }
        optionsContainer.addView(radioGroup)
    }

    private fun createRadioGroup(context: Context): RadioGroup {
        return RadioGroup(context).apply {
            orientation = LinearLayout.VERTICAL
            id = View.generateViewId()
        }
    }

    private fun createRadioButton(
        context: Context,
        answer: NetworkAnswer,
        radioButtons: MutableList<RadioButton>,
        isUpdatingFlag: BooleanArray
    ): MaterialRadioButton {
        val radioButton = MaterialRadioButton(context).apply {
            text = HtmlCompat.fromHtml(answer.textHtml, HtmlCompat.FROM_HTML_MODE_LEGACY)
            tag = answer
            id = View.generateViewId()
            setTextColor(ContextCompat.getColor(context, R.color.testpress_table_text))
            setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.testpress_text_size_xmedium))
        }
        
        radioButton.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isUpdatingFlag[0]) return@setOnCheckedChangeListener
            
            if (isChecked) {
                isUpdatingFlag[0] = true
                radioButtons.forEach { rb ->
                    if (rb != buttonView && rb.isChecked) {
                        rb.isChecked = false
                    }
                }
                isUpdatingFlag[0] = false
                actionButton?.isEnabled = true
            }
        }
        
        return radioButton
    }

    private fun setupCheckBoxQuestion(
        inflater: LayoutInflater,
        context: Context,
        optionsContainer: LinearLayout,
        question: NetworkVideoQuestion
    ) {
        question.question.answers?.forEach { answer ->
            val optionView = inflater.inflate(R.layout.list_item_question_option, optionsContainer, false)
            val choiceContainer = optionView.findViewById<FrameLayout>(R.id.choice_container)

            val checkBox = createCheckBox(context, answer)

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

    private fun createCheckBox(context: Context, answer: NetworkAnswer): MaterialCheckBox {
        return MaterialCheckBox(context).apply {
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
    }

    private fun setupGapFillQuestion(
        context: Context,
        questionContainer: FrameLayout,
        question: NetworkVideoQuestion
    ) {
        val flexboxLayout = createFlexboxLayout(context)
        val cleanText = HtmlCompat.fromHtml(question.question.questionHtml, HtmlCompat.FROM_HTML_MODE_LEGACY).toString().trim()
        val gapFillEditTexts = mutableListOf<EditText>()
        
        createGapFillViews(cleanText, flexboxLayout, gapFillEditTexts, context)
        
        questionContainer.addView(flexboxLayout)
    }

    private fun createFlexboxLayout(context: Context): FlexboxLayout {
        return FlexboxLayout(context).apply {
            flexWrap = FlexWrap.WRAP
            alignItems = AlignItems.CENTER
        }
    }

    private fun createGapFillViews(
        cleanText: String,
        flexboxLayout: FlexboxLayout,
        gapFillEditTexts: MutableList<EditText>,
        context: Context
    ) {
        val pattern = Pattern.compile("\\[(.*?)\\]")
        val matcher = pattern.matcher(cleanText)
        var lastEnd = 0

        while (matcher.find()) {
            val textBefore = cleanText.substring(lastEnd, matcher.start())
            if (textBefore.isNotEmpty()) {
                val textView = createGapFillTextView(context, textBefore)
                flexboxLayout.addView(textView)
            }

            val editText = createGapFillEditText(context, gapFillEditTexts)
            gapFillEditTexts.add(editText)
            
            val layoutParams = createGapFillLayoutParams(context)
            flexboxLayout.addView(editText, layoutParams)
            answerViews.add(editText)
            lastEnd = matcher.end()
        }

        val textAfter = cleanText.substring(lastEnd)
        if (textAfter.isNotEmpty()) {
            val textView = createGapFillTextView(context, textAfter)
            flexboxLayout.addView(textView)
        }
    }

    private fun createGapFillTextView(context: Context, text: String): TextView {
        return TextView(context).apply {
            this.text = text
            setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.testpress_text_size_large))
            setTextColor(ContextCompat.getColor(context, R.color.testpress_black))
        }
    }

    private fun createGapFillEditText(context: Context, gapFillEditTexts: MutableList<EditText>): EditText {
        val editText = EditText(context).apply {
            minEms = 4
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            setTextColor(ContextCompat.getColor(context, R.color.testpress_table_text))
            background = ContextCompat.getDrawable(context, R.drawable.testpress_gray_border)
            val padding = resources.getDimensionPixelSize(R.dimen.testpress_horizontal_margin)
            setPadding(padding, padding, padding, padding)
        }
        
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val allFilled = gapFillEditTexts.all { it.text.toString().trim().isNotEmpty() }
                actionButton?.isEnabled = allFilled
            }
        })
        
        return editText
    }

    private fun createGapFillLayoutParams(context: Context): LayoutParams {
        return LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = resources.getDimensionPixelSize(R.dimen.testpress_horizontal_margin)
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
            "R" -> extractSelectedRadioButtonAnswers()
            "C" -> extractSelectedCheckBoxAnswers()
            "G" -> return checkGapFillAnswers()
        }
        return correctAnswers.isNotEmpty() && correctAnswers == selectedAnswerIds.toSet()
    }

    private fun extractSelectedRadioButtonAnswers() {
        answerViews.forEach { view ->
            val radioButton = view.tag as? RadioButton
            if (radioButton != null && radioButton.isChecked) {
                (radioButton.tag as? NetworkAnswer)?.let { selectedAnswer ->
                    selectedAnswerIds.add(selectedAnswer.id)
                }
            }
        }
    }

    private fun extractSelectedCheckBoxAnswers() {
        answerViews.forEach { view ->
            val checkBox = view.tag as? CheckBox
            if (checkBox != null && checkBox.isChecked) {
                (checkBox.tag as? NetworkAnswer)?.let { selectedAnswer ->
                    selectedAnswerIds.add(selectedAnswer.id)
                }
            }
        }
    }

    private fun checkGapFillAnswers(): Boolean {
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
            "R", "C" -> showRadioButtonCheckBoxFeedback(context)
            "G" -> showGapFillFeedback(context, isCorrect)
        }
    }

    private fun showRadioButtonCheckBoxFeedback(context: Context) {
        feedbackText?.visibility = View.GONE
        
        answerViews.forEach { view ->
            showOptionFeedback(view, context)
        }
    }

    private fun showOptionFeedback(view: View, context: Context) {
        val optionRoot = view.findViewById<LinearLayout>(R.id.option_root)
        val icon = view.findViewById<ImageView>(R.id.question_icon)
        val button = view.tag as? CompoundButton
        
        if (button == null || optionRoot == null) return
        
        val answer = button.tag as? NetworkAnswer ?: return
        val isSelected = button.isChecked

        if (answer.isCorrect) {
            showCorrectOptionFeedback(optionRoot, icon, context)
        } else if (isSelected) {
            showIncorrectOptionFeedback(optionRoot, icon, context)
        } else {
            icon.visibility = View.INVISIBLE
        }
    }

    private fun showCorrectOptionFeedback(
        optionRoot: LinearLayout,
        icon: ImageView,
        context: Context
    ) {
        optionRoot.setBackgroundResource(R.drawable.question_border_correct)
        icon.setImageResource(R.drawable.ic_baseline_done_24)
        icon.setColorFilter(ContextCompat.getColor(context, R.color.testpress_green))
        icon.visibility = View.VISIBLE
    }

    private fun showIncorrectOptionFeedback(
        optionRoot: LinearLayout,
        icon: ImageView,
        context: Context
    ) {
        optionRoot.setBackgroundResource(R.drawable.question_border_incorrect)
        icon.setImageResource(R.drawable.ic_baseline_error_24)
        icon.setColorFilter(ContextCompat.getColor(context, R.color.testpress_red_incorrect))
        icon.visibility = View.VISIBLE
    }

    private fun showGapFillFeedback(context: Context, isCorrect: Boolean) {
        highlightGapFillEditTexts()
        showGapFillFeedbackText(context, isCorrect)
    }

    private fun highlightGapFillEditTexts() {
        val editTexts = answerViews.filterIsInstance<EditText>()
        editTexts.forEachIndexed { index, editText ->
            val isBoxCorrect = gapFillResults.getOrNull(index) ?: false
            
            if (isBoxCorrect) {
                editText.setBackgroundResource(R.drawable.question_border_correct)
            } else {
                editText.setBackgroundResource(R.drawable.question_border_incorrect)
            }
        }
    }

    private fun showGapFillFeedbackText(context: Context, isCorrect: Boolean) {
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