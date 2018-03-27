package in.testpress.exam.ui;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import in.testpress.core.TestpressSdk;
import in.testpress.exam.R;
import in.testpress.models.greendao.Exam;
import in.testpress.util.SingleTypeAdapter;
import in.testpress.util.ViewUtils;

import static in.testpress.exam.ui.CarouselFragment.TEST_TAKEN_REQUEST_CODE;
import static in.testpress.exam.ui.TestActivity.PARAM_EXAM;

public class AvailableExamsListAdapter extends SingleTypeAdapter<Exam> {

    private final Activity activity;
    private final Fragment fragment;

    AvailableExamsListAdapter(final Fragment fragment, final List<Exam> items) {
        super(fragment.getActivity().getLayoutInflater(), R.layout.testpress_content_list_item);
        this.activity = fragment.getActivity();
        this.fragment = fragment;
        setItems(items);
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[] { R.id.content_title, R.id.white_foreground, R.id.lock,
                R.id.content_item_layout, R.id.exam_info_layout, R.id.attempted_tick,
                R.id.duration, R.id.no_of_questions, R.id.comment_count_layout, R.id.no_of_comments };
    }

    @Override
    protected void update(final int position, final Exam exam) {
        ViewUtils.setTypeface(new TextView[] { textView(0), textView(6), textView(7), textView(9) },
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
                fragment.startActivityForResult(intent, TEST_TAKEN_REQUEST_CODE);
            }
        });
        if (exam.getCommentsCount() == 0) {
            setGone(8, true);
        } else {
            setText(9, exam.getCommentsCount().toString());
            setGone(8, false);
        }
    }

}
