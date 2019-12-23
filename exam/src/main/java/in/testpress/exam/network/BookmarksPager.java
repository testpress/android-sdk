package in.testpress.exam.network;

import android.annotation.SuppressLint;
import android.content.Context;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import in.testpress.core.TestpressSDKDatabase;
import in.testpress.models.greendao.Attachment;
import in.testpress.models.greendao.Bookmark;
import in.testpress.models.greendao.BookmarkDao;
import in.testpress.models.greendao.BookmarkFolder;
import in.testpress.models.greendao.Content;
import in.testpress.models.greendao.ContentType;
import in.testpress.models.greendao.Direction;
import in.testpress.models.greendao.DirectionTranslation;
import in.testpress.models.greendao.HtmlContent;
import in.testpress.models.greendao.ReviewAnswer;
import in.testpress.models.greendao.ReviewAnswerTranslation;
import in.testpress.models.greendao.ReviewItem;
import in.testpress.models.greendao.ReviewQuestion;
import in.testpress.models.greendao.ReviewQuestionTranslation;
import in.testpress.models.greendao.Subject;
import in.testpress.models.greendao.Video;
import in.testpress.network.RetrofitCall;
import in.testpress.network.TestpressApiClient;
import in.testpress.v2_4.BaseResourcePager;
import in.testpress.v2_4.models.ApiResponse;
import in.testpress.v2_4.models.BookmarksListResponse;

import static in.testpress.models.greendao.BookmarkFolder.UNCATEGORIZED;
import static in.testpress.network.TestpressApiClient.FOLDER;
import static in.testpress.network.TestpressApiClient.UNFILTERED;

public class BookmarksPager extends BaseResourcePager<BookmarksListResponse, Bookmark> {

    private Context context;
    private TestpressExamApiClient apiClient;
    private SimpleDateFormat simpleDateFormat;
    private String folder;

    private final Map<Object, BookmarkFolder> folders = new LinkedHashMap<>();
    private final Map<Object, ContentType> contentTypes = new LinkedHashMap<>();

    private final Map<Object, ReviewItem> reviewItems = new LinkedHashMap<>();
    private final Map<Object, ReviewQuestion> questions = new LinkedHashMap<>();
    private final Map<Object, ReviewAnswer> answers = new LinkedHashMap<>();
    private final Map<Object, ReviewQuestionTranslation> translations = new LinkedHashMap<>();
    private final Map<Object, ReviewAnswerTranslation> answerTranslations = new LinkedHashMap<>();

    private final Map<Object, Direction> directions = new LinkedHashMap<>();
    private final Map<Object, DirectionTranslation> directionTranslations = new LinkedHashMap<>();
    private final Map<Object, Subject> subjects = new LinkedHashMap<>();

    private final Map<Object, Content> contents = new LinkedHashMap<>();
    private final Map<Object, Attachment> attachments = new LinkedHashMap<>();
    private final Map<Object, Video> videos = new LinkedHashMap<>();
    private final Map<Object, HtmlContent> htmlContents = new LinkedHashMap<>();

    @SuppressLint("SimpleDateFormat")
    public BookmarksPager(Context context, TestpressExamApiClient apiClient, String folder) {
        this.context = context;
        this.apiClient = apiClient;
        this.folder = folder;
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    }

    public int getTotalCount() {
        return apiResponse.getCount();
    }

    @Override
    protected Object getId(Bookmark resource) {
        return resource.getId();
    }

    @Override
    public RetrofitCall<ApiResponse<BookmarksListResponse>> getResponse(int page, int size) {
        queryParams.put(TestpressApiClient.ORDER, "-created");
        if (folder == null || folder.isEmpty()) {
            queryParams.remove(FOLDER);
        } else if (folder.equals(UNCATEGORIZED)) {
            queryParams.put(FOLDER, "null");
        } else {
            queryParams.put(FOLDER, folder);
        }
        queryParams.put(TestpressApiClient.PAGE, page);
        return apiClient.getBookmarks(queryParams);
    }

    @Override
    protected Bookmark register(Bookmark bookmark) {
        try {
            if (queryParams.containsKey(UNFILTERED) && !bookmark.getActive()) {
                // Remove bookmark id from content object for deleted bookmarks
                List<Bookmark> bookmarksFromDB = TestpressSDKDatabase.getBookmarkDao(context)
                        .queryBuilder().where(BookmarkDao.Properties.Id.eq(bookmark.getId()))
                        .list();

                if (!bookmarksFromDB.isEmpty()) {
                    Object object = bookmarksFromDB.get(0).getBookmarkedObject();
                    if (object instanceof ReviewItem) {
                        ReviewItem reviewItem = (ReviewItem) object;
                        reviewItem.setBookmarkId(null);
                        TestpressSDKDatabase.getReviewItemDao(context)
                                .insertOrReplaceInTx(reviewItem);

                    } else if (object instanceof Content) {
                        Content content = (Content) object;
                        content.setBookmarkId(null);
                        TestpressSDKDatabase.getContentDao(context).insertOrReplaceInTx(content);
                    }
                }
            }
            if (bookmark != null && bookmark.getCreated() != null && !bookmark.getCreated().isEmpty()) {
                bookmark.setCreatedDate(simpleDateFormat.parse(bookmark.getCreated()).getTime());
            }
            if (bookmark != null && bookmark.getModified() != null && !bookmark.getModified().isEmpty()) {
                bookmark.setModifiedDate(simpleDateFormat.parse(bookmark.getModified()).getTime());
            }
            return bookmark;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Bookmark> getItems(BookmarksListResponse resultResponse) {
        for (BookmarkFolder folder : resultResponse.getFolders()) {
            folders.put(folder.getId(), folder);
        }
        for (ContentType contentType : resultResponse.getContentTypes()) {
            contentTypes.put(contentType.getId(), contentType);
        }

        for (ReviewItem reviewItem : resultResponse.getUserSelectedAnswers()) {
            reviewItems.put(reviewItem.getId(), reviewItem);
        }
        for (ReviewQuestion question : resultResponse.getQuestions()) {
            questions.put(question.getId(), question);
        }
        for (ReviewAnswer answer : resultResponse.getAnswers()) {
            answers.put(answer.getId(), answer);
        }
        for (ReviewQuestionTranslation question : resultResponse.getTranslations()) {
            translations.put(question.getId(), question);
        }
        for (ReviewAnswerTranslation answer : resultResponse.getAnswerTranslations()) {
            answerTranslations.put(answer.getId(), answer);
        }

        for (Direction direction : resultResponse.getDirections()) {
            directions.put(direction.getId(), direction);
        }
        for (DirectionTranslation directionTranslation : resultResponse.getDirectionTranslations()) {
            directionTranslations.put(directionTranslation.getId(), directionTranslation);
        }
        for (Subject subject : resultResponse.getSubjects()) {
            subjects.put(subject.getId(), subject);
        }

        for (Content content : resultResponse.getChapterContents()) {
            contents.put(content.getId(), content);
        }
        for (HtmlContent htmlContent : resultResponse.getHtmlContents()) {
            htmlContents.put(htmlContent.getId(), htmlContent);
        }
        for (Video video : resultResponse.getVideos()) {
            videos.put(video.getId(), video);
        }
        for (Attachment attachment : resultResponse.getAttachments()) {
            attachments.put(attachment.getId(), attachment);
        }

        return resultResponse.getBookmarks();
    }

    public ArrayList<BookmarkFolder> getFolders() {
        return new ArrayList<>(folders.values());
    }

    public ArrayList<ContentType> getContentTypes() {
        return new ArrayList<>(contentTypes.values());
    }

    public ArrayList<ReviewItem> getReviewItems() {
        return new ArrayList<>(reviewItems.values());
    }

    public ArrayList<ReviewQuestion> getQuestions() {
        return new ArrayList<>(questions.values());
    }

    public ArrayList<ReviewAnswer> getAnswers() {
        return new ArrayList<>(answers.values());
    }

    public ArrayList<ReviewQuestionTranslation> getTranslations() {
        return new ArrayList<>(translations.values());
    }

    public ArrayList<ReviewAnswerTranslation> getAnswerTranslations() {
        return new ArrayList<>(answerTranslations.values());
    }

    public ArrayList<Direction> getDirections() {
        return new ArrayList<>(directions.values());
    }

    public ArrayList<DirectionTranslation> getDirectionTranslations() {
        return new ArrayList<>(directionTranslations.values());
    }

    public ArrayList<Subject> getSubjects() {
        return new ArrayList<>(subjects.values());
    }

    public ArrayList<Content> getContents() {
        return new ArrayList<>(contents.values());
    }

    public ArrayList<Attachment> getAttachments() {
        return new ArrayList<>(attachments.values());
    }

    public ArrayList<Video> getVideos() {
        return new ArrayList<>(videos.values());
    }

    public ArrayList<HtmlContent> getHtmlContents() {
        return new ArrayList<>(htmlContents.values());
    }
}
