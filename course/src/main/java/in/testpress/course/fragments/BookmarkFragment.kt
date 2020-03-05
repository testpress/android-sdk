package `in`.testpress.course.fragments

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.R
import `in`.testpress.exam.api.TestpressExamApiClient
import `in`.testpress.exam.api.TestpressExamApiClient.BOOKMARK_FOLDERS_PATH
import `in`.testpress.exam.ui.FolderSpinnerAdapter
import `in`.testpress.models.greendao.Bookmark
import `in`.testpress.models.greendao.BookmarkFolder
import `in`.testpress.models.greendao.BookmarkFolder.UNCATEGORIZED
import `in`.testpress.ui.view.ClosableSpinner
import `in`.testpress.v2_4.models.ApiResponse
import `in`.testpress.v2_4.models.FolderListResponse
import android.content.Context
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.airbnb.lottie.LottieAnimationView
import java.io.IOException


class BookmarkFragment : Fragment() {
    private lateinit var animationView: LottieAnimationView
    private lateinit var bookmarkButtonText: TextView
    private lateinit var bookmarkButtonImage: ImageView
    private lateinit var bookmarkLayout: RelativeLayout
    private lateinit var bookmarkButtonLayout: LinearLayout

    private lateinit var bookmarkFolderSpinner: ClosableSpinner
    private lateinit var folderSpinnerAdapter: FolderSpinnerAdapter

    private lateinit var examApiClient: TestpressExamApiClient
    private lateinit var bookmarkListener: BookmarkListener

    private var isBookmarksEnabled: Boolean = false
    private val bookmarkFolders = arrayListOf<BookmarkFolder>();


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        examApiClient = TestpressExamApiClient(activity);
        isBookmarksEnabled = TestpressSdk.getTestpressSession(context!!)?.instituteSettings!!.isBookmarksEnabled
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (parentFragment != null) {
            onAttachToParentFragment(parentFragment)
        } else {
            bookmarkListener = context as BookmarkListener;
        }
    }

    private fun onAttachToParentFragment(fragment: Fragment?) {
        bookmarkListener = fragment as BookmarkListener;
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bookmark_fragment_layout, container, false);
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews()
        initializeListeners()
        initializeAdapters()
        showBookmarkStatus()
    }

    private fun bindViews() {
        animationView = view!!.findViewById(R.id.bookmark_loader);
        animationView.playAnimation()
        bookmarkLayout = view!!.findViewById(R.id.bookmark_layout);
        bookmarkButtonLayout = view!!.findViewById(R.id.bookmark_button_layout);
        bookmarkButtonImage = view!!.findViewById(R.id.bookmark_button_image);
        bookmarkButtonText = view!!.findViewById(R.id.bookmark_text);
        bookmarkButtonText.typeface = TestpressSdk.getRubikRegularFont(context!!);
        bookmarkFolderSpinner = view!!.findViewById(R.id.bookmark_folder_spinner);
    }

    private fun initializeListeners() {
        bookmarkButtonLayout.setOnClickListener {
            if (bookmarkListener.bookmarkId != null) {
                deleteBookmark(bookmarkListener.bookmarkId!!)
            } else {
                val baseURL = TestpressSdk.getTestpressSession(activity!!)?.instituteSettings?.baseUrl
                bookmarkFolders.clear()
                loadBookmarkFolders(baseURL + BOOKMARK_FOLDERS_PATH)
            }
        }

        bookmarkFolderSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, view: View?, position: Int, itemId: Long) {
                if (position != 0)
                    bookmark(folderSpinnerAdapter.getTag(position))
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }
    }

    private fun initializeAdapters() {
        folderSpinnerAdapter = FolderSpinnerAdapter(activity, resources) { folderName ->
            bookmarkFolderSpinner.dismissPopUp()
            bookmark(folderName)
        }
        folderSpinnerAdapter.hideSpinner(true)
        bookmarkFolderSpinner.adapter = folderSpinnerAdapter
    }

    private fun showBookmarkStatus() {
        if (isBookmarksEnabled) {
            if (bookmarkListener.bookmarkId != null) {
                bookmarkButtonText.text = getString(R.string.testpress_remove_bookmark)
                bookmarkButtonImage.setImageResource(R.drawable.ic_remove_bookmark)
            } else {
                bookmarkButtonText.text = getString(R.string.testpress_bookmark_this)
                bookmarkButtonImage.setImageResource(R.drawable.ic_bookmark)
            }
            bookmarkLayout.visibility = View.VISIBLE
        }
    }

    private fun bookmark(folderName: String) {
        showAnimation(true)
        examApiClient.bookmark(bookmarkListener.bookmarkContentId!!, folderName, "chaptercontent", "courses")
                .enqueue(object : TestpressCallback<Bookmark>() {
                    override fun onSuccess(bookmark: Bookmark?) {
                        bookmarkListener.onBookmarkSuccess(bookmark?.id)
                        showBookmarkStatus()
                        showAnimation(false)
                    }

                    override fun onException(exception: TestpressException) {
                        showAnimation(false)
                        handleException(exception)
                    }
                })
    }

    private fun deleteBookmark(bookmarkId: Long) {
        showAnimation(true)
        examApiClient.deleteBookmark(bookmarkId)
                .enqueue(object : TestpressCallback<Void>() {
                    override fun onSuccess(result: Void?) {
                        bookmarkListener.onDeleteBookmarkSuccess()
                        bookmarkFolderSpinner.setSelection(0)
                        showAnimation(false)
                        showBookmarkStatus()
                    }

                    override fun onException(exception: TestpressException) {
                        showAnimation(false)
                        handleException(exception)
                    }
                })
    }

    private fun showAnimation(show: Boolean) {
        if (show) {
            bookmarkButtonLayout.visibility = View.GONE
            animationView.visibility = View.VISIBLE
        } else {
            bookmarkButtonLayout.visibility = View.VISIBLE
            animationView.visibility = View.GONE
        }
    }

    private fun loadBookmarkFolders(url: String) {
        showAnimation(true)
        examApiClient.getBookmarkFolders(url)
                .enqueue(object : TestpressCallback<ApiResponse<FolderListResponse>>() {
                    override fun onSuccess(response: ApiResponse<FolderListResponse>) {
                        bookmarkFolders.addAll(response?.results?.folders ?: arrayListOf())
                        if (response.next != null) {
                            loadBookmarkFolders(response.next)
                        } else {
                            addFoldersToSpinner(bookmarkFolders)
                            showAnimation(false)
                            bookmarkFolderSpinner.performClick()
                        }
                    }

                    override fun onException(exception: TestpressException) {
                        showAnimation(false)
                        handleException(exception)
                    }
                })
    }

    private fun handleException(exception: TestpressException) {
        if (exception.isUnauthenticated) {
            Snackbar.make(bookmarkLayout, R.string.testpress_authentication_failed,
                    Snackbar.LENGTH_SHORT).show();
        } else if (exception.cause is IOException) {
            Snackbar.make(bookmarkLayout, R.string.testpress_no_internet_connection,
                    Snackbar.LENGTH_SHORT).show();
        } else if (exception.isClientError) {
            Snackbar.make(bookmarkLayout, R.string.testpress_folder_name_not_allowed,
                    Snackbar.LENGTH_SHORT).show();
        } else {
            Snackbar.make(bookmarkLayout, R.string.testpress_network_error,
                    Snackbar.LENGTH_SHORT).show();
        }
    }

    private fun addFoldersToSpinner(bookmarkFolders: List<BookmarkFolder>) {
        folderSpinnerAdapter.clear()
        folderSpinnerAdapter.addHeader("-- Select Folder --")
        for (folder in bookmarkFolders) {
            folderSpinnerAdapter.addItem(folder.name, folder.name, false, 0)
        }
        folderSpinnerAdapter.addItem(null, UNCATEGORIZED, false, 0)
        folderSpinnerAdapter.notifyDataSetChanged()
    }

}

interface BookmarkListener {
    val bookmarkId: Long?
    val bookmarkContentId: Long?
    fun onBookmarkSuccess(bookmarkId: Long?)
    fun onDeleteBookmarkSuccess()
}