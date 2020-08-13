package in.testpress.exam.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;

import java.util.ArrayList;
import java.util.List;

import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSDKDatabase;
import in.testpress.core.TestpressSdk;
import in.testpress.exam.R;
import in.testpress.exam.api.TestpressExamApiClient;
import in.testpress.exam.util.Watermark;
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
import in.testpress.ui.BaseFragment;
import in.testpress.ui.view.ClosableSpinner;
import in.testpress.util.FullScreenChromeClient;
import in.testpress.util.UIUtils;
import in.testpress.util.ViewUtils;
import in.testpress.util.WebViewUtils;
import in.testpress.v2_4.models.ApiResponse;
import in.testpress.v2_4.models.FolderListResponse;

import static in.testpress.exam.api.TestpressExamApiClient.BOOKMARK_FOLDERS_PATH;
import static in.testpress.models.greendao.BookmarkFolder.UNCATEGORIZED;

public class BookmarksFragment extends BaseFragment {

    static final String PARAM_BOOKMARK_ID = "position";
    static final String PARAM_SELECTED_LANGUAGE = "selectedLanguage";
    private ReviewItem reviewItem;
    private Content content;
    private View emptyView;
    private TextView emptyTitleView;
    private TextView emptyDescView;
    private Button retryButton;
    private ProgressBar progressBar;
    private View rootLayout;
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
    private Bookmark bookmark;
    private Button viewComments;
    private LinearLayout commentsLayout;
    private BookmarksActivity bookmarksActivity;
    private FullScreenChromeClient fullScreenChromeClient;
    private TestpressExamApiClient apiClient;
    private WebViewUtils webViewUtils;
    private Language selectedLanguage;
    private RetrofitCall<ApiResponse<FolderListResponse>> bookmarkFoldersLoader;
    private RetrofitCall<Bookmark> updateBookmarkAPIRequest;
    private RetrofitCall<Void> deleteBookmarkAPIRequest;

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
        bookmarksLayout = view.findViewById(R.id.bookmark_layout);
        rightGradientShadow = view.findViewById(R.id.right_gradient_shadow);
        TextView moveBookmarkText = view.findViewById(R.id.move_bookmark_text);
        TextView removeBookmarkText = view.findViewById(R.id.remove_bookmark_text);
        viewComments = view.findViewById(R.id.button_view_comments);
        commentsLayout = view.findViewById(R.id.comments_layout);
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
        ViewUtils.setTypeface(
                new TextView[] {difficultyTitle, difficultyPercentageText },
                TestpressSdk.getRubikMediumFont(view.getContext())
        );
        ViewUtils.setTypeface(
                new TextView[] { usersAnsweredRight, moveBookmarkText, removeBookmarkText },
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
                    if (getActivity() != null) {
                       commentsLayout.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public String getHeader() {
                return getQuestionsHeader() + getBookmarkHandlerScript();
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
        setOnClickListeners();
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

        boolean isSingleMCQType = reviewItem.getQuestion().getType().equals("R");
        boolean isMultipleMCQType = reviewItem.getQuestion().getType().equals("C");
        boolean isShortAnswerType = reviewItem.getQuestion().getType().equals("S");
        boolean isNumericalType = reviewItem.getQuestion().getType().equals("N");
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
                        answerTranslation.getMarks(),
                        null
                );
            }

            if (isSingleMCQType || isMultipleMCQType) {
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
                        optionColor, getContext(), attemptAnswer.getIsCorrect());

                if (attemptAnswer.getIsCorrect()) {
                    correctAnswerHtml += "\n" + WebViewUtils.getCorrectAnswerIndexWithTags(j);
                }
            } else if (isNumericalType) {
                correctAnswerHtml = attemptAnswer.getTextHtml();
            } else {
                if (j == 0) {
                    html += "<table width='100%' style='margin-top:0px; margin-bottom:15px;'>"
                            + WebViewUtils.getShortAnswerHeadersWithTags();
                }
                html += WebViewUtils.getShortAnswersWithTags(
                        attemptAnswer.getTextHtml(), attemptAnswer.getMarks());

                if (j == reviewAnswers.size() - 1) {
                    html += "</table>";
                }
            }
        }

        if (isShortAnswerType || isNumericalType) {
            html += "<div style='display:box; display:-webkit-box; margin-bottom:10px;'>" +
                    WebViewUtils.getHeadingTags(getString(R.string.testpress_your_answer)) +
                    reviewItem.getShortText() +
                    "</div>";
        }

        if (isSingleMCQType || isMultipleMCQType || isNumericalType) {
            // Add correct answer
            html += "<div style='display:box; display:-webkit-box; margin-bottom:10px;'>" +
                    WebViewUtils.getHeadingTags(getString(R.string.testpress_correct_answer)) +
                    correctAnswerHtml +
                    "</div>";
        }

        if (isShortAnswerType || isNumericalType) {
            html += "<div style='display:box; display:-webkit-box; margin-bottom:10px;'>" +
                    WebViewUtils.getHeadingTags(getString(R.string.testpress_marks_awarded)) +
                    reviewItem.getMarks() +
                    "</div>";
        }

        // Add explanation with watermark
        String watermark = new Watermark().get(getActivity());
        if (explanationHtml != null && !explanationHtml.isEmpty()) {
            html += WebViewUtils.getHeadingTags(getString(R.string.testpress_explanation));
            html += "<div class ='watermark'>" +
                    "© "+ getString(R.string.testpress_app_name) +" "+ watermark +
                    "\n" + "</div>";
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

    private void setOnClickListeners() {
        viewComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCommentFragment();
            }
        });
    }

    private void openCommentFragment() {
        CommentsFragment commentsFragment = CommentsFragment.Companion.getNewInstance(reviewItem, true);
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        commentsFragment.show(transaction, "CommentsFragment");
    }

    protected void setEmptyText(final int title, final int description) {
        emptyView.setVisibility(View.VISIBLE);
        emptyTitleView.setText(title);
        emptyDescView.setText(description);
        retryButton.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public RetrofitCall[] getRetrofitCalls() {
        return new RetrofitCall[] {
                bookmarkFoldersLoader, updateBookmarkAPIRequest, deleteBookmarkAPIRequest
        };
    }

    @Override
    public void onDestroyView() {
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
        if (webView != null) {
            if (isVisibleToUser) {
                webView.onResume();
            } else {
                webView.onPause();
            }
        }
    }

    void handleException(TestpressException exception) {
        if (getActivity() == null) {
            return;
        }
        setRemoveBookmarkProgress(false);
        ViewUtils.handleException(exception, rootLayout, R.string.testpress_folder_name_not_allowed);
    }

    @Override
    public void onPause() {
        super.onPause();
        webView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
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

}
