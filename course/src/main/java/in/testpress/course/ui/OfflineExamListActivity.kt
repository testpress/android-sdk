package `in`.testpress.course.ui

import `in`.testpress.course.repository.OfflineExamRepository
import `in`.testpress.course.viewmodels.OfflineExamViewModel
import `in`.testpress.database.entities.OfflineExam
import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import `in`.testpress.course.databinding.ActivityOfflineExamListBinding
import `in`.testpress.course.databinding.ItemOfflineExamBinding
import `in`.testpress.ui.BaseToolBarActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class OfflineExamListActivity : BaseToolBarActivity() {

    private lateinit var binding: ActivityOfflineExamListBinding
    private lateinit var offlineExamViewModel: OfflineExamViewModel
    private lateinit var offlineExamAdapter: OfflineExamAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOfflineExamListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initializeViewModel()
        setActionBarTitle("Offline Exam")

        offlineExamAdapter = OfflineExamAdapter()
        binding.recyclerView.adapter = offlineExamAdapter

        offlineExamViewModel.getAllOfflineExams().observe(this) { exams ->
            offlineExamAdapter.submitList(exams)
            binding.recyclerView.visibility = if (exams.isEmpty()) View.GONE else View.VISIBLE
            binding.noDataLayout.visibility = if (exams.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun initializeViewModel() {
        offlineExamViewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return OfflineExamViewModel(
                    OfflineExamRepository(this@OfflineExamListActivity)
                ) as T
            }
        })[OfflineExamViewModel::class.java]
    }

    inner class OfflineExamAdapter :
        ListAdapter<OfflineExam, OfflineExamAdapter.ExamViewHolder>(EXAM_COMPARATOR) {

        inner class ExamViewHolder(private val binding: ItemOfflineExamBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(exam: OfflineExam) {
                binding.titleTextView.text = "${exam.title} Qs"
                binding.totalTime.text = exam.duration
                binding.numberOfQuestions.text = exam.numberOfQuestions.toString()
                binding.deleteButton.setOnClickListener {
                    offlineExamViewModel.deleteExam(exam.id!!)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExamViewHolder {
            val binding =
                ItemOfflineExamBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ExamViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ExamViewHolder, position: Int) {
            val exam = getItem(position)
            holder.bind(exam)
        }
    }

    companion object {
        private val EXAM_COMPARATOR = object : DiffUtil.ItemCallback<OfflineExam>() {
            override fun areItemsTheSame(oldItem: OfflineExam, newItem: OfflineExam): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: OfflineExam, newItem: OfflineExam): Boolean {
                return oldItem == newItem
            }
        }
    }
}
