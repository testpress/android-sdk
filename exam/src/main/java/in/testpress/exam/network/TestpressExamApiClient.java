package in.testpress.exam.network;

import android.content.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.testpress.core.TestpressSdk;
import in.testpress.exam.models.AttemptItem;
import in.testpress.exam.models.Category;
import in.testpress.exam.models.Comment;
import in.testpress.exam.models.Subject;
import in.testpress.exam.models.Vote;
import in.testpress.models.TestpressApiResponse;
import in.testpress.models.greendao.Attempt;
import in.testpress.models.greendao.Bookmark;
import in.testpress.models.greendao.BookmarkFolder;
import in.testpress.models.greendao.ContentType;
import in.testpress.models.greendao.CourseAttempt;
import in.testpress.models.greendao.Exam;
import in.testpress.models.greendao.ReviewItem;
import in.testpress.v2_4.models.ApiResponse;
import in.testpress.v2_4.models.BookmarksListResponse;
import in.testpress.v2_4.models.FolderListResponse;
import in.testpress.network.RetrofitCall;
import in.testpress.network.TestpressApiClient;

public class TestpressExamApiClient extends TestpressApiClient {

    public static final String VOTES_PATH =  "/api/v2.3/votes/";
    /**
     * Exams List URL
     */
    public static final String EXAMS_LIST_PATH =  "/api/v2.2/exams/";

    public static final String QUESTIONS_PATH =  "api/v2.2/questions/";
    public static final String COMMENTS_PATH =  "/comments/";

    public static final String ACCESS_CODES_PATH =  "/api/v2.2/access_codes/";
    public static final String EXAMS_PATH =  "/exams/";

    /**
     * Categories URL
     */
    public static final String CATEGORIES_PATH =  "/api/v2.2/course_categories/";

    public static final String MAIL_PDF_PATH =  "pdf/";
    public static final String MAIL_PDF_QUESTIONS_PATH =  "pdf-questions/";

    public static final String BOOKMARK_FOLDERS_PATH =  "/api/v2.4/folders/";
    public static final String BOOKMARKS_PATH =  "/api/v2.4/bookmarks/";

    /**
     * Overall subject analytics path
     */
    public static final String SUBJECT_ANALYTICS_PATH = "api/v2.2/analytics/";

    /**
     * Subject analytics path for a particular attempt
     */
    public static final String ATTEMPT_SUBJECT_ANALYTICS_PATH =  "review/subjects/";

    /**
     * End Exam URL
     */
    public static final String END_EXAM_PATH =  "/end/";

    /**
     * Query Params
     */
    public static final String SEARCH_QUERY = "q";
    public static final String STATE = "state";
    public static final String PAGE = "page";
    public static final String PARENT = "parent";
    public static final String CATEGORY = "course_slug";

    public static final String STATE_PAUSED = "Running";

    public TestpressExamApiClient(final Context context) {
        super(context, checkTestpressSessionIsNull(TestpressSdk.getTestpressSession(context)));
    }
    
    public ExamService getExamService() {
        return retrofit.create(ExamService.class);
    }

    private BookmarkService getBookmarkService() {
        return retrofit.create(BookmarkService.class);
    }

    public RetrofitCall<TestpressApiResponse<Exam>> getExams(Map<String, Object> queryParams) {
        return getExamService().getExams(queryParams);
    }

    public RetrofitCall<TestpressApiResponse<Exam>> getExams(String accessCode,
                                                             Map<String, Object> queryParams) {
        return getExamService().getExams(accessCode, queryParams);
    }

    public RetrofitCall<Exam> getExam(String examSlug) {
        return getExamService().getExam(examSlug);
    }

    public RetrofitCall<Void> mailQuestionsPdf(String mailPdfUrlFrag) {
        return getExamService().mailQuestionsPdf(mailPdfUrlFrag);
    }

    public RetrofitCall<Void> mailExplanationsPdf(String mailPdfUrlFrag) {
        return getExamService().mailExplanationsPdf(mailPdfUrlFrag);
    }

    public RetrofitCall<Attempt> createAttempt(String attemptsUrlFrag, Map<String, Object> option) {
        return getExamService().createAttempt(attemptsUrlFrag, option);
    }

    public RetrofitCall<CourseAttempt> createContentAttempt(String attemptUrl) {
        return getExamService().createContentAttempt(attemptUrl);
    }

    public RetrofitCall<Attempt> startAttempt(String startAttemptUrlFrag) {
        return getExamService().startAttempt(startAttemptUrlFrag);
    }

    public RetrofitCall<Attempt> endAttempt(String endAttemptUrlFrag) {
        return getExamService().endExam(endAttemptUrlFrag);
    }

    public RetrofitCall<CourseAttempt> endContentAttempt(String endAttemptUrlFrag) {
        return getExamService().endContentAttempt(endAttemptUrlFrag);
    }

    public RetrofitCall<TestpressApiResponse<AttemptItem>> getQuestions(
            String questionsUrlFrag, Map<String, Object> queryParams) {
        return getExamService().getQuestions(questionsUrlFrag, queryParams);
    }

    public RetrofitCall<TestpressApiResponse<Attempt>> getAttempts(String urlFrag,
                                                                   Map<String, Object> queryParams) {
        return getExamService().getAttempts(urlFrag, queryParams);
    }

    public RetrofitCall<TestpressApiResponse<CourseAttempt>> getContentAttempts(String url) {
        return getExamService().getContentAttempts(url);
    }

    public RetrofitCall<TestpressApiResponse<ReviewItem>> getReviewItems(
            String urlFrag, Map<String, Object> queryParams) {
        return getExamService().getReviewItems(urlFrag, queryParams);
    }

    public RetrofitCall<AttemptItem> postAnswer(String answerUrlFrag, List<Integer> savedAnswers,
                                                Boolean review) {
        HashMap<String, Object> answer = new HashMap<String, Object>();
        answer.put("selected_answers", savedAnswers);
        answer.put("review", review);
        return getExamService().postAnswer(answerUrlFrag, answer);
    }

    public RetrofitCall<Attempt> heartbeat(String heartbeatUrlFrag) {
        return getExamService().heartbeat(heartbeatUrlFrag);
    }

    public RetrofitCall<Attempt> endExam(String endExamUrlFrag) {
        return getExamService().endExam(endExamUrlFrag);
    }

    public RetrofitCall<TestpressApiResponse<Category>> getCategories(Map<String, Object> queryParams) {
        return getExamService().getCategories(queryParams);
    }

    public RetrofitCall<TestpressApiResponse<Subject>> getSubjects(String urlFrag,
                                                                   Map<String, Object> queryParams) {
        return getExamService().getSubjects(urlFrag, queryParams);
    }

    public RetrofitCall<TestpressApiResponse<Comment>> getComments(String urlFrag,
                                                                   Map<String, Object> queryParams) {
        return getExamService().getComments(urlFrag, queryParams);
    }

    public RetrofitCall<Comment> postComment(String urlFrag, String comment) {
        HashMap<String, String> params = new HashMap<String, String>();
        //noinspection deprecation
        params.put("comment", comment);
        return getExamService().postComment(urlFrag, params);
    }

    public RetrofitCall<Vote<Comment>> voteComment(Comment comment, int typeOfVote) {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("content_object", comment);
        params.put("type_of_vote", typeOfVote);
        return getExamService().voteComment(params);
    }

    public RetrofitCall<String> deleteCommentVote(Comment comment) {
        return getExamService().deleteCommentVote(comment.getVoteId());
    }

    public RetrofitCall<Vote<Comment>> updateCommentVote(Comment comment, int typeOfVote) {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("content_object", comment);
        params.put("type_of_vote", typeOfVote);
        return getExamService().updateCommentVote(comment.getVoteId(), params);
    }

    public RetrofitCall<ApiResponse<FolderListResponse>> getBookmarkFolders(String bookmarkFolderUrl) {
        return getBookmarkService().getBookmarkFolders(bookmarkFolderUrl);
    }

    public RetrofitCall<BookmarkFolder> updateBookmarkFolder(long folderId, String folderName) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("name", folderName);
        return getBookmarkService().updateFolder(folderId, params);
    }

    public RetrofitCall<Void> deleteBookmarkFolder(long folderId) {
        return getBookmarkService().deleteFolder(folderId);
    }

    public RetrofitCall<Bookmark> bookmark(long objectId, String folder, String model,
                                           String appLabel) {

        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("object_id", objectId);
        params.put("folder", folder);
        params.put("content_type", new ContentType(model, appLabel));
        return getBookmarkService().bookmark(params);
    }

    public RetrofitCall<Bookmark> updateBookmark(long bookmarkId, String folder) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("folder", folder);
        return getBookmarkService().updateBookmark(bookmarkId, params);
    }

    public RetrofitCall<Bookmark> undoBookmarkDelete(long bookmarkId, long objectId, String folder) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("object_id", objectId);
        params.put("active", true);
        params.put("folder", folder);
        return getBookmarkService().updateBookmark(bookmarkId, params);
    }

    public RetrofitCall<Void> deleteBookmark(long bookmarkId) {
        return getBookmarkService().deleteBookmark(bookmarkId);
    }

    public RetrofitCall<ApiResponse<BookmarksListResponse>> getBookmarks(Map<String, Object> queryParams) {
        return getBookmarkService().getBookmarks(queryParams);
    }
}
