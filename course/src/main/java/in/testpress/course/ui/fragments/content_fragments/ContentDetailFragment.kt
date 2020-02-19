package `in`.testpress.course.ui.fragments.content_fragments

import `in`.testpress.core.TestpressException
import `in`.testpress.course.R
import `in`.testpress.course.enums.Status
import `in`.testpress.course.ui.ContentActivity.*
import `in`.testpress.course.ui.view_models.ContentViewModel
import `in`.testpress.models.greendao.Content
import `in`.testpress.models.greendao.Content.*
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView


class ContentDetailFragment : Fragment() {
    private lateinit var emptyTitleView: TextView
    private lateinit var emptyDescView: TextView
    private lateinit var emptyContainer: LinearLayout
    private lateinit var retryButton: Button
    private lateinit var progressBar: ProgressBar

    private var chapterId: Long = -1
    private var contentId: String? = null
    private var position: Int = -1
    private lateinit var viewModel: ContentViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(ContentViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.empty_view_with_progress_bar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        emptyTitleView = view.findViewById(R.id.empty_title)
        emptyDescView = view.findViewById(R.id.empty_description)
        emptyContainer = view.findViewById(R.id.empty_container)
        retryButton = view.findViewById(R.id.retry_button)
        progressBar = view.findViewById(R.id.pb_loading)
        parseArguments()
        loadContentAndSwitchFragment()
    }

    private fun loadContentAndSwitchFragment() {
        if (contentId != null) {
            val content = viewModel.getContentFromDb(contentId!!.toInt())
            if (content != null) {
                switchFragment(content)
            } else {
                viewModel.loadContent(contentId!!.toInt()).observe(this, Observer { resource ->
                    when (resource?.status) {
                        Status.SUCCESS -> switchFragment(resource.data!!)
                        Status.ERROR -> {
                            progressBar.visibility = View.GONE
                            handleError(resource.exception!!)
                        }
                    }
                })
            }
        } else {
            viewModel.getContent(position, chapterId).observe(this, Observer {
                switchFragment(it!!)
            })
        }
    }

    private fun switchFragment(content: Content) {
        lateinit var fragment: BaseContentDetailFragment
        when (content.contentType) {
            HTML_TYPE -> fragment = HtmlContentFragment()
            VIDEO_TYPE -> fragment = VideoContentFragment()
            EXAM_TYPE -> fragment = ExamContentFragment()
            QUIZ_TYPE -> fragment = ExamContentFragment()
            ATTACHMENT_TYPE -> fragment = AttachmentContentFragment()
            NOTES_TYPE -> fragment = HtmlContentFragment()
        }
        fragment.arguments = arguments
        fragmentManager?.beginTransaction()?.replace(id, fragment)?.commit()
    }

    private fun parseArguments() {
        chapterId = arguments!!.getLong(CHAPTER_ID)
        contentId = arguments!!.getString(CONTENT_ID)
        position = arguments!!.getInt(POSITION);
    }

    private fun handleError(exception: TestpressException) {
        println("Exception : ${exception}")
        when {
            exception.isForbidden -> {
                println("Forbidden Exception")
                setEmptyText(R.string.permission_denied,
                        R.string.testpress_no_permission,
                        R.drawable.ic_error_outline_black_18dp)

                retryButton.visibility = View.GONE
            }
            exception.isNetworkError -> {
                setEmptyText(R.string.testpress_network_error,
                        R.string.testpress_no_internet_try_again,
                        R.drawable.ic_error_outline_black_18dp)

                retryButton.setOnClickListener {
                    emptyContainer.visibility = View.GONE
                    loadContentAndSwitchFragment()
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

    private fun setEmptyText(title: Int, description: Int, left: Int) {
        emptyContainer.visibility = View.VISIBLE
        emptyTitleView.setText(title)
        emptyTitleView.setCompoundDrawablesWithIntrinsicBounds(left, 0, 0, 0)
        emptyDescView.setText(description)
        retryButton.visibility = View.VISIBLE
    }

    public fun getContent(): Content? {
        if (contentId != null) {
            return viewModel.getContentFromDb(contentId!!.toInt())
        } else {
            val contents = viewModel.getChapterContents(chapterId)
            return contents[position]
        }
    }
}