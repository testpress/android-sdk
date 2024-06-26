package `in`.testpress.samples.course

import `in`.testpress.course.repository.OfflineExamRepository
import `in`.testpress.course.viewmodels.OfflineExamViewModel
import `in`.testpress.database.entities.OfflineExam
import `in`.testpress.enums.Status
import `in`.testpress.samples.databinding.ActivityOfflineExamSampleBinding
import `in`.testpress.samples.databinding.SampleItemOfflineExamBinding
import `in`.testpress.ui.BaseToolBarActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class OfflineExamSampleActivity: BaseToolBarActivity() {

    private lateinit var binding: ActivityOfflineExamSampleBinding
    private lateinit var offlineExamViewModel: OfflineExamViewModel
    private lateinit var offlineExamAdapter: OfflineExamAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOfflineExamSampleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setActionBarTitle("Offline Exam")
        initializeViewModel()
        initializeListAdapter()
        initializeListView()
        initializeOnClickListener()
        observeDownloadExamResult()
        syncExamsModifiedDates()
    }

    private fun initializeViewModel() {
        offlineExamViewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return OfflineExamViewModel(
                    OfflineExamRepository(this@OfflineExamSampleActivity)
                ) as T
            }
        })[OfflineExamViewModel::class.java]
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

    private fun initializeOnClickListener() {
        binding.downloads.setOnClickListener {
            if (binding.editText.text.isNullOrEmpty()) {
                Toast.makeText(this, "Content Id required to download the exam", Toast.LENGTH_SHORT)
                    .show()
            } else {
                offlineExamViewModel.downloadExam(binding.editText.text.toString().toLong())
            }
        }
    }

    private fun observeDownloadExamResult() {
        offlineExamViewModel.downloadExamResult.observe(this){ result ->
            when(result.status){
                Status.SUCCESS -> {
                    binding.downloads.isVisible = true
                    binding.loadingProgressBar.isVisible = false
                    Toast.makeText(this,"Download completed",Toast.LENGTH_SHORT).show()
                }
                Status.LOADING -> {
                    binding.loadingProgressBar.isVisible = true
                    binding.downloads.isVisible = false
                }
                Status.ERROR -> {
                    binding.downloads.isVisible = true
                    binding.loadingProgressBar.isVisible = false
                    Toast.makeText(this,"Download Failed",Toast.LENGTH_SHORT).show()
                }
            }

        }
    }

    private fun syncExamsModifiedDates(){
        offlineExamViewModel.syncExamsModifiedDates()
    }

    inner class OfflineExamAdapter :
        ListAdapter<OfflineExam, OfflineExamAdapter.ExamViewHolder>(EXAM_COMPARATOR) {

        inner class ExamViewHolder(private val binding: SampleItemOfflineExamBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(exam: OfflineExam) {
                binding.titleTextView.text = exam.title
                binding.deleteButton.setOnClickListener {
                    offlineExamViewModel.deleteOfflineExam(exam.id!!)
                }
                binding.syncButton.visibility = if (exam.isSyncRequired) View.VISIBLE else View.GONE
                binding.syncButton.setOnClickListener {
                    offlineExamViewModel.syncExam(exam)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExamViewHolder {
            val binding =
                SampleItemOfflineExamBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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