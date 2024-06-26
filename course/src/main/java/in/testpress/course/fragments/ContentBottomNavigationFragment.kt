package `in`.testpress.course.fragments

import `in`.testpress.core.TestpressSdk.ACTION_PRESSED_HOME
import `in`.testpress.course.R
import `in`.testpress.course.TestpressCourse
import `in`.testpress.course.TestpressCourse.CHAPTER_URL
import `in`.testpress.course.TestpressCourse.PRODUCT_SLUG
import `in`.testpress.course.di.InjectorUtils
import `in`.testpress.course.domain.DomainContent
import `in`.testpress.enums.Status
import `in`.testpress.course.ui.ContentActivity.CONTENT_ID
import `in`.testpress.course.ui.ContentActivity.GO_TO_MENU
import `in`.testpress.course.ui.ContentActivity.TESTPRESS_CONTENT_SHARED_PREFS
import `in`.testpress.course.ui.ContentActivity.createIntent
import `in`.testpress.course.viewmodels.ContentViewModel
import android.app.Activity.RESULT_CANCELED
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ContentBottomNavigationFragment : Fragment() {
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    lateinit var previousButton: Button
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    lateinit var nextButton: Button
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    lateinit var pageNumber: TextView
    private lateinit var bottomLayout: LinearLayout

    private var contentId: Long = -1
    private var productSlug: String? = null
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    lateinit var content: DomainContent
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    lateinit var viewModel: ContentViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.content_bottom_navigation, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val contentType = requireArguments().getString(TestpressCourse.CONTENT_TYPE)
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ContentViewModel(
                    InjectorUtils.getContentRepository(contentType!!, context!!)
                ) as T
            }
        }).get(ContentViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews()
        parseArguments()

        if (!isProductPreview()) {
            initializeAndShowNavigationButtons()
        }
    }

    private fun isProductPreview(): Boolean {
        return productSlug != null
    }

    private fun bindViews() {
        previousButton = view!!.findViewById(R.id.previous)
        nextButton = view!!.findViewById(R.id.next)
        pageNumber = view!!.findViewById(R.id.page_number)
        bottomLayout = view!!.findViewById(R.id.bottom_layout)
    }

    private fun parseArguments() {
        contentId = arguments!!.getLong(CONTENT_ID)
        productSlug = arguments!!.getString(PRODUCT_SLUG)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun initializeAndShowNavigationButtons() {
        bottomLayout.visibility = View.VISIBLE
        val contentsFromChapterObserver = Observer<List<DomainContent>> { contents ->
            val unLockedContents = contents.filter { content ->
                content.isLocked == false && content.isScheduled == false
            }.sortedBy {
                it.order
            }
            val position = unLockedContents.indexOf(content)
            initNextButton(position, unLockedContents)
            initPrevButton(position, unLockedContents)
            pageNumber.text = String.format("%d/%d", contents.indexOf(content) + 1, contents.size)
        }

        viewModel.getContent(contentId).observe(viewLifecycleOwner, Observer { resource ->
            when (resource?.status) {
                Status.SUCCESS -> {
                    content = resource.data!!
                    viewModel.getContentsForChapter(content.chapterId!!)?.observe(
                            viewLifecycleOwner, contentsFromChapterObserver)
                }
                else -> {}
            }
        })
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun initPrevButton(position: Int, contents: List<DomainContent>) {
        if (position < 1)
            return

        previousButton.setOnClickListener {
            startActivity(createIntent(contents[position-1].id, activity, productSlug))
            finishActivity()
        }
        previousButton.visibility = View.VISIBLE
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun initNextButton(position: Int, contents: List<DomainContent>) {
        if (hasNextContent(position, contents)) {
            nextShouldOpenNextContentOnClick(position, contents)
        } else {
            nextShouldOpenMenuOnClick()
        }
    }

    private fun hasNextContent(position: Int, contents: List<DomainContent>): Boolean {
        return position < (contents.size - 1)
    }

    private fun nextShouldOpenMenuOnClick() {
        nextButton.text = getString(R.string.testpress_menu)
        nextButton.setOnClickListener {
            val pref = activity!!.getSharedPreferences(TESTPRESS_CONTENT_SHARED_PREFS, Context.MODE_PRIVATE)
            pref.edit().putBoolean(GO_TO_MENU, true).apply()
            finishActivity()
        }
        nextButton.visibility = View.VISIBLE
    }

    private fun nextShouldOpenNextContentOnClick(position: Int, contents: List<DomainContent>) {
        val nextPosition = position + 1
        if (!contents[nextPosition].isLocked!!) {
            nextButton.text = getString(R.string.testpress_next_content)
            nextButton.setOnClickListener {
                startActivity(createIntent(contents[nextPosition].id, activity, productSlug))
                finishActivity()
            }
            nextButton.visibility = View.VISIBLE
        }
    }

    private fun finishActivity() {
        activity?.let {
            if (it is AppCompatActivity) {
                if (it.getCallingActivity() != null) {
                    val intent = Intent()
                    intent.putExtra(ACTION_PRESSED_HOME, true)
                    intent.putExtra(CHAPTER_URL, content.chapterUrl)
                    it.setResult(RESULT_CANCELED, intent)
                    it.finish()
                    return
                }
                it.finish()
            }
        }
    }
}