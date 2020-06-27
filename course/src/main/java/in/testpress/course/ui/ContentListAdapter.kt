package `in`.testpress.course.ui

import `in`.testpress.core.TestpressSDKDatabase
import `in`.testpress.course.domain.ContentType
import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.ui.viewholders.BaseContentListItemViewHolder
import `in`.testpress.course.ui.viewholders.ContentListItemViewHolder
import `in`.testpress.course.ui.viewholders.ExamContentListItemViewHolder
import `in`.testpress.course.ui.viewholders.VideoContentListItemViewHolder
import `in`.testpress.models.greendao.ChapterDao
import `in`.testpress.models.greendao.CourseDao
import `in`.testpress.store.TestpressStore
import `in`.testpress.store.ui.ProductDetailsActivity
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter


class ContentListAdapter(val chapterId: Long,
                         val productSlug: String?) :
        ListAdapter<DomainContent, BaseContentListItemViewHolder>(DOMAIN_CONTENT_COMPARATOR) {
    var contents: List<DomainContent> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseContentListItemViewHolder {
        return when (viewType) {
            ContentType.Exam.ordinal -> ExamContentListItemViewHolder.create(parent)
            ContentType.Quiz.ordinal -> ExamContentListItemViewHolder.create(parent)
            ContentType.Video.ordinal -> VideoContentListItemViewHolder.create(parent)
            ContentType.Notes.ordinal -> ContentListItemViewHolder.create(parent)
            ContentType.Attachment.ordinal -> ContentListItemViewHolder.create(parent)
            else -> ContentListItemViewHolder.create(parent)
        }
    }

    override fun getItemCount(): Int {
        return contents.size
    }

    override fun getItem(position: Int): DomainContent? {
        if (contents.size > position) return contents[position]
        return null
    }

    override fun getItemViewType(position: Int): Int {
        val content = getItem(position)
        return content?.contentTypeEnum?.ordinal ?: 0
    }

    override fun onBindViewHolder(holder: BaseContentListItemViewHolder, position: Int) {
        val content = getItem(position)
        if (content != null) {
            holder.bind(content, shouldPurchase(content, holder.itemView.context)) {
                onItemClick(it, holder.itemView.context)
            }
        }
    }

    private fun onItemClick(content: DomainContent, context: Context) {
        if (shouldPurchase(content, context)) {
            val intent = Intent(context, ProductDetailsActivity::class.java)
            intent.putExtra(ProductDetailsActivity.PRODUCT_SLUG, productSlug)
             (context as Activity).startActivityForResult(intent, TestpressStore.STORE_REQUEST_CODE)
        } else if (content.isLocked != true && content.isScheduled != true) {
            context.startActivity(ContentActivity.createIntent(
                    content.id,
                    context,
                    productSlug)
            );
        }
    }

    private fun shouldPurchase(content: DomainContent, context: Context): Boolean {
        val chapterDao = TestpressSDKDatabase.getChapterDao(context)
        val courseDao = TestpressSDKDatabase.getCourseDao(context)
        val chapters = chapterDao.queryBuilder().where(ChapterDao.Properties.Id.eq(chapterId)).list()
        val chapter = chapters[0]
        val course = courseDao.queryBuilder().where(CourseDao.Properties.Id.eq(chapter.courseId)).list().get(0)
        if (course.isMyCourse == true) {
            return false
        }
        return content.freePreview != true
    }

    companion object {
        private val DOMAIN_CONTENT_COMPARATOR = object : DiffUtil.ItemCallback<DomainContent>() {
            override fun areContentsTheSame(oldItem: DomainContent, newItem: DomainContent): Boolean =
                    oldItem == newItem

            override fun areItemsTheSame(oldItem: DomainContent, newItem: DomainContent): Boolean =
                    oldItem.id == newItem.id
        }
    }
}
