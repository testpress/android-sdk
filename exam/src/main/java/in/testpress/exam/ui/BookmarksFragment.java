package in.testpress.exam.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSDKDatabase;
import in.testpress.core.TestpressSdk;
import in.testpress.exam.R;
import in.testpress.exam.models.Comment;
import in.testpress.exam.network.CommentsPager;
import in.testpress.exam.network.TestpressExamApiClient;
import in.testpress.exam.util.ImagePickerUtils;
import in.testpress.models.FileDetails;
import in.testpress.models.greendao.Attachment;
import in.testpress.models.greendao.Bookmark;
import in.testpress.models.greendao.BookmarkDao;
import in.testpress.models.greendao.BookmarkFolder;
import in.testpress.models.greendao.BookmarkFolderDao;
import in.testpress.models.greendao.Content;
import in.testpress.models.greendao.HtmlContent;
import in.testpress.models.greendao.Language;
import in.testpress.models.greendao.ReviewAnswer;
import in.testpress.models.greendao.ReviewAnswerTranslation;
import in.testpress.models.greendao.ReviewItem;
import in.testpress.models.greendao.ReviewQuestion;
import in.testpress.models.greendao.ReviewQuestionTranslation;
import in.testpress.models.greendao.Video;
import in.testpress.network.RetrofitCall;
import in.testpress.network.TestpressApiClient;
import in.testpress.ui.view.BackEventListeningEditText;
import in.testpress.ui.view.ClosableSpinner;
import in.testpress.util.CommonUtils;
import in.testpress.util.FormatDate;
import in.testpress.util.FullScreenChromeClient;
import in.testpress.util.ThrowableLoader;
import in.testpress.util.UIUtils;
import in.testpress.util.ViewUtils;
import in.testpress.util.WebViewUtils;
import in.testpress.v2_4.models.ApiResponse;
import in.testpress.v2_4.models.FolderListResponse;

import static android.app.Activity.RESULT_OK;
import static com.theartofdev.edmodo.cropper.CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE;
import static com.theartofdev.edmodo.cropper.CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE;
import static com.theartofdev.edmodo.cropper.CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE;
import static com.theartofdev.edmodo.cropper.CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE;
import static in.testpress.exam.network.TestpressExamApiClient.BOOKMARK_FOLDERS_PATH;
import static in.testpress.exam.network.TestpressExamApiClient.COMMENTS_PATH;
import static in.testpress.exam.network.TestpressExamApiClient.QUESTIONS_PATH;
import static in.testpress.models.greendao.BookmarkFolder.UNCATEGORIZED;

public class BookmarksFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<List<Comment>> {

    public static final String UPDATE_TIME_SPAN = "updateTimeSpan";
    private static final int NEW_COMMENT_SYNC_INTERVAL = 10000; // 10 sec
    private static final int PREVIOUS_COMMENTS_LOADER_ID = 0;
    private static final int NEW_COMMENTS_LOADER_ID = 1;
    static final String PARAM_BOOKMARK_ID = "position";
    static final String PARAM_SELECTED_LANGUAGE = "selectedLanguage";
    private ReviewItem reviewItem;
    private Content content;
    private View emptyView;
    private TextView emptyTitleView;
    private TextView emptyDescView;
    private Button retryButton;
    private ProgressBar progressBar;
    private LinearLayout commentsLayout;
    private LinearLayout previousCommentsLoadingLayout;
    private LinearLayout newCommentsLoadingLayout;
    private RecyclerView commentsListView;
    private LinearLayout loadPreviousCommentsLayout;
    private TextView loadPreviousCommentsText;
    private LinearLayout loadNewCommentsLayout;
    private TextView loadNewCommentsText;
    private BackEventListeningEditText commentsEditText;
    private ImageButton postCommentButton;
    private ImageButton imageCommentButton;
    private View rootLayout;
    private LinearLayout commentBoxLayout;
    private CommentsPager previousCommentsPager;
    private CommentsPager newCommentsPager;
    private CommentsListAdapter commentsAdapter;
    private ProgressDialog progressDialog;
    private View rightGradientShadow;
    private WebView webView;
    private ClosableSpinner folderSpinner;
    private FolderSpinnerAdapter folderSpinnerAdapter;
    private boolean firstCallBack = true;
    private ArrayList<BookmarkFolder> bookmarkFolders = new ArrayList<>();
    private BookmarkDao bookmarkDao;
    private BookmarkFolderDao bookmarkFolderDao;
    private LinearLayout bookmarksLayout;
    private LinearLayout moveBookmarkLayout;
    private LinearLayout removeBookmarkLayout;
    private LottieAnimationView moveBookmarkProgressBar;
    private LottieAnimationView removeBookmarkProgressBar;
    private TextView difficultyPercentageText;
    private boolean postedNewComment;
    private Bookmark bookmark;
    private BookmarksActivity bookmarksActivity;
    private FullScreenChromeClient fullScreenChromeClient;
    private TestpressExamApiClient apiClient;
    private List<Comment> comments = new ArrayList<>();
    @SuppressLint("UseSparseArrays")
    private HashMap<Integer, Comment> uniqueComments = new HashMap<>();
    ImagePickerUtils imagePickerUtils;
    private WebViewUtils webViewUtils;
    private Language selectedLanguage;
    private Handler newCommentsHandler;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            //noinspection ArraysAsListWithZeroOrOneArgument
            commentsAdapter.notifyItemRangeChanged(0, commentsAdapter.getItemCount(),
                    UPDATE_TIME_SPAN); // Update the time in comments

            getNewCommentsPager().reset();
            getLoaderManager().restartLoader(NEW_COMMENTS_LOADER_ID, null, BookmarksFragment.this);
        }
    };
    private RetrofitCall<ApiResponse<FolderListResponse>> bookmarkFoldersLoader;
    private RetrofitCall<Bookmark> updateBookmarkAPIRequest;
    private RetrofitCall<Void> deleteBookmarkAPIRequest;
    private RetrofitCall<Comment> commentAPIRequest;
    private RetrofitCall<FileDetails> imageUploadAPIRequest;

    public static BookmarksFragment getInstance(long bookmarkId, Language selectedLanguage) {
        BookmarksFragment reviewQuestionsFragment = new BookmarksFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(PARAM_BOOKMARK_ID, bookmarkId);
        bundle.putParcelable(PARAM_SELECTED_LANGUAGE, selectedLanguage);
        reviewQuestionsFragment.setArguments(bundle);
        return reviewQuestionsFragment;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiClient = new TestpressExamApiClient(getContext());
        assert getArguments() != null;
        long bookmarkId = getArguments().getLong(PARAM_BOOKMARK_ID);
        bookmarkDao = TestpressSDKDatabase.getBookmarkDao(getContext());
        bookmark = bookmarkDao.queryBuilder().where(BookmarkDao.Properties.Id.eq(bookmarkId))
                .list().get(0);

        selectedLanguage = getArguments().getParcelable(PARAM_SELECTED_LANGUAGE);
        bookmarkFolderDao = TestpressSDKDatabase.getBookmarkFolderDao(getContext());
    }

    @SuppressLint("AddJavascriptInterface")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.testpress_fragment_bookmark_question, container, false);
        progressBar = view.findViewById(R.id.pb_loading);
        emptyView = view.findViewById(R.id.empty_container);
        emptyTitleView = view.findViewById(R.id.empty_title);
        emptyDescView = view.findViewById(R.id.empty_description);
        retryButton = view.findViewById(R.id.retry_button);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                emptyView.setVisibility(View.GONE);
                updateContentObject();
            }
        });
        UIUtils.setIndeterminateDrawable(getContext(), progressBar, 4);
        webView = view.findViewById(R.id.web_view);
        commentsLayout = view.findViewById(R.id.comments_layout);
        previousCommentsLoadingLayout = view.findViewById(R.id.loading_previous_comments_layout);
        newCommentsLoadingLayout = view.findViewById(R.id.loading_new_comments_layout);
        commentsListView = view.findViewById(R.id.comments_list_view);
        loadPreviousCommentsLayout = view.findViewById(R.id.load_previous_comments_layout);
        loadPreviousCommentsText = view.findViewById(R.id.load_previous_comments);
        loadNewCommentsLayout = view.findViewById(R.id.load_new_comments_layout);
        loadNewCommentsText = view.findViewById(R.id.load_new_comments_text);
        TextView commentsLabel = view.findViewById(R.id.comments_label);
        commentsEditText = view.findViewById(R.id.comment_box);
        commentBoxLayout = view.findViewById(R.id.comment_box_layout);
        postCommentButton = view.findViewById(R.id.post_comment_button);
        imageCommentButton = view.findViewById(R.id.image_comment_button);
        bookmarksLayout = view.findViewById(R.id.bookmark_layout);
        rightGradientShadow = view.findViewById(R.id.right_gradient_shadow);
        TextView moveBookmarkText = view.findViewById(R.id.move_bookmark_text);
        TextView removeBookmarkText = view.findViewById(R.id.remove_bookmark_text);
        moveBookmarkLayout = view.findViewById(R.id.move_bookmark_layout);
        moveBookmarkLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //noinspection ConstantConditions
                String baseUrl = TestpressSdk.getTestpressSession(getActivity())
                        .getInstituteSettings().getBaseUrl();

                bookmarkFolders.clear();
                loadBookmarkFolders(baseUrl + BOOKMARK_FOLDERS_PATH);
            }
        });
        removeBookmarkLayout = view.findViewById(R.id.remove_bookmark_layout);
        removeBookmarkLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteBookmark(bookmark.getId());
            }
        });
        folderSpinner = view.findViewById(R.id.bookmark_folder_spinner);
        folderSpinnerAdapter = new FolderSpinnerAdapter(getActivity(), getResources(),
                new ViewUtils.OnInputCompletedListener() {
                    @Override
                    public void onInputComplete(String folderName) {
                        folderSpinner.dismissPopUp();
                        updateBookmark(folderName);
                    }
                });
        folderSpinnerAdapter.hideSpinner(true);
        folderSpinner.setAdapter(folderSpinnerAdapter);
        folderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> spinner, View view, int position, long itemId) {
                if (firstCallBack) {
                    firstCallBack = false;
                    return;
                }
                String existingFolder = bookmark.getFolderFromDB() != null ?
                        bookmark.getFolderFromDB() : UNCATEGORIZED;

                String selectedFolder = folderSpinnerAdapter.getTag(position);
                if (selectedFolder.equals(existingFolder)) {
                    return;
                }
                updateBookmark(selectedFolder);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        moveBookmarkProgressBar = view.findViewById(R.id.move_bookmark_loader);
        moveBookmarkProgressBar.playAnimation();
        removeBookmarkProgressBar = view.findViewById(R.id.remove_bookmark_loader);
        removeBookmarkProgressBar.playAnimation();

        difficultyPercentageText = view.findViewById(R.id.difficulty_percentage);
        TextView difficultyTitle = view.findViewById(R.id.difficulty_title);
        TextView usersAnsweredRight = view.findViewById(R.id.users_answered_right);

        rootLayout = view;
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage(getResources().getString(R.string.testpress_please_wait));
        progressDialog.setCancelable(false);
        UIUtils.setIndeterminateDrawable(getContext(), progressDialog, 4);
        ViewUtils.setTypeface(
                new TextView[] { loadPreviousCommentsText, commentsLabel, loadNewCommentsText,
                        difficultyTitle, difficultyPercentageText },
                TestpressSdk.getRubikMediumFont(view.getContext())
        );
        ViewUtils.setTypeface(
                new TextView[] { commentsEditText, usersAnsweredRight, moveBookmarkText,
                        removeBookmarkText },
                TestpressSdk.getRubikRegularFont(view.getContext())
        );
        webViewUtils = new WebViewUtils(webView) {
            @Override
            protected void onLoadFinished() {
                super.onLoadFinished();
                progressBar.setVisibility(View.GONE);
                bookmarksLayout.setVisibility(View.VISIBLE);
                if (reviewItem != null) {
                    setDifficulty(view);
                    if (commentsAdapter == null && getActivity() != null) {
                        displayComments();
                    }
                }
            }

            @Override
            public String getHeader() {
                return super.getHeader() + getBookmarkHandlerScript();
            }

            @Override
            protected void onNetworkError() {
                setEmptyText(R.string.testpress_network_error,
                        R.string.testpress_no_internet_try_again);

                retryButton.setVisibility(View.VISIBLE);
            }
        };
        fullScreenChromeClient = new FullScreenChromeClient(getActivity());
        updateContentObject();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        bookmarksActivity = (BookmarksActivity) getActivity();
    }

    void updateContentObject() {
        bookmark = bookmarkDao.queryBuilder().where(BookmarkDao.Properties.Id.eq(bookmark.getId()))
                .list().get(0);

        Object object = bookmark.getBookmarkedObject();
        if (object instanceof ReviewItem) {
            reviewItem = (ReviewItem) bookmark.getBookmarkedObject();
            webViewUtils.initWebView(getReviewItemAsHtml(), getActivity());
        } else if (object instanceof Content) {
            content = (Content) bookmark.getBookmarkedObject();
            checkContentType();
        } else {
            setEmptyText(R.string.testpress_error_loading_questions,
                    R.string.testpress_some_thing_went_wrong_try_again);
        }
    }

    private void checkContentType() {
        if (content.getHtml() != null) {
            HtmlContent htmlContent = content.getHtml();
            setContentTitle(Html.fromHtml(htmlContent.getTitle()));
            String html = "<div style='padding-left: 20px; padding-right: 20px;'>" +
                                htmlContent.getTextHtml() + "</div>";

            webViewUtils.initWebView(html, getActivity());
        } else if (content.getRawVideo() != null) {
            rightGradientShadow.setVisibility(View.GONE);
            Video video = content.getRawVideo();
            setContentTitle(video.getTitle());
            String html = "<div style='padding-left: 20px; padding-right: 20px;' class='videoWrapper'>" +
                    video.getEmbedCode() + "</div>";

            webViewUtils.initWebView(html, getActivity());
            webView.setWebChromeClient(fullScreenChromeClient);
        } else if (content.getRawAttachment() != null) {
            displayAttachmentContent();
        } else {
            setEmptyText(R.string.testpress_error_loading_bookmarks,
                    R.string.testpress_some_thing_went_wrong_try_again);
        }
    }

    private void setContentTitle(CharSequence title) {
        TextView titleView = rootLayout.findViewById(R.id.title);
        LinearLayout titleLayout = rootLayout.findViewById(R.id.title_layout);
        titleView.setText(title);
        titleLayout.setVisibility(View.VISIBLE);
        bookmarksLayout.setVisibility(View.VISIBLE);
    }

    private void displayAttachmentContent() {
        setContentTitle(content.getName());
        TextView description = rootLayout.findViewById(R.id.attachment_description);
        final Attachment attachment = content.getRawAttachment();
        if (attachment.getDescription() != null && !attachment.getDescription().isEmpty()) {
            description.setText(attachment.getDescription());
            description.setTypeface(TestpressSdk.getRubikRegularFont(description.getContext()));
            description.setVisibility(View.VISIBLE);
        } else {
            description.setVisibility(View.GONE);
        }
        Button downloadButton = rootLayout.findViewById(R.id.download_attachment);
        ViewUtils.setLeftDrawable(downloadButton.getContext(), downloadButton,
                R.drawable.ic_file_download_18dp);

        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse(attachment.getAttachmentUrl())));
            }
        });
        rootLayout.findViewById(R.id.attachment_content_layout).setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        bookmarksLayout.setVisibility(View.VISIBLE);
    }

    /**
     * Convert review item to HTML
     *
     * @return HTML as String
     */
    private String getReviewItemAsHtml() {
        String htmlContent = null;
        ReviewQuestion reviewQuestion = reviewItem.getQuestion();
        List<ReviewQuestionTranslation> translations = reviewQuestion.getTranslations();
        if (translations.size() > 0 && selectedLanguage != null &&
                !selectedLanguage.getCode().equals(reviewQuestion.getLanguage())) {

            for (ReviewQuestionTranslation translation : translations) {
                if (translation.getLanguage().equals(selectedLanguage.getCode())) {
                    htmlContent = getHtml(
                            translation.getDirectionFromDB(),
                            translation.getQuestionHtml(),
                            translation.getAnswers(),
                            translation.getExplanation(),
                            reviewQuestion.getSubjectFromDB()
                    );
                }
            }
        }
        if (htmlContent == null) {
            htmlContent = getHtml(
                    reviewQuestion.getDirectionFromDB(),
                    reviewQuestion.getQuestionHtml(),
                    reviewQuestion.getAnswers(),
                    reviewQuestion.getExplanationHtml(),
                    reviewQuestion.getSubjectFromDB()
            );
        }
        return htmlContent;
    }

    private String getHtml(String directionHtml, String questionHtml,
                           Object answers, String explanationHtml, String subject) {

        String html = "<div style='padding-left: 12px; padding-right: 12px;'>";

        // Add direction/passage
        if (directionHtml != null && !directionHtml.isEmpty()) {
            html += "<div class='question' style='padding-bottom: 0px;'>" +
                        directionHtml +
                    "</div>";
        }

        // Add question
        html += "<div class='question'>" +
                questionHtml +
                "</div>";

        // Add options
        String correctAnswerHtml = "";
        //noinspection unchecked
        List<Object> reviewAnswers = (List<Object>) answers;
        for (int j = 0; j < reviewAnswers.size(); j++) {
            ReviewAnswer attemptAnswer;
            if (reviewAnswers.get(j) instanceof ReviewAnswer) {
                attemptAnswer = (ReviewAnswer) reviewAnswers.get(j);
            } else {
                ReviewAnswerTranslation answerTranslation = (ReviewAnswerTranslation) reviewAnswers.get(j);
                attemptAnswer = new ReviewAnswer(
                        answerTranslation.getId(),
                        answerTranslation.getTextHtml(),
                        answerTranslation.getIsCorrect(),
                        null
                );
            }

            int optionColor;
            if (reviewItem.getSelectedAnswers().contains(attemptAnswer.getId().intValue())) {

                if (attemptAnswer.getIsCorrect()) {
                    optionColor = R.color.testpress_green;
                } else {
                    optionColor = R.color.testpress_red;
                }
            } else {
                optionColor = android.R.color.white;
            }
            html += "\n" + WebViewUtils.getOptionWithTags(attemptAnswer.getTextHtml(), j,
                    optionColor, getContext());
            if (attemptAnswer.getIsCorrect()) {
                correctAnswerHtml += "\n" + WebViewUtils.getCorrectAnswerIndexWithTags(j);
            }
        }

        // Add correct answer
        html += "<div style='display:box; display:-webkit-box; margin-bottom:10px;'>" +
                    WebViewUtils.getHeadingTags(getString(R.string.testpress_correct_answer)) +
                    correctAnswerHtml +
                "</div>";

        // Add explanation
        if (explanationHtml != null && !explanationHtml.isEmpty()) {
            html += WebViewUtils.getHeadingTags(getString(R.string.testpress_explanation));
            html += "<div class='review-explanation'>" +
                        explanationHtml +
                    "</div>";
        }

        // Add subject
        if (subject != null && !subject.isEmpty() && !subject.equals("Uncategorized")) {
            html += "<div>" +
                        WebViewUtils.getHeadingTags(getString(R.string.testpress_subject)) +
                        "<div class='subject'>" + subject + "</div>" +
                    "</div>";
        }
        return html + "</div>";
    }

    void loadBookmarkFolders(String url) {
        setMoveBookmarkProgress(true);
        bookmarkFoldersLoader = apiClient.getBookmarkFolders(url)
                .enqueue(new TestpressCallback<ApiResponse<FolderListResponse>>() {
                    @Override
                    public void onSuccess(ApiResponse<FolderListResponse> apiResponse) {
                        bookmarkFolders.addAll(apiResponse.getResults().getFolders());
                        if (apiResponse.getNext() != null) {
                            loadBookmarkFolders(apiResponse.getNext());
                        } else {
                            if (getActivity() == null) {
                                return;
                            }

                            bookmarkFolderDao.deleteAll();
                            bookmarkFolderDao.insertOrReplaceInTx(bookmarkFolders);
                            addFoldersToSpinner();
                            setMoveBookmarkProgress(false);
                            folderSpinner.performClick();
                        }
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        setMoveBookmarkProgress(false);
                        handleException(exception);
                    }
                });
    }

    void updateBookmark(String folder) {
        setMoveBookmarkProgress(true);
        if (folder.equals(UNCATEGORIZED)) {
            folder = "";
        }
        updateBookmarkAPIRequest = apiClient.updateBookmark(bookmark.getId(), folder)
                .enqueue(new TestpressCallback<Bookmark>() {
                    @Override
                    public void onSuccess(Bookmark newBookmark) {
                        if (newBookmark.getFolderId() != null) {
                            List<BookmarkFolder> folders = bookmarkFolderDao.queryBuilder()
                                    .where(BookmarkFolderDao.Properties.Id.eq(newBookmark.getFolderId()))
                                    .list();

                            if (folders.isEmpty()) {
                                BookmarkFolder newFolder = new BookmarkFolder(newBookmark.getFolderId(),
                                        newBookmark.getFolder(), 1);

                                bookmarkFolderDao.insertOrReplaceInTx(newFolder);
                                bookmarksActivity.addNewFolderToSpinner(newBookmark.getFolder());
                            } else {
                                BookmarkFolder folderFromDB = folders.get(0);
                                folderFromDB.setBookmarksCount(folderFromDB.getBookmarksCount() + 1);
                                bookmarkFolderDao.insertOrReplaceInTx(folderFromDB);
                                bookmarksActivity.updateFolderSpinnerItem(folderFromDB);
                            }
                        }
                        if (bookmark.getFolderId() != null) {
                            BookmarkFolder folderFromDB = bookmarkFolderDao.queryBuilder()
                                    .where(BookmarkFolderDao.Properties.Id.eq(bookmark.getFolderId()))
                                    .list().get(0);

                            folderFromDB.setBookmarksCount(folderFromDB.getBookmarksCount() - 1);
                            bookmarkFolderDao.insertOrReplaceInTx(folderFromDB);
                            bookmarksActivity.updateFolderSpinnerItem(folderFromDB);
                        }
                        bookmark.setFolder(newBookmark.getFolder());
                        bookmark.setFolderId(newBookmark.getFolderId());
                        bookmark.setLoadedInRespectiveFolder(false);
                        bookmarkDao.updateInTx(bookmark);
                        setMoveBookmarkProgress(false);
                        Snackbar.make(rootLayout, R.string.testpress_bookmark_moved,
                                Snackbar.LENGTH_SHORT).show();

                        bookmarksActivity.updateItems(true);
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        setMoveBookmarkProgress(false);
                        handleException(exception);
                    }
                });
    }

    void deleteBookmark(final Long bookmarkId) {
        setRemoveBookmarkProgress(true);
        deleteBookmarkAPIRequest = apiClient.deleteBookmark(bookmarkId)
                .enqueue(new TestpressCallback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        if (reviewItem != null) {
                            reviewItem.setBookmarkId(null);
                            TestpressSDKDatabase.getReviewItemDao(getContext())
                                    .insertOrReplaceInTx(reviewItem);

                        } else if (content != null) {
                            content.setBookmarkId(null);
                            TestpressSDKDatabase.getContentDao(getContext())
                                    .insertOrReplaceInTx(content);
                        }
                        bookmark.setActive(false);
                        bookmarkDao.updateInTx(bookmark);
                        if (bookmark.getFolderId() != null) {
                            List<BookmarkFolder> folders = bookmarkFolderDao.queryBuilder()
                                    .where(BookmarkFolderDao.Properties.Id.eq(bookmark.getFolderId()))
                                    .list();

                            BookmarkFolder folderFromDB = folders.get(0);
                            folderFromDB.setBookmarksCount(folderFromDB.getBookmarksCount() - 1);
                            bookmarkFolderDao.insertOrReplaceInTx(folderFromDB);
                            bookmarksActivity.updateFolderSpinnerItem(folderFromDB);
                        }
                        setRemoveBookmarkProgress(false);
                        Snackbar snackbar = Snackbar.make(rootLayout,
                                R.string.testpress_bookmark_deleted, Snackbar.LENGTH_LONG);

                        snackbar.setActionTextColor(ContextCompat.getColor(snackbar.getContext(),
                                R.color.testpress_color_primary_blue));

                        snackbar.setAction(R.string.testpress_undo, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                bookmarksActivity.undoBookmarkDelete(bookmarkId);
                            }
                        });
                        snackbar.show();

                        //noinspection ConstantConditions
                        bookmarksActivity.updateItems(true);
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        setRemoveBookmarkProgress(false);
                        handleException(exception);
                    }
                });
    }

    void setMoveBookmarkProgress(boolean show) {
        if (show) {
            moveBookmarkLayout.setVisibility(View.GONE);
            moveBookmarkProgressBar.setVisibility(View.VISIBLE);
        } else {
            moveBookmarkProgressBar.setVisibility(View.GONE);
            moveBookmarkLayout.setVisibility(View.VISIBLE);
        }
    }

    void setRemoveBookmarkProgress(boolean show) {
        if (show) {
            removeBookmarkLayout.setVisibility(View.GONE);
            removeBookmarkProgressBar.setVisibility(View.VISIBLE);
        } else {
            removeBookmarkProgressBar.setVisibility(View.GONE);
            removeBookmarkLayout.setVisibility(View.VISIBLE);
        }
    }

    void addFoldersToSpinner() {
        folderSpinnerAdapter.clear();
        for (BookmarkFolder folder: bookmarkFolders) {
            folderSpinnerAdapter.addItem(folder.getName(), folder.getName(), false, 0);
        }
        folderSpinnerAdapter.addItem(UNCATEGORIZED, UNCATEGORIZED, false, 0);
        folderSpinnerAdapter.notifyDataSetChanged();

        String folder = UNCATEGORIZED;
        if (bookmark.getFolderId() != null) {
            List<BookmarkFolder> folders = bookmarkFolderDao.queryBuilder()
                    .where(BookmarkFolderDao.Properties.Id.eq(bookmark.getFolderId()))
                    .list();

            if (!folders.isEmpty()) {
                folder = folders.get(0).getName();
            }
        }
        folderSpinner.setSelection(folderSpinnerAdapter.getItemPosition(folder));
    }

    @SuppressLint("SetTextI18n")
    private void setDifficulty(View view) {
        if (reviewItem.getQuestion().getPercentageGotCorrect() == null) {
            view.findViewById(R.id.difficulty_layout).setVisibility(View.GONE);
        } else {
            int percentageCorrect = Math.round(reviewItem.getQuestion().getPercentageGotCorrect());
            if (percentageCorrect >= 0) {
                setBackgroundDrawable(view, R.id.difficulty1, R.drawable.testpress_difficulty_left_on);
            }
            if (percentageCorrect > 20) {
                setBackgroundColor(view, R.id.difficulty2, R.color.testpress_difficulty_level_2);
            }
            if (percentageCorrect > 40) {
                setBackgroundColor(view, R.id.difficulty3, R.color.testpress_difficulty_level_3);
            }
            if (percentageCorrect > 60) {
                setBackgroundColor(view, R.id.difficulty4, R.color.testpress_difficulty_level_4);
            }
            if (percentageCorrect > 80) {
                setBackgroundDrawable(view, R.id.difficulty5, R.drawable.testpress_difficulty_right_on);
            }
            difficultyPercentageText.setText(percentageCorrect + "%");
            view.findViewById(R.id.difficulty_layout).setVisibility(View.VISIBLE);
        }
    }

    void displayComments() {
        commentsAdapter = new CommentsListAdapter(getActivity(), apiClient);
        commentsListView.setNestedScrollingEnabled(false);
        commentsListView.setLayoutManager(new LinearLayoutManager(getContext()));
        commentsListView.setAdapter(commentsAdapter);
        postCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickSendCommentButton();
            }
        });
        imageCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImageFromMobile();
            }
        });
        commentsEditText.setImeBackListener(new BackEventListeningEditText.EditTextImeBackListener() {
            @Override
            public void onImeBack(BackEventListeningEditText editText, String text) {
                // On back press while editing clear focus
                commentsEditText.clearFocus();
            }
        });
        commentsEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // Hide/Show navigation bar based on focus change
                //noinspection ConstantConditions
                bookmarksActivity.setNavigationBarVisible(!hasFocus);
            }
        });
        loadPreviousCommentsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadPreviousCommentsLayout.setVisibility(View.GONE);
                getLoaderManager()
                        .restartLoader(PREVIOUS_COMMENTS_LOADER_ID, null, BookmarksFragment.this);
            }
        });
        loadNewCommentsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadNewCommentsLayout.setVisibility(View.GONE);
                // Display newly loaded comments if exist or restart loader
                if (comments.size() != commentsAdapter.getItemCount()) {
                    commentsAdapter.setComments(comments);
                } else {
                    getLoaderManager()
                            .restartLoader(NEW_COMMENTS_LOADER_ID, null, BookmarksFragment.this);
                }
            }
        });
        imagePickerUtils = new ImagePickerUtils(rootLayout, this);
        commentsLayout.setVisibility(View.VISIBLE);
        getLoaderManager().initLoader(PREVIOUS_COMMENTS_LOADER_ID, null, BookmarksFragment.this);
    }

    @NonNull
    @SuppressLint("StaticFieldLeak")
    @Override
    public Loader<List<Comment>> onCreateLoader(int loaderId, Bundle args) {
        switch (loaderId) {
            case PREVIOUS_COMMENTS_LOADER_ID:
                previousCommentsLoadingLayout.setVisibility(View.VISIBLE);
                return new ThrowableLoader<List<Comment>>(getContext(), null) {
                    @Override
                    public List<Comment> loadData() throws TestpressException {
                        getPreviousCommentsPager().clearResources().next();
                        return getPreviousCommentsPager().getResources();
                    }
                };
            default:
                if (postedNewComment) {
                    newCommentsLoadingLayout.setVisibility(View.VISIBLE);
                }
                return new ThrowableLoader<List<Comment>>(getContext(), null) {
                    @Override
                    public List<Comment> loadData() throws TestpressException {
                        do {
                            getNewCommentsPager().next();
                        } while (getNewCommentsPager().hasNext());
                        return getNewCommentsPager().getResources();
                    }
                };
        }
    }

    @SuppressLint("SimpleDateFormat")
    CommentsPager getPreviousCommentsPager() {
        if (previousCommentsPager == null) {
            previousCommentsPager = new CommentsPager(reviewItem.getQuestion().getId(), apiClient);
            previousCommentsPager.queryParams.put(TestpressApiClient.ORDER, "-submit_date");
            // Query comments till now to paginate afterwards
            previousCommentsPager.queryParams.put(TestpressApiClient.UNTIL,
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ").format(new Date()));
        }
        return previousCommentsPager;
    }

    CommentsPager getNewCommentsPager() {
        if (newCommentsPager == null) {
            newCommentsPager = new CommentsPager(reviewItem.getQuestion().getId(), apiClient);
        }
        //  Query comments after the latest comment we already have
        if (newCommentsPager.queryParams.isEmpty() && comments.size() != 0) {
            Comment latestComment = comments.get(0);
            //noinspection ConstantConditions
            newCommentsPager.queryParams.put(TestpressApiClient.SINCE, latestComment.getSubmitDate());
        }
        return newCommentsPager;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<Comment>> loader, List<Comment> comments) {
        if (getActivity() == null) {
            return;
        }
        switch (loader.getId()) {
            case PREVIOUS_COMMENTS_LOADER_ID:
                onPreviousCommentsLoadFinished(loader, comments);
                break;
            case NEW_COMMENTS_LOADER_ID:
                onNewCommentsLoadFinished(loader, comments);
                break;
        }
    }

    void onPreviousCommentsLoadFinished(Loader<List<Comment>> loader, List<Comment> previousComments) {
        //noinspection ThrowableResultOfMethodCallIgnored
        final Exception exception = ThrowableLoader.getException(loader);
        if (exception != null) {
            exception.printStackTrace();
            previousCommentsLoadingLayout.setVisibility(View.GONE);
            // Discard the exception if comments count is 0
            if (reviewItem.getCommentsCount() == null || reviewItem.getCommentsCount() == 0) {
                commentBoxLayout.setVisibility(View.VISIBLE);
            } else if (exception.getCause() instanceof IOException) {
                if (commentsAdapter.getItemCount() == 0) {
                    loadPreviousCommentsText.setText(R.string.load_comments);
                }
                loadPreviousCommentsLayout.setVisibility(View.VISIBLE);
                Snackbar.make(rootLayout, R.string.testpress_no_internet_connection,
                        Snackbar.LENGTH_SHORT).show();
            } else {
                Snackbar.make(rootLayout, R.string.testpress_network_error,
                        Snackbar.LENGTH_SHORT).show();
            }
            return;
        }

        if (previousComments != null && !previousComments.isEmpty()) {
            // Add the comments to the hash map
            addComments(previousComments);
            // Append the comments to the comments in adapter
            commentsAdapter.addComments(previousComments);
        }
        if (commentBoxLayout.getVisibility() == View.GONE) {
            commentBoxLayout.setVisibility(View.VISIBLE);
        }
        if (getPreviousCommentsPager().hasNext()) {
            loadPreviousCommentsText.setText(R.string.load_previous_comments);
            loadPreviousCommentsLayout.setVisibility(View.VISIBLE);
        } else {
            loadPreviousCommentsLayout.setVisibility(View.GONE);
        }
        previousCommentsLoadingLayout.setVisibility(View.GONE);
        if (newCommentsHandler == null) {
            newCommentsHandler = new Handler();
            if (getUserVisibleHint()) {
                newCommentsHandler.postDelayed(runnable, NEW_COMMENT_SYNC_INTERVAL);
            }
        }
    }

    void onNewCommentsLoadFinished(Loader<List<Comment>> loader, List<Comment> newComments) {
        //noinspection ThrowableResultOfMethodCallIgnored
        final Exception exception = ThrowableLoader.getException(loader);
        if (exception != null) {
            newCommentsLoadingLayout.setVisibility(View.GONE);
            if (postedNewComment) {
                if (exception.getCause() instanceof IOException) {
                    Snackbar.make(rootLayout, R.string.testpress_no_internet_connection,
                            Snackbar.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(rootLayout, R.string.testpress_network_error,
                            Snackbar.LENGTH_SHORT).show();
                }
                loadNewCommentsText.setText(R.string.load_new_comments);
                loadNewCommentsLayout.setVisibility(View.VISIBLE);
            } else {
                newCommentsHandler.postDelayed(runnable, NEW_COMMENT_SYNC_INTERVAL);
            }
            return;
        }

        newCommentsLoadingLayout.setVisibility(View.GONE);
        if (!newComments.isEmpty()) {
            if (postedNewComment) {
                // Add new comments to the existing comments & set it to the adapter
                addComments(newComments);
                commentsAdapter.setComments(comments);
            } else {
                // Add new comments to the existing comments
                addComments(newComments);
                // Display new comments available label with count
                int newCommentsCount = comments.size() - commentsAdapter.getItemCount();
                loadNewCommentsText.setText(getResources().getQuantityString(
                        R.plurals.new_comments_available, newCommentsCount, newCommentsCount));

                loadNewCommentsLayout.setVisibility(View.VISIBLE);
            }
        }
        if (postedNewComment) {
            postedNewComment = false;
        }
        newCommentsHandler.postDelayed(runnable, NEW_COMMENT_SYNC_INTERVAL);
    }

    void onClickSendCommentButton() {
        final String comment = commentsEditText.getText().toString().trim();
        if (comment.isEmpty()) {
            return;
        }
        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }
        // noinspection deprecation
        postComment(Html.toHtml(new SpannableString(comment))); // Convert to html to support line breaks
    }

    void postComment(String comment) {
        // Clear edit text focus to display the navigation bar
        commentsEditText.clearFocus(getActivity());
        String url = apiClient.getBaseUrl() + QUESTIONS_PATH + reviewItem.getQuestionId() +
                COMMENTS_PATH;

        commentAPIRequest = apiClient.postComment(url, comment)
                .enqueue(new TestpressCallback<Comment>() {
                    @Override
                    public void onSuccess(Comment comment) {
                        if (getActivity() == null) {
                            return;
                        }
                        commentsEditText.setText("");
                        progressDialog.dismiss();
                        Snackbar.make(rootLayout, R.string.comment_posted,
                                Snackbar.LENGTH_SHORT).show();

                        // Stop new comments sync handler & load new comments now itself
                        if (newCommentsHandler != null) {
                            newCommentsHandler.removeCallbacks(runnable);
                        }
                        postedNewComment = true;
                        getNewCommentsPager().reset();
                        getLoaderManager().destroyLoader(NEW_COMMENTS_LOADER_ID);
                        getLoaderManager().restartLoader(NEW_COMMENTS_LOADER_ID, null,
                                BookmarksFragment.this);
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        handleException(exception);
                    }
                });
    }

    public void pickImageFromMobile() {
        //noinspection ConstantConditions
        startActivityForResult(CropImage.getPickImageChooserIntent(getContext()),
                PICK_IMAGE_CHOOSER_REQUEST_CODE);
    }

    @Override
    @SuppressLint("NewApi")
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        imagePickerUtils.onActivityResult(requestCode, resultCode, data,
                new ImagePickerUtils.ImagePickerResultHandler() {
                    @Override
                    public void onSuccessfullyImageCropped(String imagePath) {
                        uploadImage(imagePath);
                    }
                });
    }

    void uploadImage(String imagePath) {
        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }
        imageUploadAPIRequest = apiClient.upload(imagePath)
                .enqueue(new TestpressCallback<FileDetails>() {
                    @Override
                    public void onSuccess(FileDetails fileDetails) {
                        postComment(WebViewUtils.appendImageTags(fileDetails.getUrl()));
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        handleException(exception);
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {

        imagePickerUtils.permissionsUtils.onRequestPermissionsResult(requestCode, grantResults);
    }

    void addComments(List<Comment> commentsList) {
        for (Comment comment : commentsList) {
            uniqueComments.put(comment.getId(), comment);
        }
        comments = new ArrayList<>(uniqueComments.values());
        Collections.sort(this.comments, new Comparator<Comment>() {
            @Override
            public int compare(Comment o1, Comment o2) {
                //noinspection ComparatorMethodParameterNotUsed
                return FormatDate.compareDate(o2.getSubmitDate(), o1.getSubmitDate(),
                        "yyyy-MM-dd'T'HH:mm:ss", "UTC") ? 1 : -1;
            }
        });
    }

    protected void setEmptyText(final int title, final int description) {
        emptyView.setVisibility(View.VISIBLE);
        emptyTitleView.setText(title);
        emptyDescView.setText(description);
        retryButton.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        if (newCommentsHandler != null) {
            newCommentsHandler.removeCallbacks(runnable);
        }
        CommonUtils.cancelAPIRequests(new RetrofitCall[] {
                bookmarkFoldersLoader, updateBookmarkAPIRequest, deleteBookmarkAPIRequest,
                commentAPIRequest, imageUploadAPIRequest
        });
        final ViewGroup viewGroup = (ViewGroup) webView.getParent();
        if (viewGroup != null) {
            // Remove webView from its parent before destroy to support below kitkat
            viewGroup.removeView(webView);
        }
        webView.destroy();
        super.onDestroyView();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (newCommentsHandler != null) {
                newCommentsHandler.postDelayed(runnable, NEW_COMMENT_SYNC_INTERVAL);
            }
            if (webView != null) {
                webView.onResume();
            }
        } else {
            if (webView != null) {
                webView.onPause();
            }
            if (commentsEditText != null) {
                // Hide keyboard on user swiped(moved) to the adjacent question
                commentsEditText.clearFocus(getActivity());
            }
            if (newCommentsHandler != null) {
                newCommentsHandler.removeCallbacks(runnable);
            }
        }
    }

    void handleException(TestpressException exception) {
        if (getActivity() == null) {
            return;
        }
        progressDialog.dismiss();
        if(exception.isUnauthenticated()) {
            Snackbar.make(rootLayout, R.string.testpress_authentication_failed,
                    Snackbar.LENGTH_SHORT).show();
        } else if (exception.isNetworkError()) {
            Snackbar.make(rootLayout, R.string.testpress_no_internet_connection,
                    Snackbar.LENGTH_SHORT).show();
        } else if (exception.isClientError()) {
            Snackbar.make(rootLayout, R.string.testpress_folder_name_not_allowed,
                    Snackbar.LENGTH_SHORT).show();
        } else {
            Snackbar.make(rootLayout, R.string.testpress_network_error,
                    Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        webView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (imagePickerUtils != null) {
            imagePickerUtils.permissionsUtils.onResume();
        }
        webView.onResume();
    }

    void setBackgroundColor(View parentView, int viewId, @ColorRes int colorResId) {
        parentView.findViewById(viewId).setBackgroundColor(
                ContextCompat.getColor(parentView.getContext(), colorResId));
    }

    void setBackgroundDrawable(View parentView, int viewId, @DrawableRes int drawableResId) {
        parentView.findViewById(viewId).setBackground(
                ContextCompat.getDrawable(parentView.getContext(), drawableResId));
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<Comment>> loader) {
    }

}
