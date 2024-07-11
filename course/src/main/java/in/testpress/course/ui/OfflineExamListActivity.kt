package `in`.testpress.course.ui

import `in`.testpress.core.TestpressSDKDatabase
import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.R
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
import android.app.ProgressDialog
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Math.abs


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
        val itemTouchHelper = ItemTouchHelper(simpleCallback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }

    private val simpleCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

        private val scaleFactor = 0.2f // Swipe distance percentage to trigger scale effect
        private var scale = 0.0f // Initial scale value for pop-out animation


        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.bindingAdapterPosition
            offlineExamAdapter.removeItem(position)
        }

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            val itemView = viewHolder.itemView
            val background = ColorDrawable(Color.RED)
            val iconMargin = (itemView.height - deleteIcon.intrinsicHeight) / 2

            if (dX < 0) { // Swiping to the left
                background.setBounds(
                    itemView.right + dX.toInt(),
                    itemView.top,
                    itemView.right,
                    itemView.bottom
                )

                val swipePercentage = kotlin.math.abs(dX) / viewHolder.itemView.width
                scale = when {
                    swipePercentage <= scaleFactor -> {
                        swipePercentage / scaleFactor
                    }
                    else -> {
                        1.0f
                    }
                }

                deleteIcon.setBounds(
                    itemView.right - iconMargin - (deleteIcon.intrinsicWidth * scale).toInt(),
                    itemView.top + iconMargin + (deleteIcon.intrinsicHeight * (1 - scale) / 2).toInt(),
                    itemView.right - iconMargin,
                    itemView.bottom - iconMargin - (deleteIcon.intrinsicHeight * (1 - scale) / 2).toInt()
                )
            } else {
                background.setBounds(0, 0, 0, 0)
                deleteIcon.setBounds(0, 0, 0, 0)
            }

            background.draw(c)
            deleteIcon.draw(c)

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    }

    private val deleteIcon: Drawable by lazy {
        ContextCompat.getDrawable(this, R.drawable.ic_baseline_delete_forever_24)!!
    }

    private fun initializeListView() {
        offlineExamViewModel.getAll().observe(this) { exams ->
            offlineExamAdapter.submitList(exams)
            binding.recyclerView.visibility = if (exams.isEmpty()) View.GONE else View.VISIBLE
            binding.noDataLayout.visibility = if (exams.isEmpty()) View.VISIBLE else View.GONE
        }
        binding.recyclerView.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )
    }

    private fun syncExamsModifiedDates() {
        offlineExamViewModel.syncExamsModifiedDates()
    }

    private fun syncCompletedAttempts() {
        offlineExamViewModel.syncCompletedAttemptToBackEnd()
    }

    inner class OfflineExamAdapter :
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
                    if ((exam.pausedAttemptsCount ?: 0) > 0) {
                        resumeExam(exam)
                    } else {
                        startExam(exam)
                    }
                }
            }

            private fun updateExamDetails(exam: OfflineExam) {
                binding.examTitle.text = exam.title
                binding.duration.text = exam.duration
                binding.numberOfQuestions.text = exam.numberOfQuestions.toString()
            }

            private fun resumeExam(exam: OfflineExam) {
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

            private fun getContentFromDB(contentId: Long): Content? {
                val contentDao = TestpressSDKDatabase.getContentDao(this@OfflineExamListActivity)
                val contents =
                    contentDao.queryBuilder().where(ContentDao.Properties.Id.eq(contentId)).list()

                if (contents.isEmpty()) {
                    return null
                }
                return contents[0]
            }

            private fun startExam(exam: OfflineExam) {
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