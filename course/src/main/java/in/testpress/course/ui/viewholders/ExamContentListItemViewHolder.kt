package `in`.testpress.course.ui.viewholders

import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.R
import `in`.testpress.course.domain.ContentType
import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.domain.DomainExamContent
import android.os.Build
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.text.HtmlCompat

class ExamContentListItemViewHolder(view: View) : BaseContentListItemViewHolder(view) {
    private val duration: TextView = view.findViewById(R.id.duration)
    private val durationContainer: LinearLayout = view.findViewById(R.id.duration_container)
    private val numberOfQuestions: TextView = view.findViewById(R.id.number_of_questions)
    private val description: TextView = view.findViewById(R.id.description)
    private val detailsContainer: LinearLayout = view.findViewById(R.id.content_details_container)
    private val descriptionContainer: LinearLayout = view.findViewById(R.id.description_container)

    init {
        duration.typeface = TestpressSdk.getRubikMediumFont(view.context)
        numberOfQuestions.typeface = TestpressSdk.getRubikMediumFont(view.context)
        description.typeface = TestpressSdk.getRubikMediumFont(view.context)
    }

    override fun bindContentDetails(content: DomainContent) {
        displayDescription(content)
        if (content.exam != null) {
            numberOfQuestions.text = content.exam!!.numberOfQuestions.toString() + " Qs"
            bindDuration(content, content.exam!!)
        } else {
            detailsContainer.visibility = View.GONE
        }
    }

    private fun displayDescription(content: DomainContent) {
        if (!content.description.isNullOrBlank()) {
            descriptionContainer.visibility = View.VISIBLE

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                description.text = Html.fromHtml(content.description, HtmlCompat.FROM_HTML_MODE_LEGACY)
            } else {
                description.text = Html.fromHtml(content.description)
            }
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