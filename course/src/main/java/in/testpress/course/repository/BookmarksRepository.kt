package `in`.testpress.course.repository

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.core.TestpressSDKDatabase
import `in`.testpress.exam.api.TestpressExamApiClient
import `in`.testpress.models.greendao.*
import `in`.testpress.network.Resource
import `in`.testpress.network.TestpressApiClient
import `in`.testpress.v2_4.models.ApiResponse
import `in`.testpress.v2_4.models.BookmarksListResponse
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class BookmarksRepository(
    val context: Context,
    val apiClient: TestpressExamApiClient,
    val folder: String
) {

    private val TAG = "BookmarksRepository"

    private var queryParams: HashMap<String, Any> = hashMapOf()

    private val folders: HashMap<Any, BookmarkFolder> = hashMapOf()
    private val contentTypes: HashMap<Any, ContentType> = hashMapOf()
    private val reviewItems: HashMap<Any, ReviewItem> = hashMapOf()
    private val questions: HashMap<Any, ReviewQuestion> = hashMapOf()
    private val answers: HashMap<Any, ReviewAnswer> = hashMapOf()
    private val translations: HashMap<Any, ReviewQuestionTranslation> = hashMapOf()
    private val answerTranslations: HashMap<Any, ReviewAnswerTranslation> = hashMapOf()
    private val directions: HashMap<Any, Direction> = hashMapOf()
    private val directionTranslations: HashMap<Any, DirectionTranslation> = hashMapOf()
    private val subjects: HashMap<Any, Subject> = hashMapOf()
    private val contents: HashMap<Any, Content> = hashMapOf()
    private val attachments: HashMap<Any, Attachment> = hashMapOf()
    private val videos: HashMap<Any, Video> = hashMapOf()
    private val htmlContents: HashMap<Any, HtmlContent> = hashMapOf()

    private var _resourceBookmarks: MutableLiveData<Resource<List<Bookmark>>> = MutableLiveData()
    val resourceBookmarks: LiveData<Resource<List<Bookmark>>>
        get() = _resourceBookmarks

    private val bookmarkDao: BookmarkDao = TestpressSDKDatabase.getBookmarkDao(context)


    init {
        fetchBookmarksFromDB()
    }

    private fun fetchBookmarksFromDB() {
        val bookmarks = bookmarkDao.queryBuilder()
            .orderDesc(BookmarkDao.Properties.Id)
            .list()
        if (bookmarks.isNotEmpty()) {
            _resourceBookmarks.postValue(Resource.success(bookmarks))
        } else {
            _resourceBookmarks.value = Resource.loading(null)
        }
    }

    fun loadData() {
        queryParams.put(TestpressApiClient.ORDER, "-created")
        if (folder == "" || folder.isEmpty()) {
            queryParams.remove(TestpressApiClient.FOLDER)
        } else if (folder == BookmarkFolder.UNCATEGORIZED) {
            queryParams.put(TestpressApiClient.FOLDER, "null")
        } else {
            queryParams.put(TestpressApiClient.FOLDER, folder)
        }

        apiClient.getBookmarks(queryParams)
            .enqueue(object : TestpressCallback<ApiResponse<BookmarksListResponse>>() {
                override fun onSuccess(result: ApiResponse<BookmarksListResponse>) {
                    Log.d(TAG, "onSuccess: " + result.count)
                    deleteBeforeSaveItem()
                    _resourceBookmarks.postValue(Resource.success(getItems(result.results)))
                    saveBookmarksToDB(result.results)
                }

                override fun onException(exception: TestpressException?) {
                    fetchBookmarksFromDB()
                }
            })
    }

    private fun deleteBeforeSaveItem() {
        bookmarkDao.queryBuilder()
            .buildDelete()
            .executeDeleteWithoutDetachingEntities()
    }

    fun getItems(resultResponse: BookmarksListResponse): List<Bookmark> {
        for (folder in resultResponse.folders) {
            folders.put(folder.id, folder)
        }
        for (contentType in resultResponse.contentTypes) {
            contentTypes.put(contentType.id, contentType)
        }
        for (reviewItem in resultResponse.userSelectedAnswers) {
            reviewItems.put(reviewItem.id, reviewItem)
        }
        for (question in resultResponse.questions) {
            questions.put(question.id, question)
        }
        for (answer in resultResponse.answers) {
            answers.put(answer.id, answer)
        }
        for (question in resultResponse.translations) {
            translations.put(question.id, question)
        }
        for (answer in resultResponse.answerTranslations) {
            answerTranslations.put(answer.id, answer)
        }
        for (direction in resultResponse.directions) {
            directions.put(direction.id, direction)
        }
        for (directionTranslation in resultResponse.directionTranslations) {
            directionTranslations.put(directionTranslation.id, directionTranslation)
        }
        for (subject in resultResponse.subjects) {
            subjects.put(subject.id, subject)
        }
        for (content in resultResponse.chapterContents) {
            contents.put(content.id, content)
        }
        for (htmlContent in resultResponse.htmlContents) {
            htmlContents.put(htmlContent.id, htmlContent)
        }
        for (video in resultResponse.videos) {
            videos.put(video.id, video)
        }
        for (attachment in resultResponse.attachments) {
            attachments.put(attachment.id, attachment)
        }
        return resultResponse.bookmarks
    }

    fun saveBookmarksToDB(result: BookmarksListResponse) {
        deleteBeforeSaveItem()
        Bookmark.save(context, result.bookmarks, folder.isEmpty())
        TestpressSDKDatabase.getBookmarkFolderDao(context).insertOrReplaceInTx(result.folders)
        TestpressSDKDatabase.getContentTypeDao(context).insertOrReplaceInTx(result.contentTypes)

        ReviewItem.save(context, result.userSelectedAnswers)
        TestpressSDKDatabase.getReviewQuestionDao(context).insertOrReplaceInTx(result.questions)
        TestpressSDKDatabase.getReviewAnswerDao(context).insertOrReplaceInTx(result.answers)
        TestpressSDKDatabase.getReviewQuestionTranslationDao(context)
            .insertOrReplaceInTx(result.translations)
        TestpressSDKDatabase.getReviewAnswerTranslationDao(context)
            .insertOrReplaceInTx(result.answerTranslations)

        TestpressSDKDatabase.getDirectionDao(context).insertOrReplaceInTx(result.directions)
        TestpressSDKDatabase.getDirectionTranslationDao(context)
            .insertOrReplaceInTx(result.directionTranslations)
        TestpressSDKDatabase.getSubjectDao(context).insertOrReplaceInTx(result.subjects)

        Content.save(context, result.chapterContents)
        TestpressSDKDatabase.getHtmlContentDao(context).insertOrReplaceInTx(result.htmlContents)
        TestpressSDKDatabase.getVideoDao(context).insertOrReplaceInTx(result.videos)
        TestpressSDKDatabase.getAttachmentDao(context).insertOrReplaceInTx(result.attachments)
    }

}

