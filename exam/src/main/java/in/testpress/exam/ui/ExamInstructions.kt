package `in`.testpress.exam.ui

import `in`.testpress.exam.R
import `in`.testpress.ui.BaseFragment
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    private lateinit var instructionsView: TextView;
    private lateinit var toolbarTitle: TextView;
    private lateinit var confirmButton: Button

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

        val examTitle = requireArguments().getString(TITLE_FLAG)
        toolbarTitle.text = examTitle;
    }

    private fun bindViews(view: View){
        instructionsView = view.findViewById(R.id.instructions_text)
        confirmButton = view.findViewById(R.id.confirm_button)
        toolbarTitle = view.findViewById(R.id.exam_name)
    }

    private fun displayInstructions(){
        val instructions = requireArguments().getString(INSTRUCTIONFLAG)
        instructionsView.text = Html.fromHtml(instructions.toString())
    }

    private fun setListeners(){
        confirmButton.setOnClickListener {
            startExam()
        }
    }
}