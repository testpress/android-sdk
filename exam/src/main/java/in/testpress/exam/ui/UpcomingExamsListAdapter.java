package in.testpress.exam.ui;

import android.app.Activity;

import java.util.List;

import in.testpress.exam.R;
import in.testpress.exam.models.Exam;
import in.testpress.exam.util.SingleTypeAdapter;

public class UpcomingExamsListAdapter extends SingleTypeAdapter<Exam> {

    private final Activity activity;

    /**
     * @param activity
     * @param items
     */
    public UpcomingExamsListAdapter(final Activity activity, final List<Exam> items, int layout) {
        super(activity, layout);
        setItems(items);
        this.activity = activity;
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[]{R.id.exam_title, R.id.exam_duration,
                R.id.number_of_questions, R.id.exam_date};
    }

    @Override
    protected void update(final int position, final Exam item) {
        setText(0, item.getTitle());
        setText(1, item.getDuration());
        setText(2, item.getNumberOfQuestionsString());
        setText(3, item.getFormattedStartDate() + " " + getStringFromResource(activity, R.string.testpress_to)
                + " " + item.getFormattedEndDate());
    }
}
