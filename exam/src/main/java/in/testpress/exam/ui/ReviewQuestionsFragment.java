package in.testpress.exam.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.theartofdev.edmodo.cropper.CropImage;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.List;

import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSDKDatabase;
import in.testpress.core.TestpressSdk;
import in.testpress.exam.R;
import in.testpress.exam.api.TestpressExamApiClient;
import in.testpress.exam.util.CommentsUtil;
import in.testpress.exam.util.ImageUtils;
import in.testpress.models.InstituteSettings;
import in.testpress.models.greendao.Bookmark;
import in.testpress.models.greendao.BookmarkFolder;
import in.testpress.models.greendao.Language;
import in.testpress.models.greendao.ReviewAnswer;
import in.testpress.models.greendao.ReviewAnswerTranslation;
import in.testpress.models.greendao.ReviewItem;
import in.testpress.models.greendao.ReviewItemDao;
import in.testpress.models.greendao.ReviewQuestion;
import in.testpress.models.greendao.ReviewQuestionTranslation;
import in.testpress.network.RetrofitCall;
import in.testpress.ui.view.ClosableSpinner;
import in.testpress.util.CommonUtils;
import in.testpress.util.UIUtils;
import in.testpress.util.ViewUtils;
import in.testpress.util.WebViewUtils;
import in.testpress.v2_4.models.ApiResponse;
import in.testpress.v2_4.models.FolderListResponse;

import static in.testpress.exam.api.TestpressExamApiClient.BOOKMARK_FOLDERS_PATH;
import static in.testpress.models.greendao.BookmarkFolder.UNCATEGORIZED;
import static in.testpress.util.CommonUtils.isAppInstalled;

public class ReviewQuestionsFragment extends Fragment {

    static final String PARAM_REVIEW_ITEM_ID = "reviewItemId";
    static final String PARAM_SELECTED_LANGUAGE = "selectedLanguage";
    private ReviewItem reviewItem;
    private View emptyView;
    private TextView emptyTitleView;
    private TextView emptyDescView;
    private Button retryButton;
    private ProgressBar progressBar;
    View rootLayout;
    WebView webView;
    ClosableSpinner bookmarkFolderSpinner;
    FolderSpinnerAdapter folderSpinnerAdapter;
    ArrayList<BookmarkFolder> bookmarkFolders = new ArrayList<>();
    ReviewItemDao reviewItemDao;
    LottieAnimationView animationView;
    TextView difficultyTitle;
    TextView difficultyPercentageText;
    TextView usersAnsweredRight;
    ImageView imageView1;
    ImageView imageView2;
    ImageView imageView3;
    ImageView imageView4;
    ImageView imageView5;
    int percentageCorrect;
    TestpressExamApiClient apiClient;
    ImageUtils imageUtils;
    private CommentsUtil commentsUtil;
    private WebViewUtils webViewUtils;
    private Language selectedLanguage;
    private InstituteSettings instituteSettings;
    private boolean loadComments = false;

    private RetrofitCall<ApiResponse<FolderListResponse>> bookmarkFoldersLoader;
    private RetrofitCall<Bookmark> bookmarkAPIRequest;
    private RetrofitCall<Void> deleteBookmarkAPIRequest;

    public static ReviewQuestionsFragment getInstance(long reviewItemId, Language selectedLanguage) {
        ReviewQuestionsFragment reviewQuestionsFragment = new ReviewQuestionsFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(ReviewQuestionsFragment.PARAM_REVIEW_ITEM_ID, reviewItemId);
        bundle.putParcelable(PARAM_SELECTED_LANGUAGE, selectedLanguage);
        reviewQuestionsFragment.setArguments(bundle);
        return reviewQuestionsFragment;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiClient = new TestpressExamApiClient(getContext());
        long reviewItemId = getArguments().getLong(PARAM_REVIEW_ITEM_ID);
        Assert.assertNotNull("PARAM_REVIEW_ITEM_ID must not be null", reviewItemId);
        selectedLanguage = getArguments().getParcelable(PARAM_SELECTED_LANGUAGE);
        reviewItemDao = TestpressSDKDatabase.getReviewItemDao(getContext());
        imageUtils = new ImageUtils(rootLayout, this);
        //noinspection ConstantConditions
        instituteSettings = TestpressSdk.getTestpressSession(getContext()).getInstituteSettings();

        List<ReviewItem> reviewItems = reviewItemDao.queryBuilder()
                .where(ReviewItemDao.Properties.Id.eq(reviewItemId)).list();
        if (!reviewItems.isEmpty()) {
            reviewItem = reviewItems.get(0);
        } else {
            setEmptyText(R.string.testpress_error_loading_questions,
                    R.string.testpress_some_thing_went_wrong_try_again);
        }
    }

    @SuppressLint("AddJavascriptInterface")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.testpress_fragment_review_question, container, false);
        progressBar = view.findViewById(R.id.pb_loading);
        emptyView = view.findViewById(R.id.empty_container);
        emptyTitleView = view.findViewById(R.id.empty_title);
        emptyDescView = view.findViewById(R.id.empty_description);
        retryButton = view.findViewById(R.id.retry_button);
        UIUtils.setIndeterminateDrawable(getContext(), progressBar, 4);
        webView = view.findViewById(R.id.web_view);
        bookmarkFolderSpinner = view.findViewById(R.id.bookmark_folder_spinner);
        folderSpinnerAdapter = new FolderSpinnerAdapter(getActivity(), getResources(),
                new ViewUtils.OnInputCompletedListener() {
                    @Override
                    public void onInputComplete(String folderName) {
                        bookmarkFolderSpinner.dismissPopUp();
                        bookmark(folderName);
                    }
                });
        folderSpinnerAdapter.hideSpinner(true);
        bookmarkFolderSpinner.setAdapter(folderSpinnerAdapter);
        bookmarkFolderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> spinner, View view, int position, long itemId) {
                if (position == 0) {
                    return;
                }
                bookmark(folderSpinnerAdapter.getTag(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        animationView = view.findViewById(R.id.move_bookmark_loader);
        animationView.playAnimation();
        difficultyTitle = view.findViewById(R.id.difficulty_title);
        difficultyPercentageText = view.findViewById(R.id.difficulty_percentage);
        usersAnsweredRight = view.findViewById(R.id.users_answered_right);
        imageView1 = view.findViewById(R.id.difficulty1);
        imageView2 = view.findViewById(R.id.difficulty2);
        imageView3 = view.findViewById(R.id.difficulty3);
        imageView4 = view.findViewById(R.id.difficulty4);
        imageView5 = view.findViewById(R.id.difficulty5);
        percentageCorrect = Math.round(reviewItem.getQuestion().getPercentageGotCorrect() == null ?
                0 : reviewItem.getQuestion().getPercentageGotCorrect());

        rootLayout = view;
        ViewUtils.setTypeface(
                new TextView[] { difficultyTitle, difficultyPercentageText },
                TestpressSdk.getRubikMediumFont(getContext())
        );
        usersAnsweredRight.setTypeface(TestpressSdk.getRubikRegularFont(getContext()));
        webViewUtils = new WebViewUtils(webView) {
            @Override
            protected void onLoadFinished() {
                super.onLoadFinished();
                if (getActivity() == null) {
                    return;
                }
                setDifficulty(view);
                progressBar.setVisibility(View.GONE);
                if (commentsUtil == null && loadComments) {
                    commentsUtil = new CommentsUtil(
                            ReviewQuestionsFragment.this,
                            getLoaderManager(),
                            CommentsUtil.getQuestionCommentsUrl(apiClient, reviewItem),
                            rootLayout,
                            ((ReviewQuestionsActivity) getActivity()).buttonLayout
                    );
                    commentsUtil.displayComments();
                }
                animationView.bringToFront();
                webViewUtils.addLogo(instituteSettings.getAppToolbarLogo());
                setHasOptionsMenu(true);
            }

            @Override
            public String getHeader() {
                return getQuestionsHeader() + getBookmarkHandlerScript();
            }

        };
        webView.addJavascriptInterface(new BookmarkListener(), "BookmarkListener");
        difficultyPercentageText.setText(percentageCorrect + "%");
        webViewUtils.initWebView(getReviewItemAsHtml(), getActivity());
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.testpress_share, menu);
        if (instituteSettings.isGrowthHackEnabled()) {
            menu.findItem(R.id.share).setVisible(true);

            if (isAppInstalled("com.whatsapp", getContext())) {
                MenuItem whatsapp = menu.findItem(R.id.whatsapp);
                whatsapp.setVisible(true);
            }

            if (isAppInstalled("org.telegram.messenger", getContext())) {
                MenuItem telegram = menu.findItem(R.id.telegram);
                telegram.setVisible(true);
            }
        }
    }

    private void setDifficulty(View view) {
        if (percentageCorrect >= 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                imageView1.setBackground(getResources().getDrawable(R.drawable.testpress_difficulty_left_on));
            } else {
                imageView1.setBackgroundColor(getResources().getColor(R.color.testpress_difficulty_level_1));
            }
        }
        if (percentageCorrect > 20) {
            imageView2.setBackgroundColor(ContextCompat.getColor(getContext(),
                    R.color.testpress_difficulty_level_2));
        }
        if (percentageCorrect > 40) {
            imageView3.setBackgroundColor(ContextCompat.getColor(getContext(),
                    R.color.testpress_difficulty_level_3));
        }
        if (percentageCorrect > 60) {
            imageView4.setBackgroundColor(ContextCompat.getColor(getContext(),
                    R.color.testpress_difficulty_level_4));
        }
        if (percentageCorrect > 80) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                imageView5.setBackground(getResources().getDrawable(R.drawable.testpress_difficulty_right_on));
            } else {
                imageView5.setBackgroundColor(getResources().getColor(R.color.testpress_difficulty_level_5));
            }
        }
        if (reviewItem.getQuestion().getPercentageGotCorrect() == null) {
            view.findViewById(R.id.difficulty_layout).setVisibility(View.GONE);
        } else {
            view.findViewById(R.id.difficulty_layout).setVisibility(View.VISIBLE);
        }
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
                    htmlContent = getHtml(translation.getDirection(), translation.getQuestionHtml(),
                            translation.getAnswers(), translation.getExplanation(), reviewQuestion.getSubject());
                }
            }
        }
        if (htmlContent == null) {
            htmlContent = getHtml(reviewQuestion.getDirection(), reviewQuestion.getQuestionHtml(),
                    reviewQuestion.getAnswers(), reviewQuestion.getExplanationHtml(), reviewQuestion.getSubject());
        }

        return htmlContent;
    }

    private String getHtml(String directionHtml, String questionHtml,
                           Object answers, String explanationHtml, String subject) {

        String html = "<div class='review-question-container' style='padding-left: 2px; padding-right: 4px;'>";

        // Add index
        html += "<div>" +
                "<div class='review-question-index'>" +
                reviewItem.getIndex() +
                "</div>";

        if (instituteSettings.isBookmarksEnabled()) {
            html += WebViewUtils.getBookmarkButtonWithTags(reviewItem.getBookmarkId() != null);
        }

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
                        optionColor, getContext());

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
            html += "<div class='correct-answer' style='display:box; display:-webkit-box;'>" +
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

    private class BookmarkListener {

        @JavascriptInterface
        public void onClickBookmark() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (reviewItem.getBookmarkId() != null) {
                        deleteBookmark(reviewItem.getBookmarkId());
                    } else {
                        bookmarkFolders.clear();
                        loadBookmarkFolders(instituteSettings.getBaseUrl() + BOOKMARK_FOLDERS_PATH);
                    }
                }
            });
        }
    }

    void loadBookmarkFolders(String url) {
        setBookmarkProgress(true);
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

                            addFoldersToSpinner(bookmarkFolders);
                            setBookmarkProgress(false);
                            bookmarkFolderSpinner.performClick();
                        }

                    }

                    @Override
                    public void onException(TestpressException exception) {
                        handleException(exception);
                    }
                });
    }

    void bookmark(String folder) {
        setBookmarkProgress(true);
        bookmarkAPIRequest = apiClient
                .bookmark(reviewItem.getId(), folder, "userselectedanswer", "exams")
                .enqueue(new TestpressCallback<Bookmark>() {
                    @Override
                    public void onSuccess(Bookmark bookmark) {
                        reviewItem.setBookmarkId(bookmark.getId());
                        reviewItemDao.updateInTx(reviewItem);
                        webViewUtils.updateBookmarkButtonState(true);
                        setBookmarkProgress(false);
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        handleException(exception);
                    }
                });
    }

    void deleteBookmark(Long bookmarkId) {
        setBookmarkProgress(true);
        deleteBookmarkAPIRequest = apiClient.deleteBookmark(bookmarkId)
                .enqueue(new TestpressCallback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        reviewItem.setBookmarkId(null);
                        reviewItemDao.updateInTx(reviewItem);
                        bookmarkFolderSpinner.setSelection(0);
                        webViewUtils.updateBookmarkButtonState(false);
                        setBookmarkProgress(false);
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        handleException(exception);
                    }
                });
    }

    void setBookmarkProgress(boolean show) {
        if (show) {
            webViewUtils.hideBookmarkButton();
            animationView.setVisibility(View.VISIBLE);
        } else {
            animationView.setVisibility(View.GONE);
            webViewUtils.displayBookmarkButton();
        }
    }

    void addFoldersToSpinner(List<BookmarkFolder> bookmarkFolders) {
        folderSpinnerAdapter.clear();
        folderSpinnerAdapter.addHeader("-- Select Folder --");
        for (BookmarkFolder folder: bookmarkFolders) {
            folderSpinnerAdapter.addItem(folder.getName(), folder.getName(), false, 0);
        }
        folderSpinnerAdapter.addItem(null, UNCATEGORIZED, false, 0);
        folderSpinnerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        imageUtils.onActivityResult(requestCode, resultCode, data,
                new ImageUtils.ImagePickerResultHandler() {
                    @Override
                    public void onSuccessfullyImageCropped(CropImage.ActivityResult result) {
                        commentsUtil.uploadImage(result.getUri().getPath());
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {

        imageUtils.permissionsUtils.onRequestPermissionsResult(requestCode, grantResults);
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
        if (commentsUtil != null) {
            commentsUtil.onDestroy();
        }
        CommonUtils.cancelAPIRequests(new RetrofitCall[] {
                bookmarkFoldersLoader, bookmarkAPIRequest, deleteBookmarkAPIRequest
        });
        final ViewGroup viewGroup = (ViewGroup) webView.getParent();
        if (viewGroup != null) {
            // Remove webView from its parent before destroy to support below kitkat
            viewGroup.removeView(webView);
        }
        webView.removeAllViews();
        webView.destroy();
        super.onDestroyView();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (commentsUtil != null) {
            commentsUtil.setUserVisibleHint(isVisibleToUser);
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
        if (imageUtils != null) {
            imageUtils.permissionsUtils.onResume();
        }
        webView.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.share) {
            shareQuestionAsImage(null);
            return true;
        } else if (item.getItemId() == R.id.telegram) {
            shareQuestionAsImage("org.telegram.messenger");
        } else if (item.getItemId() == R.id.whatsapp) {
            shareQuestionAsImage("com.whatsapp");
        } else if (item.getItemId() == R.id.close) {
            getActivity().finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void shareQuestionAsImage(final String package_name) {
        webViewUtils.hideBookmarkButton();
        if (instituteSettings.isGrowthHackEnabled()) {
            webViewUtils.showLogo();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = ImageUtils.getBitmapFromView(webView);
                webViewUtils.displayBookmarkButton();
                webViewUtils.hideLogo();
                ImageUtils.shareBitmap(bitmap, webView.getContext(), package_name);
            }
        }, 100);
    }

    void handleException(TestpressException exception) {
        if (getActivity() == null) {
            return;
        }
        setBookmarkProgress(false);
        ViewUtils.handleException(exception, rootLayout, R.string.testpress_folder_name_not_allowed);
    }

    public void update() {
        webViewUtils.loadHtml(getReviewItemAsHtml());
    }

}
