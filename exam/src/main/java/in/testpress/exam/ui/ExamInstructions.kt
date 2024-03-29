package `in`.testpress.exam.ui

import `in`.testpress.exam.R
import `in`.testpress.ui.BaseFragment
import `in`.testpress.util.WebViewUtils
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.Toolbar


class ExamInstructions(val startExam: () -> Unit) : BaseFragment() {

    companion object {
        val INSTRUCTIONFLAG = "exam_instructions"
        val TITLE_FLAG = "exam_title"

        fun createInstance(examInstructions: String, examTitle: String, startExam: () -> Unit): ExamInstructions{
            val instructions = ExamInstructions{
                startExam()
            };

            val instructionArguments = Bundle();
            instructionArguments.putString(INSTRUCTIONFLAG, examInstructions);
            instructionArguments.putString(TITLE_FLAG, examTitle);
            instructions.setArguments(instructionArguments);
            return instructions
        }
    }

    private lateinit var instructionsView: WebView;
    private lateinit var confirmButton: Button
    lateinit var webViewUtils: WebViewUtils

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_test_instructions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindViews(view)
        setToolBar(view)
        displayInstructions()
        setListeners()
    }

    private fun setToolBar(view: View){
        val toolbar: Toolbar = view.findViewById(R.id.toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        toolbar.setNavigationOnClickListener(View.OnClickListener { requireActivity().onBackPressed() })
    }

    private fun bindViews(view: View){
        instructionsView = view.findViewById(R.id.instructions_text)
        confirmButton = view.findViewById(R.id.confirm_button)
    }

    private fun displayInstructions(){
        val instructions = requireArguments().getString(INSTRUCTIONFLAG)
        WebViewUtils(instructionsView).initWebView(instructions, activity)
    }

    private fun setListeners(){
        confirmButton.setOnClickListener {
            startExam()
        }
    }
}