package in.testpress.exam.ui;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.List;

import in.testpress.exam.R;
import in.testpress.exam.models.Exam;
import in.testpress.exam.util.SingleTypeAdapter;

public class AvailableExamsListAdapter extends SingleTypeAdapter<Exam> {

    private final Activity activity;

    public AvailableExamsListAdapter(final Activity activity, final List<Exam> items, int layout) {
        super(activity.getLayoutInflater(), layout);
        this.activity = activity;
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
        setText(3, exam.getFormattedStartDate() + " " + getStringFromResource(activity, R.string.testpress_to)
                + " " + exam.getFormattedEndDate());
        setText(4, exam.getCourse_category());
        updater.view.findViewById(R.id.start_exam).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ToDo : Go to ExamActivity
            }
        });
        Button emailMcqs = (Button)updater.view.findViewById(R.id.email_mcqs);
        emailMcqs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ToDo : Email Mcqs
            }
        });
        if (exam.getAllowPdf()) {
            emailMcqs.setVisibility(View.VISIBLE);
        } else {
            emailMcqs.setVisibility(View.GONE);
        }
    }
}
