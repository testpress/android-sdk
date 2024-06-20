package `in`.testpress.course.ui

import `in`.testpress.course.R
import `in`.testpress.course.repository.OfflineExamRepository
import `in`.testpress.course.viewmodels.OfflineExamViewModel
import `in`.testpress.database.entities.OfflineExam
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class OfflineExamListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: OfflineExamAdapter
    private lateinit var offlineExamViewModel: OfflineExamViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_offline_exam_list)

        offlineExamViewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return OfflineExamViewModel(
                    OfflineExamRepository(this@OfflineExamListActivity)
                ) as T
            }
        }).get(OfflineExamViewModel::class.java)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = OfflineExamAdapter(emptyList())
        recyclerView.adapter = adapter

        offlineExamViewModel.getAllOfflineExams().observe(this) { exams ->
            adapter = OfflineExamAdapter(exams)
            recyclerView.adapter = adapter
        }
    }

    inner class OfflineExamAdapter(private val exams: List<OfflineExam>) : RecyclerView.Adapter<OfflineExamAdapter.ExamViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExamViewHolder {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_offline_exam, parent, false)
            return ExamViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: ExamViewHolder, position: Int) {
            val exam = exams[position]
            holder.bind(exam)
        }

        override fun getItemCount(): Int {
            return exams.size
        }

        inner class ExamViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
            private val totalTime: TextView = itemView.findViewById(R.id.total_time)
            private val totalQuestion: TextView = itemView.findViewById(R.id.number_of_questions)

            fun bind(exam: OfflineExam) {
                titleTextView.text = exam.title
                totalTime.text = exam.duration
                totalQuestion.text = exam.numberOfQuestions.toString()
                // Bind other properties of OfflineExam as needed
            }
        }
    }
}