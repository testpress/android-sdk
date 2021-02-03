package `in`.testpress.exam.ui

import `in`.testpress.exam.R
import `in`.testpress.exam.api.TestpressExamApiClient
import `in`.testpress.exam.util.CommentsUtil
import `in`.testpress.exam.util.ImageUtils
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.fragment_comments.*

class CommentsFragment: DialogFragment() {

    private lateinit var apiClient: TestpressExamApiClient
    private lateinit var commentsUtil: CommentsUtil
    private lateinit var imageUtils: ImageUtils
    private lateinit var commentsUrl: String

    companion object {
        private const val COMMENTS_URL = "commentsUrl"
        fun getNewInstance(commentsUrl: String): CommentsFragment {
            val commentsFragment = CommentsFragment()
            val bundle = Bundle()
            bundle.putString(COMMENTS_URL, commentsUrl)
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
        return inflater.inflate(R.layout.fragment_comments, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        parseArguments()
        createCommentUtil()
        displayComments()
        setOnClickListeners()
    }

    override fun onResume() {
        super.onResume()
        imageUtils.permissionsUtils.onResume()
    }

    private fun parseArguments() {
        commentsUrl = arguments?.getString(COMMENTS_URL)?: ""
    }

    private fun createCommentUtil() {
        commentsUtil = CommentsUtil(
                this,
                loaderManager,
                commentsUrl,
                view
        )
    }

    private fun displayComments() {
        commentsUtil.displayComments()
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
            commentsUtil.uploadImage(result?.uri?.path)
        }
    }
}