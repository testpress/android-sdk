package `in`.testpress.exam.ui

import `in`.testpress.exam.R
import `in`.testpress.exam.api.TestpressExamApiClient
import `in`.testpress.exam.util.CommentsUtil
import `in`.testpress.exam.util.ImageUtils
import `in`.testpress.models.greendao.ReviewItem
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
    private var rootLayout: View? = null
    private lateinit var imageUtils: ImageUtils

    companion object {
        private var reviewItem: ReviewItem? = null
        private var isReviewQuestion: Boolean = true
        fun getNewInstance(review: ReviewItem, isFromReviewQuestion: Boolean): CommentsFragment {
            reviewItem = review
            isReviewQuestion = isFromReviewQuestion
            return CommentsFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        apiClient = TestpressExamApiClient(context)
        imageUtils = ImageUtils(rootLayout, this)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogTheme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootLayout = inflater.inflate(R.layout.fragment_comments, container, false)
        return rootLayout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        displayComments()
        setOnClickListeners()
    }

    override fun onResume() {
        super.onResume()
        imageUtils.permissionsUtils.onResume()
    }

    private fun displayComments() {
        if (commentsUtil == null) {
            if (isReviewQuestion) {
                createReviewCommentUtil()
            } else {
                createBookmarkCommentUtil()
            }
            commentsUtil!!.displayComments()
        }
    }

    private fun createReviewCommentUtil() {
        commentsUtil = CommentsUtil(
                this,
                loaderManager,
                CommentsUtil.getQuestionCommentsUrl(apiClient, reviewItem),
                rootLayout,
                (activity as ReviewQuestionsActivity?)!!.buttonLayout
        )
    }

    private fun createBookmarkCommentUtil() {
        commentsUtil = CommentsUtil(
                this,
                loaderManager,
                CommentsUtil.getQuestionCommentsUrl(apiClient, reviewItem),
                rootLayout,
                (activity as BookmarksActivity?)!!.buttonLayout
        )
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