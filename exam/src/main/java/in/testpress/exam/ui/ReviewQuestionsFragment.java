package in.testpress.exam.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.theartofdev.edmodo.cropper.CropImage;

import junit.framework.Assert;

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
import in.testpress.core.TestpressSdk;
import in.testpress.exam.R;
import in.testpress.exam.TestpressExam;
import in.testpress.exam.models.Comment;
import in.testpress.exam.models.Language;
import in.testpress.exam.models.greendao.ReviewAnswer;
import in.testpress.exam.models.greendao.ReviewAnswerTranslation;
import in.testpress.exam.models.greendao.ReviewItem;
import in.testpress.exam.models.greendao.ReviewItemDao;
import in.testpress.exam.models.greendao.ReviewQuestion;
import in.testpress.exam.models.greendao.ReviewQuestionTranslation;
import in.testpress.exam.network.CommentsPager;
import in.testpress.exam.network.TestpressExamApiClient;
import in.testpress.model.FileDetails;
import in.testpress.network.TestpressApiClient;
import in.testpress.ui.view.BackEventListeningEditText;
import in.testpress.util.FormatDate;
import in.testpress.util.ThrowableLoader;
import in.testpress.util.UIUtils;
import in.testpress.util.ViewUtils;
import in.testpress.util.WebViewUtils;

import static android.app.Activity.RESULT_OK;
import static com.theartofdev.edmodo.cropper.CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE;
import static com.theartofdev.edmodo.cropper.CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE;
import static com.theartofdev.edmodo.cropper.CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE;
import static com.theartofdev.edmodo.cropper.CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE;

public class ReviewQuestionsFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<List<Comment>> {

    public static final String UPDATE_TIME_SPAN = "updateTimeSpan";
    private static final int NEW_COMMENT_SYNC_INTERVAL = 10000; // 10 sec
    private static final int PREVIOUS_COMMENTS_LOADER_ID = 0;
    private static final int NEW_COMMENTS_LOADER_ID = 1;
    static final String PARAM_REVIEW_ITEM_ID = "reviewItemId";
    static final String PARAM_SELECTED_LANGUAGE = "selectedLanguage";
    private ReviewItem reviewItem;
    private View emptyView;
    private TextView emptyTitleView;
    private TextView emptyDescView;
    private Button retryButton;
    private ProgressBar progressBar;
    LinearLayout commentsLayout;
    LinearLayout previousCommentsLoadingLayout;
    LinearLayout newCommentsLoadingLayout;
    RecyclerView commentsListView;
    LinearLayout loadPreviousCommentsLayout;
    TextView loadPreviousCommentsText;
    LinearLayout loadNewCommentsLayout;
    TextView loadNewCommentsText;
    TextView commentsLabel;
    BackEventListeningEditText commentsEditText;
    ImageButton postCommentButton;
    ImageButton imageCommentButton;
    View rootLayout;
    LinearLayout commentBoxLayout;
    CommentsPager previousCommentsPager;
    CommentsPager newCommentsPager;
    CommentsListAdapter commentsAdapter;
    ProgressDialog progressDialog;
    WebView webView;
    boolean postedNewComment;
    TestpressExamApiClient apiClient;
    List<Comment> comments = new ArrayList<>();
    @SuppressLint("UseSparseArrays")
    HashMap<Integer, Comment> uniqueComments = new HashMap<>();
    Uri selectedCommentImageUri;
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
            getLoaderManager().restartLoader(NEW_COMMENTS_LOADER_ID, null, ReviewQuestionsFragment.this);
        }
    };

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
        ReviewItemDao reviewItemDao= TestpressExam.getReviewItemDao(getContext());
        List<ReviewItem> reviewItems = reviewItemDao.queryBuilder()
                .where(ReviewItemDao.Properties.Id.eq(reviewItemId)).list();
        if (!reviewItems.isEmpty()) {
            reviewItem = reviewItems.get(0);
        } else {
            setEmptyText(R.string.testpress_error_loading_questions,
                    R.string.testpress_some_thing_went_wrong_try_again);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.testpress_fragment_review_question, container, false);
        progressBar = (ProgressBar) view.findViewById(R.id.pb_loading);
        emptyView = view.findViewById(R.id.empty_container);
        emptyTitleView = (TextView) view.findViewById(R.id.empty_title);
        emptyDescView = (TextView) view.findViewById(R.id.empty_description);
        retryButton = (Button) view.findViewById(R.id.retry_button);
        UIUtils.setIndeterminateDrawable(getContext(), progressBar, 4);
        webView = (WebView) view.findViewById(R.id.web_view);
        commentsLayout = (LinearLayout) view.findViewById(R.id.comments_layout);
        previousCommentsLoadingLayout = (LinearLayout) view.findViewById(R.id.loading_previous_comments_layout);
        newCommentsLoadingLayout = (LinearLayout) view.findViewById(R.id.loading_new_comments_layout);
        commentsListView = (RecyclerView) view.findViewById(R.id.comments_list_view);
        loadPreviousCommentsLayout = (LinearLayout) view.findViewById(R.id.load_previous_comments_layout);
        loadPreviousCommentsText = (TextView) view.findViewById(R.id.load_previous_comments);
        loadNewCommentsLayout = (LinearLayout) view.findViewById(R.id.load_new_comments_layout);
        loadNewCommentsText = (TextView) view.findViewById(R.id.load_new_comments_text);
        commentsLabel = (TextView) view.findViewById(R.id.comments_label);
        commentsEditText = (BackEventListeningEditText) view.findViewById(R.id.comment_box);
        commentBoxLayout = (LinearLayout) view.findViewById(R.id.comment_box_layout);
        postCommentButton = (ImageButton) view.findViewById(R.id.post_comment_button);
        imageCommentButton = (ImageButton) view.findViewById(R.id.image_comment_button);
        rootLayout = view;
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage(getResources().getString(R.string.testpress_please_wait));
        progressDialog.setCancelable(false);
        UIUtils.setIndeterminateDrawable(getContext(), progressDialog, 4);
        ViewUtils.setTypeface(
                new TextView[] { loadPreviousCommentsText, commentsLabel, loadNewCommentsText },
                TestpressSdk.getRubikMediumFont(getContext())
        );
        ViewUtils.setTypeface(new TextView[] { commentsEditText },
                TestpressSdk.getRubikRegularFont(getContext()));
        webViewUtils = new WebViewUtils(webView) {
            @Override
            protected void onLoadFinished() {
                super.onLoadFinished();
                progressBar.setVisibility(View.GONE);
                if (commentsAdapter == null && getActivity() != null) {
                    displayComments();
                }
            }
        };
        webViewUtils.initWebView(getReviewItemAsHtml(), getActivity());
        return view;
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
                            translation.getAnswers(), translation.getExplanation());
                }
            }
        }
        if (htmlContent == null) {
            htmlContent = getHtml(reviewQuestion.getDirection(), reviewQuestion.getQuestionHtml(),
                    reviewQuestion.getAnswers(), reviewQuestion.getExplanationHtml());
        }
        return htmlContent;
    }

    private String getHtml(String directionHtml, String questionHtml,
                           Object answers, String explanationHtml) {

        String html = "<div style='padding-left: 2px; padding-right: 4px;'>";

        // Add index
        html += "<div>" +
                "<div class='review-question-index'>" +
                reviewItem.getIndex() +
                "</div>";

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
        html += "<div style='display:block;'>" +
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
        return html + "</div>";
    }

    void displayComments() {
        commentsAdapter = new CommentsListAdapter(getActivity());
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
                ((ReviewQuestionsActivity) getActivity()).setNavigationBarVisible(!hasFocus);
            }
        });
        loadPreviousCommentsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadPreviousCommentsLayout.setVisibility(View.GONE);
                getLoaderManager()
                        .restartLoader(PREVIOUS_COMMENTS_LOADER_ID, null, ReviewQuestionsFragment.this);
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
                            .restartLoader(NEW_COMMENTS_LOADER_ID, null, ReviewQuestionsFragment.this);
                }
            }
        });
        commentsLayout.setVisibility(View.VISIBLE);
        getLoaderManager().initLoader(PREVIOUS_COMMENTS_LOADER_ID, null, ReviewQuestionsFragment.this);
    }

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
            case NEW_COMMENTS_LOADER_ID:
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
            default:
                //An invalid id was passed
                return null;
        }
    }

    @SuppressLint("SimpleDateFormat")
    CommentsPager getPreviousCommentsPager() {
        if (previousCommentsPager == null) {
            String commentsUrl = reviewItem.getQuestion().getCommentsUrl();
            previousCommentsPager = new CommentsPager(commentsUrl, apiClient);
            previousCommentsPager.queryParams.put(TestpressApiClient.ORDER, "-submit_date");
            // Query comments till now to paginate afterwards
            previousCommentsPager.queryParams.put(TestpressApiClient.UNTIL,
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ").format(new Date()));
        }
        return previousCommentsPager;
    }

    CommentsPager getNewCommentsPager() {
        if (newCommentsPager == null) {
            String commentsUrl = reviewItem.getQuestion().getCommentsUrl();
            newCommentsPager = new CommentsPager(commentsUrl, apiClient);
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
    public void onLoadFinished(Loader<List<Comment>> loader, List<Comment> comments) {
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
            if (reviewItem.getCommentsCount() == 0) {
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
            // Update the comments count if need
            if (reviewItem.getCommentsCount() < getPreviousCommentsPager().getCommentsCount()) {
                reviewItem.setCommentsCount(getPreviousCommentsPager().getCommentsCount());
            }
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
            // Update comments count
            int noOfComments = reviewItem.getCommentsCount() +
                    getNewCommentsPager().getCommentsCount();

            reviewItem.setCommentsCount(noOfComments);
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
        apiClient.postComment(reviewItem.getQuestion().getCommentsUrl(), comment)
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
                                ReviewQuestionsFragment.this);
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        handleExceptionOnPostComment(exception);
                    }
                });
    }

    public void pickImageFromMobile() {
        startActivityForResult(CropImage.getPickImageChooserIntent(getContext()),
                PICK_IMAGE_CHOOSER_REQUEST_CODE);
    }

    @Override
    @SuppressLint("NewApi")
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri imageUri = CropImage.getPickImageResultUri(getContext(), data);
            // For API >= 23 we need to check specifically that we have permissions to read external storage.
            if (CropImage.isReadExternalStoragePermissionsRequired(getContext(), imageUri)) {
                // Request permission
                selectedCommentImageUri = imageUri;
                requestPermissions(new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                        PICK_IMAGE_PERMISSIONS_REQUEST_CODE);
            } else {
                // No permissions required or already grunted
                startCropImageActivity(imageUri);
            }
        } else if (requestCode == CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                uploadImage(result.getUri().getPath());
            } else if (resultCode == CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                //noinspection ThrowableResultOfMethodCallIgnored
                Exception exception = result.getError();
                Snackbar.make(rootLayout, exception.getMessage(), Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    void startCropImageActivity(Uri imageUri) {
        CropImage.activity(imageUri)
                .setAllowFlipping(false)
                .start(getContext(), this);
    }

    void uploadImage(String imagePath) {
        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }
        apiClient.upload(imagePath).enqueue(new TestpressCallback<FileDetails>() {
            @Override
            public void onSuccess(FileDetails fileDetails) {
                postComment(WebViewUtils.appendImageTags(fileDetails.getUrl()));
            }

            @Override
            public void onException(TestpressException exception) {
                handleExceptionOnPostComment(exception);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {

        if (requestCode == PICK_IMAGE_PERMISSIONS_REQUEST_CODE) {
            if (selectedCommentImageUri == null ||
                    (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {

                // Permission granted show image picker
                startCropImageActivity(selectedCommentImageUri);
            } else {
                Snackbar.make(rootLayout, R.string.action_cant_done_without_permission,
                        Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    void addComments(List<Comment> commentsList) {
        for (Comment comment : commentsList) {
            uniqueComments.put(comment.getId(), comment);
        }
        comments = new ArrayList<>(uniqueComments.values());
        Collections.sort(this.comments, new Comparator<Comment>() {
            @Override
            public int compare(Comment o1, Comment o2) {
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
        } else {
            if (commentsEditText != null) {
                // Hide keyboard on user swiped(moved) to the adjacent question
                commentsEditText.clearFocus(getActivity());
            }
            if (newCommentsHandler != null) {
                newCommentsHandler.removeCallbacks(runnable);
            }
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
        webView.onResume();
    }

    void handleExceptionOnPostComment(TestpressException exception) {
        progressDialog.dismiss();
        if(exception.isUnauthenticated()) {
            Snackbar.make(rootLayout, R.string.testpress_authentication_failed,
                    Snackbar.LENGTH_SHORT).show();
        } else if (exception.getCause() instanceof IOException) {
            Snackbar.make(rootLayout, R.string.testpress_no_internet_connection,
                    Snackbar.LENGTH_SHORT).show();
        } else {
            Snackbar.make(rootLayout, R.string.testpress_network_error,
                    Snackbar.LENGTH_SHORT).show();
        }
    }

    public void update() {
        webViewUtils.loadHtml(getReviewItemAsHtml());
    }

    @Override
    public void onLoaderReset(Loader<List<Comment>> loader) {
    }

}
