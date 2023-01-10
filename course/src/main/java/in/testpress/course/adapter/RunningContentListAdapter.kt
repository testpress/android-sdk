package `in`.testpress.course.adapter

import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.R
import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.ui.ContentActivity
import `in`.testpress.database.entities.RunningContentEntity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class RunningContentListAdapter :
    ListAdapter<RunningContentEntity, RunningContentViewHolder>(DOMAIN_CONTENT_COMPARATOR) {

    var contents: List<RunningContentEntity> = listOf()

    companion object {
        private val DOMAIN_CONTENT_COMPARATOR = object : DiffUtil.ItemCallback<RunningContentEntity>() {
            override fun areContentsTheSame(
                oldItem: RunningContentEntity,
                newItem: RunningContentEntity
            ): Boolean =
                oldItem == newItem

            override fun areItemsTheSame(oldItem: RunningContentEntity, newItem: RunningContentEntity): Boolean =
                oldItem.id == newItem.id
        }
    }

    override fun getItemCount(): Int {
        return contents.size
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RunningContentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.runn, parent, false)
        return RunningContentViewHolder(view)
    }

    private fun onItemClick(content: RunningContentEntity, context: Context) {
        context.startActivity(
            ContentActivity.createIntent(
                content.id,
                context,
                ""
            )
        );

    }

    override fun getItem(position: Int): RunningContentEntity? {
        if (contents.size > position) return contents[position]
        return null
    }

    override fun onBindViewHolder(holder: RunningContentViewHolder, position: Int) {
        val content = getItem(position)
        if (content != null) {
            holder.bind(content) { onItemClick(content,holder.itemView.context)}
        }
    }
}

class RunningContentViewHolder(val view: View):RecyclerView.ViewHolder(view){
    private val title = view.findViewById<TextView>(R.id.running_content_title)
    private val path = view.findViewById<TextView>(R.id.tree_path)
    //private val date = view.findViewById<TextView>(R.id.start_date_and_end_date)
    private val image = view.findViewById<ImageView>(R.id.running_content_image)


    init {
        title.typeface = TestpressSdk.getRubikMediumFont(view.context)
        path.typeface = TestpressSdk.getRubikMediumFont(view.context)
        //date.typeface = TestpressSdk.getRubikMediumFont(view.context)
    }

    fun bind(content: RunningContentEntity, clickListener: (RunningContentEntity) -> Unit){
        title.text = content.title
        path.text = "Courses > HYBRID ONL IIT - 2024 (INTEGRATED PROGRAMME) > MATHEMATICS > Trigonometry - I > Lecture Videos"
        image.setImageResource(R.drawable.testpress_video_content_icon)
        itemView.setOnClickListener { clickListener(content)}
    }

}