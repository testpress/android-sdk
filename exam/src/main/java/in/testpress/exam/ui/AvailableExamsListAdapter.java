package in.testpress.exam.ui;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.List;

import in.testpress.exam.R;
import in.testpress.exam.models.Exam;
import in.testpress.exam.util.SingleTypeAdapter;

public class AvailableExamsListAdapter extends SingleTypeAdapter<Exam> {

    private final Activity activity;
    private final Fragment fragment;

    AvailableExamsListAdapter(final Fragment fragment, final List<Exam> items, int layout) {
        super(fragment.getActivity().getLayoutInflater(), layout);
        this.activity = fragment.getActivity();
        this.fragment = fragment;
        setItems(items);
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[]{R.id.exam_title, R.id.exam_duration, R.id.number_of_questions,
                R.id.exam_date, R.id.course_category};
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        convertView = super.getView(position, convertView, parent);
        return convertView;
    }

    @Override
    protected void update(final int position, final Exam item) {
        final Exam exam = getItem(position);
        setText(0, exam.getTitle());
        setText(1, exam.getDuration());
        setText(2, exam.getNumberOfQuestionsString());
        setText(3, exam.getFormattedStartDate() + " " + getStringFromResource(activity,
                R.string.testpress_to) + " " + exam.getFormattedEndDate());
        setText(4, exam.getCourse_category());
        updater.view.findViewById(R.id.start_exam).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, TestActivity.class);
                intent.putExtra("exam", exam);
                fragment.startActivityForResult(intent, CarouselFragment.TEST_TAKEN_REQUEST_CODE);
            }
        });
        Button emailMcqs = (Button)updater.view.findViewById(R.id.email_mcqs);
        emailMcqs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new EmailPdfDialog(activity, R.style.TestpressAppCompatAlertDialogStyle, false,
                        exam.getUrlFrag()).show();
            }
        });
        if (exam.getAllowPdf()) {
            emailMcqs.setVisibility(View.VISIBLE);
        } else {
            emailMcqs.setVisibility(View.GONE);
        }
    }
}
