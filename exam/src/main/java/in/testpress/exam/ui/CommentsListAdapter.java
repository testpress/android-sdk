package in.testpress.exam.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSdk;
import in.testpress.exam.R;
import in.testpress.exam.models.Comment;
import in.testpress.exam.models.Vote;
import in.testpress.exam.network.TestpressExamApiClient;
import in.testpress.models.InstituteSettings;
import in.testpress.util.FormatDate;
import in.testpress.util.ImageUtils;
import in.testpress.util.UILImageGetter;
import in.testpress.util.UIUtils;
import in.testpress.util.ViewUtils;
import in.testpress.util.ZoomableImageString;

import static in.testpress.exam.util.CommentsUtil.UPDATE_TIME_SPAN;

public class CommentsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Activity activity;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private List<Comment> comments = new ArrayList<>();
    private TestpressExamApiClient apiClient;
    private ProgressDialog progressDialog;
    private static final int DOWNVOTE = -1;
    private static final int UPVOTE = 1;

    public CommentsListAdapter(Activity activity, TestpressExamApiClient apiClient) {
        this.activity = activity;
        imageLoader = ImageUtils.initImageLoader(activity);
        options = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisk(true).resetViewBeforeLoading(true)
                .showImageOnFail(R.drawable.testpress_profile_image_place_holder)
                .showImageForEmptyUri(R.drawable.testpress_profile_image_place_holder)
                .showImageOnLoading(R.drawable.testpress_profile_image_place_holder).build();
        this.apiClient = apiClient;
        progressDialog = new ProgressDialog(activity);
        progressDialog.setMessage(activity.getString(R.string.testpress_please_wait));
        progressDialog.setCancelable(false);
    }

    private class CommentsViewHolder extends RecyclerView.ViewHolder {
        TextView userName;
        TextView comment;
        ImageView userImage;
        TextView submitDate;
        ImageButton upvoteButton;
        ImageButton downvoteButton;
        TextView voteCount;
        View divider;
        LinearLayout voteLayout;

        CommentsViewHolder(View convertView) {
            super(convertView);
            divider = convertView.findViewById(R.id.comment_seperator);
            userName = ((TextView) convertView.findViewById(R.id.user_name));
            comment = ((TextView) convertView.findViewById(R.id.comment));
            userImage = ((ImageView) convertView.findViewById(R.id.display_picture));
            submitDate = ((TextView) convertView.findViewById(R.id.submit_date));
            upvoteButton = ((ImageButton) convertView.findViewById(R.id.upvote_button));
            downvoteButton = (ImageButton) convertView.findViewById(R.id.downvote_button);
            voteCount = ((TextView) convertView.findViewById(R.id.vote_count));
            voteLayout = ((LinearLayout) convertView.findViewById(R.id.vote_layout));
            ViewUtils.setTypeface(new TextView[] {submitDate, comment},
                    TestpressSdk.getRubikRegularFont(activity));

            ViewUtils.setTypeface(new TextView[] {userName, voteCount},
                    TestpressSdk.getRubikMediumFont(activity));

            upvoteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    voteComment(getLayoutPosition(), view, UPVOTE);
                }
            });
            downvoteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    voteComment(getLayoutPosition(), view, DOWNVOTE);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.testpress_comments_list_item, parent, false);

        return new CommentsViewHolder(v);
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        if (viewHolder instanceof CommentsViewHolder) {
            final CommentsViewHolder holder = (CommentsViewHolder) viewHolder;
            final Comment comment = comments.get(position);
            imageLoader.displayImage(comment.getUser().getMediumImage(), holder.userImage, options);
            if (comment.getUser().getMediumImage().isEmpty()) {
                holder.userImage.setColorFilter(Color.parseColor("#888888"));
            } else {
                holder.userImage.clearColorFilter();
            }
            holder.userName.setText(comment.getUser().getDisplayName());
            Spanned htmlSpan = Html.fromHtml(comment.getComment(),
                    new UILImageGetter(holder.comment, activity), null);

            holder.comment.setText(ZoomableImageString.convertString(htmlSpan, activity, false));
            holder.comment.setMovementMethod(LinkMovementMethod.getInstance());

            updateTimeSpan(comment, holder);
            //noinspection ConstantConditions
            InstituteSettings instituteSettings =
                    TestpressSdk.getTestpressSession(activity).getInstituteSettings();

            if (instituteSettings.isCommentsVotingEnabled()) {
                holder.voteCount.setText(String.valueOf(comment.getUpvotes() - comment.getDownvotes()));
                int grayColor = ContextCompat.getColor(activity, R.color.testpress_text_gray_medium);
                int primaryColor = ContextCompat.getColor(activity, R.color.testpress_color_primary);
                holder.voteCount.setTextColor(grayColor);
                if (comment.getVoteId() != null) {
                    if (comment.getTypeOfVote() == 1) {
                        holder.upvoteButton.setColorFilter(primaryColor);
                        holder.downvoteButton.setColorFilter(grayColor);
                    } else {
                        holder.downvoteButton.setColorFilter(primaryColor);
                        holder.upvoteButton.setColorFilter(grayColor);
                    }
                } else {
                    holder.upvoteButton.setColorFilter(grayColor);
                    holder.downvoteButton.setColorFilter(grayColor);
                }
            } else {
                holder.voteLayout.setVisibility(View.GONE);
            }
        }
    }

    private void voteComment(final int position, View view, int typeOfVote) {
        final Comment comment = comments.get(position);
        if (isSelfVote(comment.getUser().getId())) {
            UIUtils.showSnackBar(view, R.string.testpress_self_vote_error);
            return;
        }
        progressDialog.show();
        if (comment.getVoteId() == null) {
            apiClient.voteComment(comment, typeOfVote)
                    .enqueue(new TestpressCallback<Vote<Comment>>() {
                        @Override
                        public void onSuccess(Vote<Comment> vote) {
                            onVoteCasted(position, vote);
                        }

                        @Override
                        public void onException(TestpressException exception) {
                            handleException(exception, comment);
                        }
                    });
        } else {
            if (comment.getTypeOfVote() == typeOfVote) {
                deleteVote(comment, position);
            } else {
                apiClient.updateCommentVote(comment, typeOfVote)
                        .enqueue(new TestpressCallback<Vote<Comment>>() {
                            @Override
                            public void onSuccess(Vote<Comment> vote) {
                                onVoteCasted(position, vote);
                            }

                            @Override
                            public void onException(TestpressException exception) {
                                handleException(exception, comment);
                            }
                        });
            }
        }
    }

    private void onVoteCasted(int position, Vote<Comment> vote) {
        UIUtils.showSnackBar(activity.findViewById(android.R.id.content),
                R.string.testpress_vote_casted);

        comments.set(position, vote.getContentObject());
        notifyItemChanged(position);
        progressDialog.dismiss();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position,
                                 List<Object> payloads) {

        if (payloads.isEmpty() || !payloads.get(0).equals(UPDATE_TIME_SPAN)) {
            // Perform a full update
            onBindViewHolder(viewHolder, position);
        } else { // Update time span only
            updateTimeSpan(comments.get(position), (CommentsViewHolder) viewHolder);
        }
    }

    private void updateTimeSpan(Comment comment, CommentsViewHolder holder) {
        //noinspection ConstantConditions
        long submitDateMillis = FormatDate.getDate(comment.getSubmitDate(),
                "yyyy-MM-dd'T'HH:mm:ss", "UTC").getTime();

        holder.submitDate.setText(FormatDate.getAbbreviatedTimeSpan(submitDateMillis));
    }

    public void setComments(List<Comment> comments) {
        this.comments = new ArrayList<>(comments);
        notifyDataSetChanged();
    }

    public void addComments(List<Comment> comments) {
        this.comments.addAll(comments);
        notifyItemRangeInserted(getItemCount(), comments.size());
    }

    private void deleteVote(final Comment comment, final int position) {
        apiClient.deleteCommentVote(comment)
                .enqueue(new TestpressCallback<String>() {
                    @Override
                    public void onSuccess(String response) {
                        if (comment.getTypeOfVote() == 1) {
                            comment.setUpvotes(comment.getUpvotes() - 1);
                        } else {
                            comment.setDownvotes(comment.getDownvotes() - 1);
                        }
                        comment.setTypeOfVote(null);
                        comment.setVoteId(null);
                        notifyItemChanged(position);
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        handleException(exception, comment);
                    }
                });
    }

    private void handleException(TestpressException exception, Comment comment) {

        int error;
        if (exception.isUnauthenticated()) {
            error = R.string.testpress_authentication_failed;
        } else if (exception.getCause() instanceof IOException) {
            error = R.string.testpress_no_internet_try_again;
        } else if (exception.getResponse().code() == 400) {
            error = R.string.testpress_self_vote_error;
            if (TestpressSdk.getTestpressUserId(activity) != comment.getUser().getId()) {
                TestpressSdk.setTestpressUserId(activity, comment.getUser().getId());
            }
        } else {
            error = R.string.testpress_some_thing_went_wrong_try_again;
        }
        UIUtils.showSnackBar(activity.findViewById(android.R.id.content), error);
        progressDialog.dismiss();
    }

    private boolean isSelfVote(int id) {
        return TestpressSdk.isTestpressUserIdExist(activity) && (id == TestpressSdk.getTestpressUserId(activity));
    }
}