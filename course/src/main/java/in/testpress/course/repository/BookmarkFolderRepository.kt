package `in`.testpress.course.repository

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.core.TestpressSDKDatabase
import `in`.testpress.exam.api.TestpressExamApiClient
import `in`.testpress.models.greendao.BookmarkDao
import `in`.testpress.models.greendao.BookmarkFolder
import `in`.testpress.models.greendao.BookmarkFolderDao
import `in`.testpress.network.NetworkBoundResource
import `in`.testpress.network.Resource
import `in`.testpress.network.RetrofitCall
import `in`.testpress.v2_4.models.ApiResponse
import `in`.testpress.v2_4.models.FolderListResponse
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

private const val TAG = "BookmarkFolderRepository"

class BookmarkFolderRepository(
    val context: Context,
    val apiClient: TestpressExamApiClient,
) {

    private var _resourceBookmarkFolders: MutableLiveData<Resource<List<BookmarkFolder>>> =
        MutableLiveData()
    val resourceBookmarkFolders: LiveData<Resource<List<BookmarkFolder>>>
        get() = _resourceBookmarkFolders

    private var _updateBookmarkFolders: MutableLiveData<Resource<BookmarkFolder>> =
        MutableLiveData()
    val updateBookmarkFolders: LiveData<Resource<BookmarkFolder>>
        get() = _updateBookmarkFolders

    private var _deleteBookmarkFolders: MutableLiveData<Resource<Void>> =
        MutableLiveData()
    val deleteBookmarkFolders: LiveData<Resource<Void>>
        get() = _deleteBookmarkFolders

    private val bookmarkDao: BookmarkDao = TestpressSDKDatabase.getBookmarkDao(context)
    private val bookmarkFolderDao: BookmarkFolderDao =
        TestpressSDKDatabase.getBookmarkFolderDao(context)

    init {
        fetchBookmarksFromDB()
    }

    private fun fetchBookmarksFromDB() {
        val bookmarkFolder = bookmarkFolderDao.queryBuilder()
            .list()
        if (bookmarkFolder.isNotEmpty()) {
            _resourceBookmarkFolders.value = Resource.success(bookmarkFolder)
        } else {
            _resourceBookmarkFolders.value = Resource.loading(null)
        }
    }

    fun loadBookmarkFolders(url: String) {
        apiClient.getBookmarkFolders(url)
            .enqueue(object : TestpressCallback<ApiResponse<FolderListResponse>>() {
                override fun onSuccess(result: ApiResponse<FolderListResponse>) {
                    _resourceBookmarkFolders.value=(Resource.success(result.results.folders))
                    if (result.next != null) {
                        loadBookmarkFolders(result.next)
                    } else {
                        bookmarkFolderDao.deleteAll()
                        bookmarkFolderDao.insertOrReplaceInTx(_resourceBookmarkFolders.value?.data)
                    }
                }

                override fun onException(exception: TestpressException) {
                    fetchBookmarksFromDB()
                    _resourceBookmarkFolders.postValue(Resource.error(exception,null))
                }

            })
    }

    fun updateBookmarkFolder(folderId: Long, folderName: String) {
        apiClient.updateBookmarkFolder(folderId, folderName)
            .enqueue(object : TestpressCallback<BookmarkFolder>() {
                override fun onSuccess(result: BookmarkFolder) {
                    bookmarkFolderDao.updateInTx(result)
                    val updatedBookmarkFolder = bookmarkFolderDao.queryBuilder()
                        .where(BookmarkFolderDao.Properties.Id.eq(result.id))
                        .list()
                        .get(0)
                    _updateBookmarkFolders.postValue(Resource.success(updatedBookmarkFolder))
                }

                override fun onException(exception: TestpressException) {
                    _updateBookmarkFolders.postValue(Resource.error(exception,null))
                }
            })
    }

    fun deleteFolder(folderId: Long) {
        apiClient.deleteBookmarkFolder(folderId).enqueue(object : TestpressCallback<Void>() {
            override fun onSuccess(result: Void?) {
                bookmarkFolderDao.deleteByKeyInTx(folderId)
                val bookmarks = bookmarkDao.queryBuilder()
                    .where(BookmarkDao.Properties.FolderId.eq(folderId)).list()

                if (bookmarks.isNotEmpty()) {
                    for (bookmark in bookmarks) {
                        bookmark.folderId = null
                        bookmark.folder = null
                        bookmark.loadedInRespectiveFolder = false
                        bookmarkDao.insertOrReplaceInTx(bookmark)
                    }
                }
                _deleteBookmarkFolders.value=(Resource.success(null))
            }

            override fun onException(exception: TestpressException) {
                _deleteBookmarkFolders.postValue(Resource.error(exception,null))
            }
        })
    }

}