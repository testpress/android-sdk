package in.testpress.exam.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Html;
import android.text.SpannableString;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import in.testpress.core.TestpressSdk;
import in.testpress.exam.R;
import in.testpress.exam.models.Comment;
import in.testpress.exam.network.CommentsPager;
import in.testpress.exam.network.TestpressExamApiClient;
import in.testpress.exam.ui.CommentsListAdapter;
import in.testpress.models.FileDetails;
import in.testpress.models.greendao.ReviewItem;
import in.testpress.network.RetrofitCall;
import in.testpress.network.TestpressApiClient;
import in.testpress.ui.view.BackEventListeningEditText;
import in.testpress.util.CommonUtils;
import in.testpress.util.FormatDate;
import in.testpress.util.ThrowableLoader;
import in.testpress.util.UIUtils;
import in.testpress.util.ViewUtils;
import in.testpress.util.WebViewUtils;

import static com.theartofdev.edmodo.cropper.CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE;
import static in.testpress.exam.network.TestpressExamApiClient.COMMENTS_PATH;
import static in.testpress.exam.network.TestpressExamApiClient.QUESTIONS_PATH;

public class CommentsUtil implements LoaderManager.LoaderCallbacks<List<Comment>> {

    public static final String UPDATE_TIME_SPAN = "updateTimeSpan";
    private static final int NEW_COMMENT_SYNC_INTERVAL = 10000; // 10 sec
    private static final int PREVIOUS_COMMENTS_LOADER_ID = 0;
    private static final int NEW_COMMENTS_LOADER_ID = 1;

    private LinearLayout loadPreviousCommentsLayout;
    private LinearLayout loadNewCommentsLayout;
    private TextView loadPreviousCommentsText;
    private TextView loadNewCommentsText;
    private LinearLayout previousCommentsLoadingLayout;
    private LinearLayout newCommentsLoadingLayout;
    private BackEventListeningEditText commentsEditText;
    private LinearLayout commentBoxLayout;
    private ProgressDialog progressDialog;

    private Activity activity;
    private Fragment fragment;
    private LoaderManager loaderManager;
    private View rootLayout;
    private View bottomNavigationBarLayout;
    private String commentsUrl;

    private CommentsPager previousCommentsPager;
    private CommentsPager newCommentsPager;
    private CommentsListAdapter commentsAdapter;
    private boolean postedNewComment;
    private TestpressExamApiClient apiClient;
    private List<Comment> comments = new ArrayList<>();
    @SuppressLint("UseSparseArrays")
    private HashMap<Integer, Comment> uniqueComments = new HashMap<>();
    private Handler newCommentsHandler;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            //noinspection ArraysAsListWithZeroOrOneArgument
            commentsAdapter.notifyItemRangeChanged(0, commentsAdapter.getItemCount(),
                    UPDATE_TIME_SPAN); // Update the time in comments

            getNewCommentsPager().reset();
            loaderManager.restartLoader(NEW_COMMENTS_LOADER_ID, null, CommentsUtil.this);
        }
    };
    private RetrofitCall<Comment> commentAPIRequest;
    private RetrofitCall<FileDetails> imageUploadAPIRequest;

    public CommentsUtil(Fragment fragment, LoaderManager loaderManager, String commentsUrl,
                        View rootLayout, View bottomNavigationBarLayout) {

        this(fragment.getActivity(), loaderManager, commentsUrl, rootLayout, bottomNavigationBarLayout);
        this.fragment = fragment;
    }

    public CommentsUtil(Activity activity, LoaderManager loaderManager, String commentsUrl,
                        View rootLayout, View bottomNavigationBarLayout) {

        this.activity = activity;
        this.loaderManager = loaderManager;
        this.commentsUrl = commentsUrl;
        this.rootLayout = rootLayout;
        this.bottomNavigationBarLayout = bottomNavigationBarLayout;
        previousCommentsLoadingLayout = rootLayout.findViewById(R.id.loading_previous_comments_layout);
        newCommentsLoadingLayout = rootLayout.findViewById(R.id.loading_new_comments_layout);

        loadPreviousCommentsLayout = rootLayout.findViewById(R.id.load_previous_comments_layout);
        loadPreviousCommentsText = rootLayout.findViewById(R.id.load_previous_comments);
        loadNewCommentsLayout = rootLayout.findViewById(R.id.load_new_comments_layout);
        loadNewCommentsText = rootLayout.findViewById(R.id.load_new_comments_text);
        commentsEditText = rootLayout.findViewById(R.id.comment_box);
        commentBoxLayout = rootLayout.findViewById(R.id.comment_box_layout);
        TextView commentsLabel = rootLayout.findViewById(R.id.comments_label);

        ViewUtils.setTypeface(
                new TextView[] { loadPreviousCommentsText, commentsLabel, loadNewCommentsText },
                TestpressSdk.getRubikMediumFont(activity)
        );

        commentsEditText.setTypeface(TestpressSdk.getRubikRegularFont(activity));

        progressDialog = new ProgressDialog(activity);
        progressDialog.setMessage(activity.getString(R.string.testpress_please_wait));
        progressDialog.setCancelable(false);
        UIUtils.setIndeterminateDrawable(activity, progressDialog, 4);

        apiClient = new TestpressExamApiClient(activity);
    }

    @SuppressLint("SimpleDateFormat")
    CommentsPager getPreviousCommentsPager() {
        if (previousCommentsPager == null) {
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

    @NonNull
    @Override
    public Loader<List<Comment>> onCreateLoader(int loaderId, Bundle args) {
        if (PREVIOUS_COMMENTS_LOADER_ID == loaderId) {
            previousCommentsLoadingLayout.setVisibility(View.VISIBLE);
        } else {
            if (postedNewComment) {
                newCommentsLoadingLayout.setVisibility(View.VISIBLE);
            }
        }
        return new CommentsLoader(this, loaderId);
    }

    private static class CommentsLoader extends ThrowableLoader<List<Comment>> {

        CommentsUtil commentsUtil;
        int loaderId;

        CommentsLoader(CommentsUtil commentsUtil, int loaderId) {
            super(commentsUtil.activity, null);
            this.commentsUtil = commentsUtil;
            this.loaderId = loaderId;
        }

        @Override
        public List<Comment> loadData() throws TestpressException {
            if (PREVIOUS_COMMENTS_LOADER_ID == loaderId) {
                commentsUtil.getPreviousCommentsPager().clearResources().next();
                return commentsUtil.getPreviousCommentsPager().getResources();
            }
            do {
                commentsUtil.getNewCommentsPager().next();
            } while (commentsUtil.getNewCommentsPager().hasNext());
            return commentsUtil.getNewCommentsPager().getResources();
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<Comment>> loader, List<Comment> comments) {
        loaderManager.destroyLoader(loader.getId());
        switch (loader.getId()) {
            case PREVIOUS_COMMENTS_LOADER_ID:
                onPreviousCommentsLoadFinished(loader, comments);
                break;
            case NEW_COMMENTS_LOADER_ID:
                onNewCommentsLoadFinished(loader, comments);
                break;
        }
    }

    public void displayComments() {
        commentsAdapter = new CommentsListAdapter(activity, apiClient);
        RecyclerView commentsListView = rootLayout.findViewById(R.id.comments_list_view);
        commentsListView.setNestedScrollingEnabled(false);
        commentsListView.setLayoutManager(new LinearLayoutManager(activity));
        commentsListView.setAdapter(commentsAdapter);
        View postCommentButton = rootLayout.findViewById(R.id.post_comment_button);
        postCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickSendCommentButton();
            }
        });
        View imageCommentButton = rootLayout.findViewById(R.id.image_comment_button);
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
                bottomNavigationBarLayout.setVisibility(!hasFocus ? View.VISIBLE : View.GONE);
            }
        });
        loadPreviousCommentsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadPreviousCommentsLayout.setVisibility(View.GONE);
                loaderManager
                        .restartLoader(PREVIOUS_COMMENTS_LOADER_ID, null, CommentsUtil.this);
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
                    loaderManager
                            .restartLoader(NEW_COMMENTS_LOADER_ID, null, CommentsUtil.this);
                }
            }
        });
        View commentsLayout = rootLayout.findViewById(R.id.comments_layout);
        commentsLayout.setVisibility(View.VISIBLE);
        loaderManager.initLoader(PREVIOUS_COMMENTS_LOADER_ID, null, CommentsUtil.this);
    }

    private void onPreviousCommentsLoadFinished(Loader<List<Comment>> loader,
                                                List<Comment> previousComments) {

        final TestpressException exception = ThrowableLoader.getException(loader);
        if (exception != null) {
            exception.printStackTrace();
            previousCommentsLoadingLayout.setVisibility(View.GONE);
            // Discard the exception if comments count is 0
           if (exception.isNetworkError()) {
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
            if (fragment != null && fragment.getUserVisibleHint()) {
                newCommentsHandler.postDelayed(runnable, NEW_COMMENT_SYNC_INTERVAL);
            }
        }
    }

    private void onNewCommentsLoadFinished(Loader<List<Comment>> loader, List<Comment> newComments) {
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
                loadNewCommentsText.setText(activity.getResources().getQuantityString(
                        R.plurals.new_comments_available, newCommentsCount, newCommentsCount));

                loadNewCommentsLayout.setVisibility(View.VISIBLE);
            }
        }
        if (postedNewComment) {
            postedNewComment = false;
        }
        newCommentsHandler.postDelayed(runnable, NEW_COMMENT_SYNC_INTERVAL);
    }

    private void onClickSendCommentButton() {
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

    private void postComment(String comment) {
        // Clear edit text focus to display the navigation bar
        commentsEditText.clearFocus(activity);
        commentAPIRequest = apiClient.postComment(commentsUrl, comment)
                .enqueue(new TestpressCallback<Comment>() {
                    @Override
                    public void onSuccess(Comment comment) {
                        if (activity == null) {
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
                        loaderManager.destroyLoader(NEW_COMMENTS_LOADER_ID);
                        loaderManager.restartLoader(NEW_COMMENTS_LOADER_ID, null, CommentsUtil.this);
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        ViewUtils.handleException(exception, rootLayout);
                    }
                });
    }

    private void addComments(List<Comment> commentsList) {
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

    public void uploadImage(String imagePath) {
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
                        ViewUtils.handleException(exception, rootLayout);
                    }
                });
    }

    private void pickImageFromMobile() {
        if (fragment != null) {
            fragment.startActivityForResult(CropImage.getPickImageChooserIntent(activity),
                    PICK_IMAGE_CHOOSER_REQUEST_CODE);
        } else {
            activity.startActivityForResult(CropImage.getPickImageChooserIntent(activity),
                    PICK_IMAGE_CHOOSER_REQUEST_CODE);
        }
    }

    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (isVisibleToUser) {
            if (newCommentsHandler != null) {
                newCommentsHandler.postDelayed(runnable, NEW_COMMENT_SYNC_INTERVAL);
            }
        } else {
            if (commentsEditText != null) {
                // Hide keyboard on user swiped(moved) to the adjacent question
                commentsEditText.clearFocus(activity);
            }
            if (newCommentsHandler != null) {
                newCommentsHandler.removeCallbacks(runnable);
            }
        }
    }

    public void onDestroy() {
        CommonUtils.cancelAPIRequests(new RetrofitCall[] {
                commentAPIRequest, imageUploadAPIRequest
        });
        setUserVisibleHint(false);
    }

    public static String getQuestionCommentsUrl(TestpressExamApiClient apiClient,
                                                ReviewItem reviewItem) {

        return apiClient.getBaseUrl() + QUESTIONS_PATH + reviewItem.getQuestionId() +
                COMMENTS_PATH;
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<Comment>> loader) {
    }
}
