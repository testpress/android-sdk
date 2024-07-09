package `in`.testpress.course.ui

import `in`.testpress.core.TestpressSDKDatabase
import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.databinding.OfflineExamListActivityBinding
import `in`.testpress.course.databinding.OfflineExamListItemBinding
import `in`.testpress.course.viewmodels.OfflineExamViewModel
import `in`.testpress.database.entities.OfflineExam
import `in`.testpress.database.mapping.asGreenDaoModel
import `in`.testpress.database.mapping.asGreenDoaModels
import `in`.testpress.database.mapping.createGreenDoaModel
import `in`.testpress.exam.TestpressExam
import `in`.testpress.models.greendao.Attempt
import `in`.testpress.models.greendao.Content
import `in`.testpress.models.greendao.ContentDao
import `in`.testpress.ui.BaseToolBarActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


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
        syncCompletedAttempts()
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

    private fun syncCompletedAttempts() {
        offlineExamViewModel.syncCompletedAttemptToBackEnd()
    }

    inner class OfflineExamAdapter :
        ListAdapter<OfflineExam, OfflineExamAdapter.ExamViewHolder>(EXAM_COMPARATOR) {

        inner class ExamViewHolder(private val binding: OfflineExamListItemBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(exam: OfflineExam) {
                updateExamDetails(exam)
                deleteOnClickListener(exam)
                syncOnClickListener(exam)
                startExamOnClickListener(exam)
                resumeExamOnClickListener(exam)
                updateButtonVisibility(exam)
            }

            private fun updateExamDetails(exam: OfflineExam) {
                binding.titleTextView.text = exam.title
                binding.duration.text = exam.duration
                binding.numberOfQuestions.text = exam.numberOfQuestions.toString()
            }

            private fun deleteOnClickListener(exam: OfflineExam) {
                binding.deleteButton.setOnClickListener {
                    offlineExamViewModel.deleteOfflineExam(exam.id!!)
                }
            }

            private fun syncOnClickListener(exam: OfflineExam) {
                binding.syncButton.setOnClickListener {
                    offlineExamViewModel.syncExam(exam)
                }
            }

            private fun startExamOnClickListener(exam: OfflineExam) {
                binding.startExamOffline.setOnClickListener {
                    CoroutineScope(Dispatchers.IO).launch {
                        val greenDaoContent = getContentFromDB(exam.contentId!!)
                        greenDaoContent?.exam = exam.asGreenDaoModel()
                        withContext(Dispatchers.Main) {
                            TestpressExam.startCourseExam(
                                this@OfflineExamListActivity, greenDaoContent!!, false, false,
                                TestpressSdk.getTestpressSession(this@OfflineExamListActivity)!!
                            )
                        }
                    }
                }
            }

            private fun resumeExamOnClickListener(exam: OfflineExam) {
                binding.resumeExamOffline.setOnClickListener {
                    CoroutineScope(Dispatchers.IO).launch {
                        val greenDaoContent = getContentFromDB(exam.contentId!!)
                        greenDaoContent?.exam = exam.asGreenDaoModel()
                        val offlineAttempt =
                            offlineExamViewModel.getOfflineAttemptsByExamIdAndState(
                                exam.id!!,
                                Attempt.RUNNING
                            ).lastOrNull()
                        offlineAttempt?.let {
                            val offlineAttemptSectionList =
                                offlineExamViewModel.getOfflineAttemptSectionList(it.id)
                            val offlineContentAttempt =
                                offlineExamViewModel.getOfflineContentAttempts(it.id)
                            val pausedCourseAttempt = offlineContentAttempt?.createGreenDoaModel(
                                offlineAttempt.createGreenDoaModel(
                                    offlineAttemptSectionList.asGreenDoaModels()
                                )
                            )!!
                            withContext(Dispatchers.Main) {
                                TestpressExam.resumeCourseAttempt(
                                    this@OfflineExamListActivity,
                                    greenDaoContent!!,
                                    pausedCourseAttempt,
                                    false,
                                    TestpressSdk.getTestpressSession(this@OfflineExamListActivity)!!
                                )
                            }
                        }
                    }
                }
            }

            private fun updateButtonVisibility(exam: OfflineExam) {
                if (exam.downloadedQuestionCount.toInt() == exam.numberOfQuestions) {
                    binding.syncButton.isVisible = exam.isSyncRequired
                    binding.startExamOffline.isVisible =
                        !exam.isSyncRequired && (exam.pausedAttemptsCount ?: 0) == 0
                    binding.deleteButton.isVisible = true
                    binding.downloadingButton.isVisible = false
                    binding.resumeExamOffline.isVisible = (exam.pausedAttemptsCount ?: 0) > 0
                } else {
                    binding.syncButton.isVisible = false
                    binding.startExamOffline.isVisible = false
                    binding.deleteButton.isVisible = false
                    binding.downloadingButton.isVisible = true
                    binding.resumeExamOffline.isVisible = false
                }
            }

            private fun getContentFromDB(contentId: Long): Content? {
                val contentDao = TestpressSDKDatabase.getContentDao(this@OfflineExamListActivity)
                val contents =
                    contentDao.queryBuilder().where(ContentDao.Properties.Id.eq(contentId)).list()

                if (contents.isEmpty()) {
                    return null
                }
                return contents[0]
            }

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExamViewHolder {
            val binding =
                OfflineExamListItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
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