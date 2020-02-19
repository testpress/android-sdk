package `in`.testpress.course.ui.fragments.content_fragments

import `in`.testpress.core.TestpressSDKDatabase
import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.R
import `in`.testpress.course.TestpressCourse.PRODUCT_SLUG
import `in`.testpress.course.enums.Status
import `in`.testpress.course.network.TestpressCourseApiClient
import `in`.testpress.course.ui.ContentActivity.*
import `in`.testpress.course.ui.fragments.BookmarkFragment
import `in`.testpress.course.ui.fragments.BookmarkListener
import `in`.testpress.course.ui.view_models.ContentViewModel
import `in`.testpress.exam.network.TestpressExamApiClient
import `in`.testpress.models.greendao.Content
import `in`.testpress.models.greendao.ContentDao
import `in`.testpress.util.ViewUtils
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import `in`.testpress.core.TestpressException
import android.support.annotation.VisibleForTesting


abstract class BaseContentDetailFragment : Fragment(), BookmarkListener {
    private lateinit var emptyTitleView: TextView
    private lateinit var emptyDescView: TextView
    private lateinit var pageNumber: TextView
    private lateinit var contentView: RelativeLayout
    protected lateinit var emptyContainer: LinearLayout
    private lateinit var buttonLayout: LinearLayout
    protected lateinit var retryButton: Button
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    lateinit var previousButton: Button
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    lateinit var nextButton: Button
    protected lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var toast: Toast

    private var chapterId: Long = -1
    private var contentId: String? = null
    private var productSlug: String? = null
    open var isBookmarkEnabled = true
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    var position: Int = -1
    protected lateinit var content: Content
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    lateinit var contentDao: ContentDao
    private lateinit var examApiClient: TestpressExamApiClient
    private lateinit var courseApiClient: TestpressCourseApiClient
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    open lateinit var viewModel: ContentViewModel

    override val bookmarkId: Long?
        get() = content.bookmarkId
    override val bookmarkContentId: Long?
        get() = content.id

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contentDao = TestpressSDKDatabase.getContentDao(activity)
        examApiClient = TestpressExamApiClient(activity);
        courseApiClient = TestpressCourseApiClient(activity)
        viewModel = ViewModelProviders.of(this).get(ContentViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.testpress_activity_content_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews()
        parseIntentArguments()
        initializeListenters()
        initNavigationButtons()
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun bindViews() {
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

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun initializeListenters() {
        swipeRefresh.setOnRefreshListener {
            updateContent()
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun parseIntentArguments() {
        chapterId = arguments!!.getLong(CHAPTER_ID)
        contentId = arguments!!.getString(CONTENT_ID)
        position = arguments!!.getInt(POSITION);
        productSlug = arguments!!.getString(PRODUCT_SLUG)

        val onContentLoad = Observer<Content> {
            content = it!!
            contentId = content.id.toString()
            loadContent()

            if (isBookmarkEnabled) {
                initBookmarkFragment()
            }
        }

        if (contentId != null) {
            viewModel.getContent(contentId!!.toInt()).observe(this, onContentLoad)
            buttonLayout.visibility = View.GONE
        } else {
            pageNumber.text = String.format("%d/%d", position + 1, viewModel.getChapterContents(chapterId).size)
            viewModel.getContent(position, chapterId).observe(this, onContentLoad)
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun initNavigationButtons() {
        initPrevButton()
        initNextButton()
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun initPrevButton() {
        if (position < 1)
            return

        val previousPosition = position - 1;
        previousButton.setOnClickListener {
            startActivity(createIntent(previousPosition, chapterId, activity as AppCompatActivity, productSlug))
            finishActivity()
        }
        previousButton.visibility = View.VISIBLE
    }

    private fun finishActivity() {
        activity?.let {
            if (it is AppCompatActivity) {
                it.finish()
            }
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun initNextButton() {
        val contents = viewModel.getChapterContents(chapterId)
        if (contents.isEmpty())
            return

        if (position == (contents.size - 1)) {
            nextButton.text = getString(R.string.testpress_menu)
            nextButton.setOnClickListener {
                val pref = activity!!.getSharedPreferences(TESTPRESS_CONTENT_SHARED_PREFS, Context.MODE_PRIVATE)
                pref.edit().putBoolean(GO_TO_MENU, true).apply()
                finishActivity()
            }
            nextButton.visibility = View.VISIBLE
        } else {
            val nextPosition = position + 1
            if (!contents[nextPosition].isLocked) {
                nextButton.text = getString(R.string.testpress_next_content)
                nextButton.setOnClickListener {
                    startActivity(createIntent(nextPosition, chapterId, activity as AppCompatActivity, productSlug))
                    finishActivity()
                }
                nextButton.visibility = View.VISIBLE
            }
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    fun updateContent() {
        viewModel.loadContent(contentId!!.toInt()).observe(this,
                Observer { resource ->
                    swipeRefresh.isRefreshing = false
                    if (resource != null) {
                        when (resource.status) {
                            Status.SUCCESS -> {
                                content = resource.data!!
                                onUpdateContent(content)
                                loadContent()
                            }
                            Status.ERROR -> {
                                handleError(resource.exception!!)
                            }
                        }
                    }
                })
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun handleError(exception: TestpressException) {
        when {
            exception.isForbidden -> {
                setEmptyText(R.string.permission_denied,
                        R.string.testpress_no_permission,
                        R.drawable.ic_error_outline_black_18dp)

                retryButton.visibility = View.GONE
            }
            exception.isNetworkError -> {
                if (!swipeRefresh.isRefreshing) {
                    if (!toast.view.isShown) {
                        toast.show()
                    }
                    return
                }
                setEmptyText(R.string.testpress_network_error,
                        R.string.testpress_no_internet_try_again,
                        R.drawable.ic_error_outline_black_18dp)

                retryButton.setOnClickListener {
                    emptyContainer.visibility = View.GONE
                    viewModel.loadContent(content.id.toInt() ?: contentId!!.toInt())
                }
            }
            exception.isPageNotFound -> {
                setEmptyText(R.string.testpress_content_not_available,
                        R.string.testpress_content_not_available_description,
                        R.drawable.ic_error_outline_black_18dp)

                retryButton.visibility = View.GONE
            }
            else -> {
                setEmptyText(R.string.testpress_error_loading_contents,
                        R.string.testpress_some_thing_went_wrong_try_again,
                        R.drawable.ic_error_outline_black_18dp)

                retryButton.visibility = View.GONE
            }
        }
    }

    protected fun setEmptyText(title: Int, description: Int, left: Int) {
        emptyContainer.visibility = View.VISIBLE
        emptyTitleView.setText(title)
        emptyTitleView.setCompoundDrawablesWithIntrinsicBounds(left, 0, 0, 0)
        emptyDescView.setText(description)
        swipeRefresh.isRefreshing = false
        swipeRefresh.visibility = View.GONE
        retryButton.visibility = View.VISIBLE
    }

    private fun initBookmarkFragment() {
        val bookmarkFragment = BookmarkFragment()
        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.bookmark_fragment_layout, bookmarkFragment)
        transaction.commit()
    }

    override fun onBookmarkSuccess(bookmarkId: Long?) {
        viewModel.storeBookmarkId(bookmarkId)
    }

    override fun onDeleteBookmarkSuccess() {
        viewModel.storeBookmarkId(null)
    }

    internal abstract fun onUpdateContent(content: Content)
    internal abstract fun loadContent()

}