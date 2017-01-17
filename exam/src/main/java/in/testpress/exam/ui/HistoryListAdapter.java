package in.testpress.exam.ui;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Button;

import java.util.List;

import in.testpress.exam.R;
import in.testpress.exam.models.Exam;
import in.testpress.util.SingleTypeAdapter;
import in.testpress.util.ViewUtils;

public class HistoryListAdapter extends SingleTypeAdapter<Exam> {

    private final Activity activity;
    private final Fragment fragment;

    /**
     * @param fragment
     * @param items
     */
    public HistoryListAdapter(final Fragment fragment, final List<Exam> items, int layout) {
        super(fragment.getActivity().getLayoutInflater(), layout);
        this.activity = fragment.getActivity();
        this.fragment = fragment;
        setItems(items);
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[]{R.id.exam_title, R.id.exam_duration, R.id.number_of_questions,
                R.id.exam_date, R.id.attempts_count, R.id.attempts_string, R.id.retake,
                R.id.review_attempt, R.id.resume_exam};
    }

    @Override
    protected void update(final int position, final Exam item) {
        final Exam exam = getItem(position);
        Button reviewButton = (Button) updater.view.findViewById(R.id.review_attempt);
        ViewUtils.setLeftDrawable(activity, reviewButton, R.drawable.ic_zoom_in_white_18dp);
        reviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (exam.getAttemptsCount() == 1 && exam.getPausedAttemptsCount() == 0) {
                    activity.startActivity(ReviewActivity.createIntent(activity, exam, null));
                } else {
                    Intent intent = new Intent(activity, AttemptsListActivity.class);
                    intent.putExtra(AttemptsListFragment.PARAM_EXAM, exam);
                    fragment.startActivityForResult(intent, CarouselFragment.TEST_TAKEN_REQUEST_CODE);
                }
            }
        });
        Button retakeButton = (Button) updater.view.findViewById(R.id.retake);
        ViewUtils.setLeftDrawable(activity, retakeButton, R.drawable.ic_replay_white_18dp);
        retakeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, TestActivity.class);
                intent.putExtra(TestActivity.PARAM_EXAM, exam);
                fragment.startActivityForResult(intent, CarouselFragment.TEST_TAKEN_REQUEST_CODE);
            }
        });
        Button resumeButton = (Button) updater.view.findViewById(R.id.resume_exam);
        ViewUtils.setLeftDrawable(activity, resumeButton, R.drawable.ic_repeat_white_18dp);
        resumeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, AttemptsListActivity.class);
                intent.putExtra(AttemptsListFragment.PARAM_EXAM, exam);
                intent.putExtra(AttemptsListFragment.PARAM_STATE, AttemptsListFragment.STATE_PAUSED);
                fragment.startActivityForResult(intent, CarouselFragment.TEST_TAKEN_REQUEST_CODE);
            }
        });
        setText(0, exam.getTitle());
        setText(1, exam.getDuration());
        setText(2, exam.getNumberOfQuestionsString());
        setText(3, exam.getFormattedStartDate() + " " + getStringFromResource(activity, R.string.testpress_to)
                + " " + exam.getFormattedEndDate());
        setText(4, "" + exam.getAttemptsCount());
        setText(5, (exam.getAttemptsCount() > 1) ? getStringFromResource(activity,
                R.string.testpress_attempts) : getStringFromResource(activity, R.string.testpress_attempt));

        if (!exam.getAllowRetake()) {
            setGone(6, true);
        } else if (exam.getMaxRetakes() > 0 && exam.getAttemptsCount() >= exam.getMaxRetakes() + 1) {
            setGone(6, true);
        } else if (exam.getPausedAttemptsCount() > 0) {
            setGone(6, true);
        } else {
            setGone(6, false);
            setText(6, R.string.testpress_retake);
        }
        setGone(7, !exam.getShowAnswers());
        setGone(8, (exam.getPausedAttemptsCount() <= 0));
    }

}
