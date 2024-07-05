package `in`.testpress.course.ui

import `in`.testpress.course.databinding.OfflineExamListActivityBinding
import `in`.testpress.course.databinding.OfflineExamListItemBinding
import `in`.testpress.course.viewmodels.OfflineExamViewModel
import `in`.testpress.database.entities.OfflineExam
import `in`.testpress.ui.BaseToolBarActivity
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView


class OfflineExamListActivity : BaseToolBarActivity() {

    private lateinit var binding: OfflineExamListActivityBinding
    private lateinit var offlineExamViewModel: OfflineExamViewModel
    private lateinit var offlineExamAdapter: OfflineExamAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = OfflineExamListActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setActionBarTitle("Offline Exam")
        initializeViewModel()
        initializeListAdapter()
        initializeListView()
        syncExamsModifiedDates()
    }

    private fun initializeViewModel() {
        offlineExamViewModel = OfflineExamViewModel.initializeViewModel(this)
    }

    private fun initializeListAdapter() {
        offlineExamAdapter = OfflineExamAdapter()
        binding.recyclerView.adapter = offlineExamAdapter
    }

    private fun initializeListView() {
        offlineExamViewModel.getAll().observe(this) { exams ->
            offlineExamAdapter.submitList(exams)
            binding.recyclerView.visibility = if (exams.isEmpty()) View.GONE else View.VISIBLE
            binding.noDataLayout.visibility = if (exams.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun syncExamsModifiedDates(){
        offlineExamViewModel.syncExamsModifiedDates()
    }

    inner class OfflineExamAdapter :
        ListAdapter<OfflineExam, OfflineExamAdapter.ExamViewHolder>(EXAM_COMPARATOR) {

        inner class ExamViewHolder(private val binding: OfflineExamListItemBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(exam: OfflineExam) {
                binding.titleTextView.text = exam.title
                binding.deleteButton.setOnClickListener {
                    offlineExamViewModel.deleteOfflineExam(exam.id!!)
                }

                binding.syncButton.setOnClickListener {
                    offlineExamViewModel.syncExam(exam)
                }

                binding.openExamDetail.setOnClickListener {
                    startActivity(
                        ContentActivity.createIntent(
                            exam.contentId,
                            this@OfflineExamListActivity,
                            ""
                        )
                    )
                }
                if (exam.downloadedQuestionCount.toInt() == exam.numberOfQuestions){
                    binding.syncButton.isVisible = exam.isSyncRequired
                    binding.openExamDetail.isVisible = !exam.isSyncRequired
                    binding.deleteButton.isVisible = true
                    binding.downloadingButton.isVisible = false
                } else {
                    binding.syncButton.isVisible = false
                    binding.openExamDetail.isVisible = false
                    binding.deleteButton.isVisible = false
                    binding.downloadingButton.isVisible = true
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExamViewHolder {
            val binding =
                OfflineExamListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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