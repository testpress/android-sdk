package `in`.testpress.course.ui

import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.R
import `in`.testpress.course.databinding.OfflineExamListActivityBinding
import `in`.testpress.course.databinding.OfflineExamListItemBinding
import `in`.testpress.course.util.ProgressDialog
import `in`.testpress.course.util.SwipeToDeleteCallback
import `in`.testpress.course.viewmodels.OfflineExamViewModel
import `in`.testpress.database.entities.OfflineExam
import `in`.testpress.enums.Status
import `in`.testpress.exam.TestpressExam
import `in`.testpress.ui.BaseToolBarActivity
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OfflineExamListActivity : BaseToolBarActivity() {

    private lateinit var binding: OfflineExamListActivityBinding
    private lateinit var offlineExamViewModel: OfflineExamViewModel
    private lateinit var offlineExamAdapter: OfflineExamAdapter
    private lateinit var progressDialog: ProgressDialog
    private lateinit var onItemClickListener: OnItemClickListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = OfflineExamListActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setActionBarTitle("Offline Exam")
        initializeViewModel()
        initializeOnItemClickListener()
        initializeListAdapter()
        initializeListView()
        initializeProgressDialog()
        syncExamsModifiedDates()
        observeCompletedAttemptSyncResult()
    }

    override fun onResume() {
        super.onResume()
        syncCompletedAttempts()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.offline_attempt_sync, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.sync_completed_attempt -> {
                syncCompletedAttempts(forceSync = true)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun initializeViewModel() {
        offlineExamViewModel = OfflineExamViewModel.initializeViewModel(this)
    }

    private fun initializeOnItemClickListener() {
        onItemClickListener = object : OnItemClickListener {
            override fun onItemClick(exam: OfflineExam) {
                if ((exam.pausedAttemptsCount ?: 0) > 0) {
                    resumeExam(exam)
                } else {
                    if (exam.isSyncRequired) {
                        observeExamDownloadState(exam)
                        monitorAndShowExamDownloadProgress(exam.contentId!!)
                        offlineExamViewModel.downloadExam(exam.contentId!!)
                    } else {
                        startExam(exam)
                    }
                }
            }
        }
    }

    private fun initializeListAdapter() {
        offlineExamAdapter = OfflineExamAdapter(onItemClickListener)
        binding.recyclerView.adapter = offlineExamAdapter
        val deleteIcon = ContextCompat.getDrawable(this, R.drawable.ic_baseline_delete_forever_24)
        val swipeToDeleteCallback =
            SwipeToDeleteCallback(deleteIcon!!, object : SwipeToDeleteCallback.OnSwipeListener {
                override fun onSwiped(position: Int) {
                    offlineExamAdapter.removeItem(position)
                }
            })
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }

    private fun initializeListView() {
        offlineExamViewModel.getAll().observe(this) { exams ->
            offlineExamAdapter.submitList(exams)
            binding.recyclerView.visibility = if (exams.isEmpty()) View.GONE else View.VISIBLE
            binding.noDataLayout.visibility = if (exams.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun initializeProgressDialog() {
        progressDialog = ProgressDialog.create(
            this,
            "Syncing Exam...",
            false
        )
    }

    private fun syncExamsModifiedDates() {
        offlineExamViewModel.syncExamsModifiedDates()
    }

    private fun syncCompletedAttempts(forceSync: Boolean = false) {
        offlineExamViewModel.syncCompletedAllAttemptToBackEnd(forceSync)
    }

    private fun observeCompletedAttemptSyncResult() {
        offlineExamViewModel.syncCompletedAttempt.observe(this) { it ->
            when (it.status) {
                Status.SUCCESS -> {
                    Toast.makeText(
                        this,
                        "Answers submitted successfully. To review the results, please visit the exam page within the course.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                Status.LOADING -> {}
                Status.ERROR -> {
                    Toast.makeText(
                        this,
                        "Please connect to internet to submit your answers.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {}
            }
        }
    }

    private fun resumeExam(exam: OfflineExam) {
        CoroutineScope(Dispatchers.IO).launch {
            val content = offlineExamViewModel.getOfflineExamContent(exam.contentId!!)
            val pausedAttempt = offlineExamViewModel.getOfflinePausedAttempt(exam.id!!)
            if (content != null && pausedAttempt != null) {
                withContext(Dispatchers.Main) {
                    TestpressExam.resumeCourseAttempt(
                        this@OfflineExamListActivity,
                        content,
                        pausedAttempt,
                        false,
                        TestpressSdk.getTestpressSession(this@OfflineExamListActivity)!!
                    )
                }
            }
        }
    }

    private fun startExam(exam: OfflineExam) {
        CoroutineScope(Dispatchers.IO).launch {
            offlineExamViewModel.getOfflineExamContent(exam.contentId!!)?.let { content ->
                withContext(Dispatchers.Main) {
                    TestpressExam.startCourseExam(
                        this@OfflineExamListActivity, content, false, false,
                        TestpressSdk.getTestpressSession(this@OfflineExamListActivity)!!
                    )
                }
            }
        }
    }

    private fun monitorAndShowExamDownloadProgress(contentId: Long) {
        offlineExamViewModel.get(contentId)
            .observe(this@OfflineExamListActivity) { offlineExam ->
                if (offlineExam != null && ((offlineExam.numberOfQuestions
                        ?: 0) >= offlineExam.downloadedQuestionCount.toInt())
                ) {
                    updateProgressDialog(offlineExam.getDownloadProgress())
                }
            }
    }

    private fun observeExamDownloadState(offlineExam: OfflineExam) {
        offlineExamViewModel.downloadExamResult.observe(this) { result ->
            when (result.status) {
                Status.SUCCESS -> {
                    hideProgressDialog()
                    startExam(offlineExam)
                }
                Status.LOADING -> {
                    showProgressDialog()
                }
                Status.ERROR -> {
                    hideProgressDialog()
                    startExam(offlineExam)
                }
            }

        }
    }

    private fun showProgressDialog() {
        if (!progressDialog.isShowing) {
            progressDialog.show()
        }
    }

    private fun updateProgressDialog(progress: Int) {
        if (progressDialog.isShowing) {
            progressDialog.updateProgress(progress)
        }
    }

    private fun hideProgressDialog() {
        if (progressDialog.isShowing) {
            progressDialog.dismiss()
        }
    }

    inner class OfflineExamAdapter(private val clickListener: OnItemClickListener) :
        ListAdapter<OfflineExam, OfflineExamAdapter.ExamViewHolder>(EXAM_COMPARATOR) {

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

        fun removeItem(position: Int) {
            val exam = getItem(position)
            offlineExamViewModel.deleteOfflineExam(exam.id!!)
        }

        inner class ExamViewHolder(private val binding: OfflineExamListItemBinding) :
            RecyclerView.ViewHolder(binding.root) {

            init {
                binding.examTitle.typeface = TestpressSdk.getRubikMediumFont(binding.root.context)
                binding.duration.typeface = TestpressSdk.getRubikMediumFont(binding.root.context)
                binding.numberOfQuestions.typeface =
                    TestpressSdk.getRubikMediumFont(binding.root.context)
            }

            fun bind(exam: OfflineExam) {
                updateExamDetails(exam)
                itemView.setOnClickListener {
                    clickListener.onItemClick(exam)
                }
            }

            private fun updateExamDetails(exam: OfflineExam) {
                binding.examTitle.text = exam.title
                binding.duration.text = exam.duration
                binding.numberOfQuestions.text = exam.numberOfQuestions.toString()
                binding.examResumeState.isVisible = ((exam.pausedAttemptsCount ?: 0) > 0)
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(exam: OfflineExam)
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