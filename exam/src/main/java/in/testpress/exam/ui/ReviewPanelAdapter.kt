package `in`.testpress.exam.ui

import `in`.testpress.exam.R
import `in`.testpress.models.greendao.ReviewItem
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.review_panel_item_layout.view.*

class ReviewPanelAdapter(var questions: List<ReviewItem>, val listener: ListItemClickListener): RecyclerView.Adapter<ReviewPanelAdapter.ViewHolder>() {

    private val onClickListener: View.OnClickListener = View.OnClickListener { v ->
        val item = v.tag as ReviewItem
        listener.onItemClicked(questions.indexOf(item))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewPanelAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.review_panel_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewPanelAdapter.ViewHolder, position: Int) {
        val item = questions[position]
        holder.questionIndex.text = item.index.toString()
        setBackgroundColor(holder.questionIndex, item)
        with(holder.view) {
            tag = item
            setOnClickListener(onClickListener)
        }
    }

    fun setItems(items: List<ReviewItem>) {
        questions = items
    }

    private fun setBackgroundColor(view: View, reviewItem: ReviewItem) {
        var backgroundColor = R.color.testpress_text_gray
        if (reviewItem.result == null || reviewItem.result == ReviewItem.UNANSWERED) {
            backgroundColor = R.color.testpress_text_gray
        } else if (reviewItem.result == ReviewItem.ANSWERED_INCORRECT) {
            backgroundColor = R.color.testpress_red
        }
        view.setBackgroundColor(ContextCompat.getColor(view.context, backgroundColor))
    }

    override fun getItemCount(): Int = questions.size

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val questionIndex: TextView = view.question_index_all
    }
}

interface ListItemClickListener {
    fun onItemClicked(position: Int)
}