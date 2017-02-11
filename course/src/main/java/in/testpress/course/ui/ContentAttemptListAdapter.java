package in.testpress.course.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import in.testpress.core.TestpressSdk;
import in.testpress.course.R;
import in.testpress.course.models.Content;
import in.testpress.exam.TestpressExam;
import in.testpress.exam.models.Attempt;
import in.testpress.exam.models.CourseAttempt;
import in.testpress.exam.models.CourseContent;
import in.testpress.exam.network.TestpressExamApiClient;
import in.testpress.exam.ui.ReviewActivity;
import in.testpress.util.ViewUtils;

class ContentAttemptListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private Activity mActivity;
    private Content mContent;
    private List<CourseAttempt> mAttempts = new ArrayList<>();

    ContentAttemptListAdapter(Activity activity, Content content,
                                     final List<CourseAttempt> attempts) {
        mActivity = activity;
        mContent = content;
        mAttempts = attempts;
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout completedAttemptLayout;
        TextView completedDate;
        TextView correct;
        TextView score;
        TextView reviewLabel;
        LinearLayout pausedAttemptLayout;
        TextView startedDate;
        TextView pausedLabel;
        TextView resumeLabel;

        ViewHolder(View convertView, Context context) {
            super(convertView);
            completedAttemptLayout =
                    (LinearLayout) convertView.findViewById(R.id.completed_attempt_layout);
            completedDate = ((TextView) convertView.findViewById(R.id.completed_date));
            correct = ((TextView) convertView.findViewById(R.id.correct));
            score = ((TextView) convertView.findViewById(R.id.score));
            reviewLabel = ((TextView) convertView.findViewById(R.id.review_label));
            pausedAttemptLayout = (LinearLayout) convertView.findViewById(R.id.paused_attempt_layout);
            startedDate = ((TextView) convertView.findViewById(R.id.started_date));
            pausedLabel = ((TextView) convertView.findViewById(R.id.paused_label));
            resumeLabel = ((TextView) convertView.findViewById(R.id.resume_label));
            ViewUtils.setTypeface(new TextView[] {completedDate, correct, score, reviewLabel,
                    startedDate, pausedLabel, resumeLabel}, TestpressSdk.getRubikRegularFont(context));
        }
    }

    private static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView completedDateLabel;
        TextView correctLabel;
        TextView scoreLabel;
        TextView trophiesLabel;

        HeaderViewHolder(View convertView, Context context) {
            super(convertView);
            completedDateLabel = ((TextView) convertView.findViewById(R.id.date_label));
            correctLabel = ((TextView) convertView.findViewById(R.id.correct_label));
            scoreLabel = ((TextView) convertView.findViewById(R.id.score_label));
            trophiesLabel = ((TextView) convertView.findViewById(R.id.action_label));
            ViewUtils.setTypeface(new TextView[] {completedDateLabel, correctLabel, scoreLabel,
                    trophiesLabel}, TestpressSdk.getRubikMediumFont(context));
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEADER;
        } else {
            return TYPE_ITEM;
        }
    }

    @Override
    public int getItemCount() {
        return mAttempts.size() + 1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.testpress_content_attempt_list_header, parent, false);
            return new HeaderViewHolder(v, mActivity) {};
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.testpress_content_attempt_list_item, parent, false);
            return new ViewHolder(v, mActivity);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof ViewHolder) {
            final ViewHolder holder = (ViewHolder) viewHolder;
            final CourseAttempt courseAttempt = mAttempts.get(position - 1);
            final Attempt attempt = courseAttempt.getAssessment();
            if (attempt.getState().equals(TestpressExamApiClient.STATE_PAUSED)) {
                holder.completedAttemptLayout.setVisibility(View.GONE);
                holder.pausedAttemptLayout.setVisibility(View.VISIBLE);
                holder.startedDate.setText(attempt.getShortDate());
                holder.pausedAttemptLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //noinspection ConstantConditions
                        TestpressExam.resumeCourseAttempt(mActivity,
                                new CourseContent(mContent.getAttemptsUrl(), mContent.getExam()),
                                courseAttempt, false, TestpressSdk.getTestpressSession(mActivity));
                    }
                });
            } else {
                holder.completedDate.setText(attempt.getShortDate());
                holder.correct.setText(attempt.getCorrectCount() + "/" + attempt.getTotalQuestions());
                holder.score.setText(attempt.getScore());
                holder.completedAttemptLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mActivity.startActivity(ReviewActivity.createIntent(mActivity,
                                mContent.getExam(), attempt));
                    }
                });
                holder.completedAttemptLayout.setVisibility(View.VISIBLE);
                holder.pausedAttemptLayout.setVisibility(View.GONE);
            }
        }
    }

}