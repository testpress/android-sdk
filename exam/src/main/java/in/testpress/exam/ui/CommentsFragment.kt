package `in`.testpress.exam.ui

import `in`.testpress.core.TestpressSDKDatabase
import `in`.testpress.exam.R
import `in`.testpress.exam.api.TestpressExamApiClient
import `in`.testpress.exam.util.CommentsUtil
import `in`.testpress.exam.util.ImageUtils
import `in`.testpress.models.greendao.BookmarkDao
import `in`.testpress.models.greendao.ReviewItem
import `in`.testpress.models.greendao.ReviewItemDao
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.fragment_comments.*

class CommentsFragment: DialogFragment() {

    private var apiClient: TestpressExamApiClient? = null
    private var commentsUtil: CommentsUtil? = null
    private lateinit var imageUtils: ImageUtils
    private var reviewItem: ReviewItem? = null
    private var reviewItemId: Long? = null
    private var bookmarkId: Long? = null
    private var isReviewQuestion: Boolean = true

    companion object {
        private const val IS_REVIEW_QUESTION = "isReviewQuestion"
        fun getNewInstance(itemId: Long, isFromReviewQuestion: Boolean): CommentsFragment {
            val commentsFragment = CommentsFragment()
            val bundle = Bundle()
            bundle.putBoolean(IS_REVIEW_QUESTION, isFromReviewQuestion)
            bundle.putLong(ReviewQuestionsFragment.PARAM_REVIEW_ITEM_ID, itemId)
            commentsFragment.arguments = bundle
            return commentsFragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        apiClient = TestpressExamApiClient(context)
        imageUtils = ImageUtils(view, this)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogTheme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        getDataFromBundle()
        return inflater.inflate(R.layout.fragment_comments, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getReviewItem()
        createCommentUtil()
        displayComments()
        setOnClickListeners()
    }

    override fun onResume() {
        super.onResume()
        imageUtils.permissionsUtils.onResume()
    }

    private fun getDataFromBundle() {
        isReviewQuestion = arguments!!.getBoolean(IS_REVIEW_QUESTION)
        if (isReviewQuestion) {
            reviewItemId = arguments!!.getLong(ReviewQuestionsFragment.PARAM_REVIEW_ITEM_ID)
        } else {
            bookmarkId = arguments!!.getLong(ReviewQuestionsFragment.PARAM_REVIEW_ITEM_ID)
        }
    }

    private fun getReviewItem() {
        reviewItem = if (isReviewQuestion) {
            getReviewItemFromReview()
        } else {
            getReviewItemFromBookmark()
        }

    }

    private fun getReviewItemFromReview(): ReviewItem? {
        val reviewItemDao = TestpressSDKDatabase.getReviewItemDao(context)
        val reviewItems = reviewItemDao!!.queryBuilder()
                .where(ReviewItemDao.Properties.Id.eq(reviewItemId)).list()
        return reviewItems[0]
    }

    private fun getReviewItemFromBookmark(): ReviewItem? {
        val bookmarkDao = TestpressSDKDatabase.getBookmarkDao(context)
        val bookmark = bookmarkDao.queryBuilder().where(BookmarkDao.Properties.Id.eq(bookmarkId))
                .list()[0]
        return bookmark.bookmarkedObject as ReviewItem
    }

    private fun createCommentUtil() {
        if (commentsUtil == null) {
            if (isReviewQuestion) {
                createReviewCommentUtil()
            } else {
                createBookmarkCommentUtil()
            }
        }
    }

    private fun createReviewCommentUtil() {
        commentsUtil = CommentsUtil(
                this,
                loaderManager,
                CommentsUtil.getQuestionCommentsUrl(apiClient, reviewItem),
                view,
                (activity as ReviewQuestionsActivity?)!!.buttonLayout
        )
    }

    private fun createBookmarkCommentUtil() {
        commentsUtil = CommentsUtil(
                this,
                loaderManager,
                CommentsUtil.getQuestionCommentsUrl(apiClient, reviewItem),
                view,
                (activity as BookmarksActivity?)!!.buttonLayout
        )
    }

    private fun displayComments() {
        commentsUtil!!.displayComments()
    }

    private fun setOnClickListeners() {
        closeButton.setOnClickListener {
            dismiss()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>,
                                            grantResults: IntArray) {
        imageUtils.permissionsUtils.onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        imageUtils.onActivityResult(requestCode, resultCode, data) { result ->
            commentsUtil!!.uploadImage(result?.uri?.path)
        }
    }
}