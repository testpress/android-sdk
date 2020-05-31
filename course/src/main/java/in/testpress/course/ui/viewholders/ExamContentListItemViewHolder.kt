package `in`.testpress.course.ui.viewholders

import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.R
import `in`.testpress.course.domain.ContentType
import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.domain.DomainExamContent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView

class ExamContentListItemViewHolder(view: View) : BaseContentListItemViewHolder(view) {
    private val duration: TextView = view.findViewById(R.id.duration)
    private val durationContainer: LinearLayout = view.findViewById(R.id.duration_container)
    private val numberOfQuestions: TextView = view.findViewById(R.id.number_of_questions)

    init {
        duration.typeface = TestpressSdk.getRubikMediumFont(view.context)
        numberOfQuestions.typeface = TestpressSdk.getRubikMediumFont(view.context)
    }

    override fun bindContentDetails(content: DomainContent) {
        content.exam?.let {
            numberOfQuestions.text = it.numberOfQuestions.toString() + " Qs"
            bindDuration(content, it)
        }
    }

    private fun bindDuration(content: DomainContent, exam: DomainExamContent) {
        if (content.contentTypeEnum == ContentType.Quiz || exam.duration.isNullOrBlank()) {
            durationContainer.visibility = View.GONE
        } else {
            duration.text = exam.duration
            durationContainer.visibility = View.VISIBLE
        }
    }

    companion object {
        fun create(parent: ViewGroup): ExamContentListItemViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.exam_content_list_item, parent, false)
            return ExamContentListItemViewHolder(view)
        }

    }
}