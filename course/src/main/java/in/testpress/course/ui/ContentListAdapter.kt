package `in`.testpress.course.ui

import `in`.testpress.core.TestpressSDKDatabase
import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.R
import `in`.testpress.course.domain.ContentType
import `in`.testpress.course.domain.DomainContent
import `in`.testpress.models.greendao.Chapter
import `in`.testpress.models.greendao.ChapterDao
import `in`.testpress.models.greendao.Course
import `in`.testpress.models.greendao.CourseDao
import `in`.testpress.store.TestpressStore
import `in`.testpress.store.ui.ProductDetailsActivity
import `in`.testpress.util.ImageUtils
import `in`.testpress.util.SingleTypeAdapter
import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.github.testpress.mikephil.charting.charts.PieChart
import com.github.testpress.mikephil.charting.data.PieData
import com.github.testpress.mikephil.charting.data.PieDataSet
import com.github.testpress.mikephil.charting.data.PieEntry
import java.util.ArrayList

class ContentListAdapter(val activity: Activity, val chapterId: Long, val productSlug: String? = null) :
    SingleTypeAdapter<DomainContent>(activity, R.layout.testpress_content_list_item) {

    private val imageLoader = ImageUtils.initImageLoader(activity)
    private val imageOptions = ImageUtils.getPlaceholdersOption()
    private lateinit var content: DomainContent

    override fun getItemId(position: Int): Long {
        return getItem(position).id
    }

    override fun getChildViewIds(): IntArray {
        return intArrayOf(
            R.id.content_title,
            R.id.thumbnail_image,
            R.id.white_foreground,
            R.id.lock,
            R.id.content_item_layout,
            R.id.exam_info_layout,
            R.id.attempted_tick,
            R.id.duration,
            R.id.no_of_questions,
            R.id.video_completion_progress_chart,
            R.id.video_completion_progress_container,
            R.id.lock_image,
            R.id.general_info_layout,
            R.id.info,
            R.id.video_info,
            R.id.video_duration
        )
    }

    override fun update(position: Int, content: DomainContent) {
        setTypeface(intArrayOf(0, 7, 8, 13), TestpressSdk.getRubikMediumFont(activity))
        this.content = content
        hideViews() // Initially hide un common views, so that when reusing view it won't appear
        setTitle()
        setImage()
        showOrHideLockIcon()
        showOrHideScheduledContent()
        showAdditionalData()
        setOnClickListener()
        handleStoreCourseContent()
    }

    private fun hideViews() {
        setGone(5, true)
        setGone(14, true)
    }

    private fun showOrHideScheduledContent() {
        setGone(12, true)

        if (content.isScheduled == true) {
            if (content.getFormattedStart() != null) {
                setText(13, "This will be available on " + content.getFormattedStart())
            } else {
                setText(13, "Coming soon")
            }
            (view<View>(11) as ImageView).setImageResource(R.drawable.clock)
            setGone(5, true)
            setGone(14, true)
            setGone(12, false)
        }
    }

    private fun showAdditionalData() {
        if (content.contentTypeEnum in arrayListOf(ContentType.Quiz, ContentType.Exam)) {
            showExamDetails()
        } else if(content.contentTypeEnum == ContentType.Video) {
            showVideoDetails()
        }
    }

    private fun setOnClickListener() {
        view<View>(4).setOnClickListener {
            activity.startActivity(ContentActivity.createIntent(content.id, activity, productSlug))
        }
    }

    private fun showOrHideLockIcon() {
        if (content.isLocked == true || content.isScheduled == true) {
            setGone(2, false)
            setGone(3, false)
            setGone(6, true)
            setGone(10, true)
            view<View>(4).isClickable = false
        } else {
            setGone(2, true)
            setGone(3, true)
            view<View>(4).isClickable = true
        }
    }

    private fun setTitle() {
        setText(0, content.title)
    }

    private fun setImage() {
        if (content.image.isNullOrEmpty()) {
            setGone(1, true)
        } else {
            setGone(1, false)
            imageLoader.displayImage(content.image, imageView(1), imageOptions)
        }
    }

    private fun showVideoDetails() {
        showWatchPercentage()
        showDuration()
    }

    private fun showDuration() {
        val video = content.video
        setGone(14, true)
        video?.duration.let { duration ->
            setGone(14, false)
            setText(15, duration)
        }
    }

    private fun showWatchPercentage() {
        if (content.video?.isNativeVideo == true) {
            when (content.videoWatchedPercentage) {
                0 -> {
                    setGone(6, true)
                    setGone(10, true)
                }
                100 -> {
                    setGone(6, false)
                    setGone(10, true)
                }
                else -> {
                    setGone(6, true)
                    displayVideoWatchedPercentage(
                        view<View>(9) as PieChart,
                        content.videoWatchedPercentage!!
                    )
                    setGone(10, false)
                }
            }
        }
    }

    private fun displayVideoWatchedPercentage(chart: PieChart, percentage: Int) {
        val data: PieData = getVideoProgressPieChartData(percentage.toLong())
        chart.data = data
        chart.setDescription("")
        chart.isClickable = false
        chart.setTouchEnabled(false)
        chart.setUsePercentValues(true)
        chart.centerText = "$percentage%"
        chart.setCenterTextSize(6f)
        chart.setCenterTextColor(ContextCompat.getColor(activity, R.color.testpress_text_gray))
        chart.holeRadius = 85f
        chart.transparentCircleRadius = 0f
        chart.setExtraOffsets(0f, 0f, 0f, 0f)
        chart.legend.isEnabled = false
    }

    private fun getVideoProgressPieChartData(watchedDuration: Long): PieData {
        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(watchedDuration.toFloat(), 0))
        entries.add(PieEntry((100 - watchedDuration).toFloat(), 1))
        val dataSet = PieDataSet(entries, "")
        val colors = ArrayList<Int>()
        colors.add(ContextCompat.getColor(activity, R.color.testpress_green))
        colors.add(ContextCompat.getColor(activity, R.color.testpress_gray_light))
        dataSet.colors = colors
        val data = PieData(dataSet)
        data.setDrawValues(false)
        return data
    }

    private fun handleStoreCourseContent() {
        val chapterDao = TestpressSDKDatabase.getChapterDao(activity)
        val courseDao = TestpressSDKDatabase.getCourseDao(activity)
        val chapters: List<Chapter> =
            chapterDao.queryBuilder().where(ChapterDao.Properties.Id.eq(chapterId)).list()
        val course: Course =
            courseDao.queryBuilder().where(CourseDao.Properties.Id.eq(chapters[0].courseId)).list()
                .get(0)

        if (shouldOpenPaymentPage(course, content)) {
            setGone(2, false)
            setGone(3, false)
            setGone(6, true)
            setGone(10, true)
            (view<View>(11) as ImageView).setImageResource(R.drawable.crown)
            view<View>(4).isClickable = true
            view<View>(4).setOnClickListener {
                val intent = Intent(activity, ProductDetailsActivity::class.java)
                intent.putExtra(ProductDetailsActivity.PRODUCT_SLUG, productSlug)
                activity.startActivityForResult(intent, TestpressStore.STORE_REQUEST_CODE)
            }
        }
    }

    private fun shouldOpenPaymentPage(course: Course, content: DomainContent): Boolean {
        return if (course.isMyCourse == true) {
            false
        } else productSlug != null && (content.freePreview == null || !content.freePreview)
    }

    private fun showExamDetails() {
        val exam = content.exam
        setGone(5, true)

        if (content.attemptsCount == 0 || content.attemptsCount == 1 && exam?.isPaused == true) {
            setGone(6, true)
            setGone(10, true)
        }

        exam?.let{
            setGone(5, false)
            setText(7, exam.duration)
            setText(8, exam.numberOfQuestions.toString() + " Qs")
        }
    }
}
