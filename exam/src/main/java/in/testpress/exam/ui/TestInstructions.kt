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


class TestInstructions : BaseFragment() {

    companion object {
        public val INSTRUCTIONFLAG = "exam_instructions"
    }

    private lateinit var instructionsView: TextView;
    private lateinit var confirmButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_test_instructions, container, false)
        initViews(view)

        return view
    }

    private fun initViews(view: View){
        instructionsView = view.findViewById(R.id.instructions_text)
        val instructions = requireArguments().getString(INSTRUCTIONFLAG)
        instructionsView.text = Html.fromHtml(instructions.toString())

        confirmButton = view.findViewById(R.id.confirm_button)
        confirmButton.setOnClickListener {
            startTest()
        }
    }

    private fun startTest(){
        val testFragment = TestFragment()
        testFragment.arguments = arguments
        parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, testFragment).commitAllowingStateLoss()
    }
}
