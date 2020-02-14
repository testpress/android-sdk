package `in`.testpress.course.ui.fragments.content_fragments

import `in`.testpress.core.TestpressSDKDatabase
import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.R
import `in`.testpress.course.network.TestpressCourseApiClient
import `in`.testpress.course.ui.ContentActivity.CHAPTER_ID
import `in`.testpress.course.ui.view_models.ContentViewModel
import `in`.testpress.exam.network.TestpressExamApiClient
import `in`.testpress.models.greendao.ContentDao
import `in`.testpress.util.ViewUtils
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*


abstract class BaseContentDetailFragment : Fragment() {
    private lateinit var emptyTitleView: TextView
    private lateinit var emptyDescView: TextView
    private lateinit var pageNumber: TextView
    private lateinit var contentView: RelativeLayout
    private lateinit var emptyContainer: LinearLayout
    private lateinit var buttonLayout: LinearLayout
    private lateinit var retryButton: Button
    private lateinit var previousButton: Button
    private lateinit var nextButton: Button
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var toast: Toast

    private var chapterId: Long = -1
    private lateinit var contentDao: ContentDao
    private lateinit var examApiClient: TestpressExamApiClient
    private lateinit var courseApiClient: TestpressCourseApiClient
    val vm: ContentViewModel by lazy { ViewModelProviders.of(this).get(ContentViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contentDao = TestpressSDKDatabase.getContentDao(activity)
        examApiClient = TestpressExamApiClient(activity);
        courseApiClient = TestpressCourseApiClient(activity)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.testpress_activity_content_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews()
        initializeListenters()
        parseIntentArguments()
    }

    private fun bindViews() {
        contentView = view!!.findViewById(R.id.main_content)
        emptyContainer = view!!.findViewById(R.id.empty_container)
        emptyTitleView = view!!.findViewById(R.id.empty_title)
        emptyDescView = view!!.findViewById(R.id.empty_description)
        retryButton = view!!.findViewById(R.id.retry_button)
        toast = Toast.makeText(activity, R.string.testpress_no_internet_try_again, Toast.LENGTH_SHORT)
        previousButton = view!!.findViewById(R.id.previous)
        nextButton = view!!.findViewById(R.id.next)
        pageNumber = view!!.findViewById(R.id.page_number)
        buttonLayout = view!!.findViewById(R.id.button_layout)
        swipeRefresh = view!!.findViewById(R.id.swipe_container)
        swipeRefresh.setColorSchemeResources(R.color.testpress_color_primary)

        ViewUtils.setTypeface(
                arrayOf(previousButton, nextButton, pageNumber),
                TestpressSdk.getRubikMediumFont(activity!!)
        )
    }

    private fun initializeListenters() {
        swipeRefresh.setOnRefreshListener { updateContent() }
    }

    private fun parseIntentArguments() {
        chapterId = arguments!!.getLong(CHAPTER_ID)

    }
}