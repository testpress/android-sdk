package in.testpress.exam.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import in.testpress.core.TestpressSdk;

import in.testpress.exam.R;
import in.testpress.exam.TestpressExam;
import in.testpress.exam.models.Attempt;
import in.testpress.exam.models.Exam;
import in.testpress.exam.network.TestpressExamApiClient;
import in.testpress.util.ViewUtils;

class AttemptListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private Activity mActivity;
    private Exam mExam;
    private List<Attempt> mAttempts = new ArrayList<>();

    AttemptListAdapter(Activity activity, Exam exam, List<Attempt> attempts) {
        mActivity = activity;
        mExam = exam;
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
                    .inflate(R.layout.testpress_attempt_list_header, parent, false);
            return new HeaderViewHolder(v, mActivity) {};
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.testpress_attempt_list_item, parent, false);
            return new ViewHolder(v, mActivity);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof ViewHolder) {
            final ViewHolder holder = (ViewHolder) viewHolder;
            final Attempt attempt = mAttempts.get(position - 1);
            if (attempt.getState().equals(TestpressExamApiClient.STATE_PAUSED)) {
                holder.completedAttemptLayout.setVisibility(View.GONE);
                holder.pausedAttemptLayout.setVisibility(View.VISIBLE);
                holder.startedDate.setText(attempt.getShortDate());
                holder.pausedAttemptLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mActivity, TestActivity.class);
                        intent.putExtra(TestActivity.PARAM_EXAM, mExam);
                        intent.putExtra(TestActivity.PARAM_ATTEMPT, attempt);
                        mActivity.startActivityForResult(intent,
                                CarouselFragment.TEST_TAKEN_REQUEST_CODE);
                    }
                });
            } else {
                holder.completedDate.setText(attempt.getShortDate());
                holder.correct.setText(attempt.getCorrectCount() + "/" + attempt.getTotalQuestions());
                holder.score.setText(attempt.getScore());
                holder.completedAttemptLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //noinspection ConstantConditions
                        TestpressExam.showAttemptReport(mActivity, mExam, attempt,
                                TestpressSdk.getTestpressSession(mActivity));
                    }
                });
                holder.completedAttemptLayout.setVisibility(View.VISIBLE);
                holder.pausedAttemptLayout.setVisibility(View.GONE);
            }
        }
    }

}