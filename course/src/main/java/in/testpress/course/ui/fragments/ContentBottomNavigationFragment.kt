package `in`.testpress.course.ui.fragments

import `in`.testpress.core.TestpressSDKDatabase
import `in`.testpress.course.R
import `in`.testpress.course.TestpressCourse.PRODUCT_SLUG
import `in`.testpress.course.enums.Status
import `in`.testpress.course.network.TestpressCourseApiClient
import `in`.testpress.course.repository.ContentRepository
import `in`.testpress.course.ui.ContentActivity.*
import `in`.testpress.course.ui.view_models.ContentViewModel
import `in`.testpress.models.greendao.Content
import `in`.testpress.models.greendao.ContentDao
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.annotation.VisibleForTesting
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView


class ContentBottomNavigationFragment : Fragment() {
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    lateinit var previousButton: Button
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    lateinit var nextButton: Button
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    lateinit var pageNumber: TextView

    private var contentId: Int = -1
    private var productSlug: String? = null
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    lateinit var content: Content
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    lateinit var viewModel: ContentViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val courseApiClient = TestpressCourseApiClient(context)
        val contentDao = TestpressSDKDatabase.getContentDao(context)
        val contentRepository = ContentRepository(contentDao, courseApiClient)
        val viewModelFactory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ContentViewModel(contentRepository) as T
            }
        }
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(ContentViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.content_bottom_navigation, container, false);
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews()
        parseArguments()
        initNavigationButtons()
    }

    private fun bindViews() {
        previousButton = view!!.findViewById(R.id.previous)
        nextButton = view!!.findViewById(R.id.next)
        pageNumber = view!!.findViewById(R.id.page_number)
    }

    private fun parseArguments() {
        contentId = arguments!!.getInt(CONTENT_ID)
        productSlug = arguments!!.getString(PRODUCT_SLUG)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun initNavigationButtons() {
        viewModel.getContent(contentId).observe(this, Observer { resource ->
            when (resource?.status) {
                Status.SUCCESS -> {
                    content = resource.data!!
                    val chapterContents = viewModel.getChapterContents(content.chapterId)
                    val position = chapterContents.indexOf(content)
                    initNextButton(position)
                    initPrevButton(position)
                    pageNumber.text = String.format("%d/%d", position + 1, chapterContents.size)
                }
            }
        })
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun initPrevButton(position: Int) {
        if (position < 1)
            return

        val previousContentPosition = position - 1
        previousButton.setOnClickListener {
            startActivity(createIntent(previousContentPosition, content.chapterId, activity as AppCompatActivity, productSlug))
            finishActivity()
        }
        previousButton.visibility = View.VISIBLE
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun initNextButton(position: Int) {
        val contents = viewModel.getChapterContents(content.chapterId)

        if (contents.isEmpty())
            return

        if (hasNextContent(position, contents)) {
            openNextContentOnClick(position, contents)
        } else {
            openMenuOnClick()
        }
    }

    private fun hasNextContent(position: Int, contents: List<Content>): Boolean {
        return position < (contents.size - 1)
    }

    private fun openMenuOnClick() {
        nextButton.text = getString(R.string.testpress_menu)
        nextButton.setOnClickListener {
            val pref = activity!!.getSharedPreferences(TESTPRESS_CONTENT_SHARED_PREFS, Context.MODE_PRIVATE)
            pref.edit().putBoolean(GO_TO_MENU, true).apply()
            finishActivity()
        }
        nextButton.visibility = View.VISIBLE
    }

    private fun openNextContentOnClick(position: Int, contents: List<Content>) {
        val nextPosition = position + 1
        if (!contents[nextPosition].isLocked) {
            nextButton.text = getString(R.string.testpress_next_content)
            nextButton.setOnClickListener {
                startActivity(createIntent(nextPosition, content.chapterId, activity as AppCompatActivity, productSlug))
                finishActivity()
            }
            nextButton.visibility = View.VISIBLE
        }
    }

    private fun finishActivity() {
        activity?.let {
            if (it is AppCompatActivity) {
                it.finish()
            }
        }
    }
}