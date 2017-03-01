package in.testpress.exam.ui;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.List;

import in.testpress.exam.R;
import in.testpress.exam.models.Exam;
import in.testpress.util.SingleTypeAdapter;
import in.testpress.util.ViewUtils;

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
                R.id.exam_date, R.id.course_category, R.id.course_category_layout,
                R.id.web_only_label};
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        convertView = super.getView(position, convertView, parent);
        return convertView;
    }

    @Override
    protected void update(final int position, final Exam exam) {
        setText(0, exam.getTitle());
        setText(1, exam.getDuration());
        setText(2, exam.getNumberOfQuestionsString());
        setText(3, exam.getFormattedStartDate() + " " + getStringFromResource(activity,
                R.string.testpress_to) + " " + exam.getFormattedEndDate());
        if (TextUtils.isEmpty(exam.getCourse_category())) {
            setGone(5, true);
        } else {
            setGone(5, false);
            setText(4, exam.getCourse_category());
        }
        Button startExamButton = (Button)updater.view.findViewById(R.id.start_exam);
        Button emailMcqButton = (Button)updater.view.findViewById(R.id.email_mcqs);
        // Display start exam button only if exam can be taken in mobile
        if (exam.getDeviceAccessControl().equals("web")) {
            setGone(6, false);
            view(6).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(activity, TestActivity.class);
                    intent.putExtra("exam", exam);
                    fragment.startActivityForResult(intent, CarouselFragment.TEST_TAKEN_REQUEST_CODE);
                }
            });
            startExamButton.setVisibility(View.GONE);
            emailMcqButton.setVisibility(View.GONE);
        } else {
            setGone(6, true);
            ViewUtils.setLeftDrawable(activity, startExamButton, R.drawable.ic_assignment_white_18dp);
            startExamButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(activity, TestActivity.class);
                    intent.putExtra("exam", exam);
                    fragment.startActivityForResult(intent, CarouselFragment.TEST_TAKEN_REQUEST_CODE);
                }
            });
            startExamButton.setVisibility(View.VISIBLE);
            // Validate email mcq button
            if (exam.getAllowPdf()) {
                ViewUtils.setLeftDrawable(activity, emailMcqButton, R.drawable.ic_email_white_18dp);
                emailMcqButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new EmailPdfDialog(activity, R.style.TestpressAppCompatAlertDialogStyle, false,
                                exam.getUrlFrag()).show();
                    }
                });
                emailMcqButton.setVisibility(View.VISIBLE);
            } else {
                emailMcqButton.setVisibility(View.GONE);
            }
        }
    }

}
