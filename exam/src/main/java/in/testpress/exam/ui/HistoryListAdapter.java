package in.testpress.exam.ui;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.view.View;

import java.util.List;

import in.testpress.exam.R;
import in.testpress.exam.models.Exam;
import in.testpress.exam.util.SingleTypeAdapter;

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
        updater.view.findViewById(R.id.review_attempt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (exam.getAttemptsCount() == 1 && exam.getPausedAttemptsCount() == 0) {
                    // ToDo: Go to ReviewActivity
                } else {
                    Intent intent = new Intent(activity, AttemptsListActivity.class);
                    intent.putExtra(AttemptsListFragment.PARAM_EXAM, exam);
                    fragment.startActivityForResult(intent, CarouselFragment.TEST_TAKEN_REQUEST_CODE);
                }
            }
        });
        updater.view.findViewById(R.id.retake).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, TestActivity.class);
                intent.putExtra(TestActivity.PARAM_EXAM, exam);
                fragment.startActivityForResult(intent, CarouselFragment.TEST_TAKEN_REQUEST_CODE);
            }
        });
        updater.view.findViewById(R.id.resume_exam).setOnClickListener(new View.OnClickListener() {
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
        setGone(8, (exam.getPausedAttemptsCount() <= 0));
    }
}
