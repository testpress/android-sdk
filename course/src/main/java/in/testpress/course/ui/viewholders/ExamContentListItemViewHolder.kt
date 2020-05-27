package `in`.testpress.course.ui.viewholders

import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.R
import `in`.testpress.course.domain.DomainContent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

class ExamContentListItemViewHolder(view: View) : BaseContentListItemViewHolder(view) {
    private val duration: TextView = view.findViewById(R.id.duration)
    private val numberOfQuestions: TextView = view.findViewById(R.id.number_of_questions)

    init {
        duration.typeface = TestpressSdk.getRubikMediumFont(view.context)
        numberOfQuestions.typeface = TestpressSdk.getRubikMediumFont(view.context)
    }

    override fun bindContentDetails(content: DomainContent) {
        content.exam?.let {
            duration.text = it.duration
            numberOfQuestions.text = it.numberOfQuestions.toString()
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