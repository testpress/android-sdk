package `in`.testpress.exam.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import `in`.testpress.exam.databinding.ExamContentListItemBinding
import `in`.testpress.exam.databinding.ReportQuestionFragmentBinding

class ReportQuestionFragment : Fragment() {

    private var questionId: Long = -1
    private lateinit var binding: ReportQuestionFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parseArguments()
    }

    private fun parseArguments() {
        questionId = arguments!!.getString("question_id")?.toLong()!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ReportQuestionFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }



}