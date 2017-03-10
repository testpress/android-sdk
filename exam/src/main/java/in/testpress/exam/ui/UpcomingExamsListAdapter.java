package in.testpress.exam.ui;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import in.testpress.core.TestpressSdk;
import in.testpress.exam.R;
import in.testpress.exam.models.Exam;
import in.testpress.util.SingleTypeAdapter;
import in.testpress.util.ViewUtils;

import static in.testpress.exam.ui.TestActivity.PARAM_EXAM;

public class UpcomingExamsListAdapter extends SingleTypeAdapter<Exam> {

    private final Activity activity;

    /**
     * @param activity
     * @param items
     */
    public UpcomingExamsListAdapter(final Activity activity, final List<Exam> items) {
        super(activity, R.layout.testpress_content_list_item);
        setItems(items);
        this.activity = activity;
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[] { R.id.content_title, R.id.white_foreground, R.id.lock,
                R.id.content_item_layout, R.id.exam_info_layout, R.id.attempted_tick, R.id.duration,
                R.id.no_of_questions };
    }

    @Override
    protected void update(final int position, final Exam exam) {
        ViewUtils.setTypeface(new TextView[] {textView(0), textView(6), textView(7)},
                TestpressSdk.getRubikMediumFont(activity));

        setText(0, exam.getTitle());
        setText(6, exam.getDuration());
        setText(7, exam.getNumberOfQuestions().toString() + " Qs");
        setGone(1, true);
        setGone(2, true);
        setGone(4, false);
        setGone(5, true);
        view(3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, TestActivity.class);
                intent.putExtra(PARAM_EXAM, exam);
                activity.startActivity(intent);
            }
        });
    }
}
