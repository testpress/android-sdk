package in.testpress.exam.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import in.testpress.core.TestpressSdk;
import in.testpress.exam.R;
import in.testpress.exam.models.CourseAttempt;
import in.testpress.models.greendao.Exam;
import in.testpress.util.Assert;
import in.testpress.util.ViewUtils;

import static in.testpress.exam.ui.ReviewStatsActivity.PARAM_COURSE_ATTEMPT;
import static in.testpress.exam.ui.ReviewStatsActivity.PARAM_EXAM;

public class TrophiesAchievedFragment extends Fragment {

    private CourseAttempt courseAttempt;
    private Exam exam;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        exam = getArguments().getParcelable(PARAM_EXAM);
        Assert.assertNotNull("PARAM_EXAM must not be null.", exam);
        courseAttempt = getArguments().getParcelable(PARAM_COURSE_ATTEMPT);
        Assert.assertNotNull("CourseAttempt must not be null.", courseAttempt);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.testpress_fragment_trophies_achieved, container, false);
        TextView trophiesText = (TextView) view.findViewById(R.id.trophies);
        TextView sign = (TextView) view.findViewById(R.id.sign);
        TextView trophiesLabel = (TextView) view.findViewById(R.id.trophies_label);
        TextView completedMessage = (TextView) view.findViewById(R.id.completed_message);
        Button okayButton = (Button) view.findViewById(R.id.okay_button);
        ViewUtils.setTypeface(new TextView[] { sign, okayButton },
                TestpressSdk.getRubikMediumFont(getContext()));
        ViewUtils.setTypeface(new TextView[] { trophiesText, trophiesLabel, completedMessage },
                TestpressSdk.getRubikRegularFont(getContext()));

        String trophies = courseAttempt.getTrophies();
        if (trophies.equals("NA")) {
            trophiesText.setText("0");
            sign.setVisibility(View.GONE);
        } else if (trophies.contains("+")) {
            sign.setText("+");
            trophiesText.setText(trophies.substring(1));
        } else if (trophies.contains("-")) {
            sign.setText("-");
            trophiesText.setText(trophies.substring(1));
        } else {
            trophiesText.setText(trophies);
            sign.setVisibility(View.GONE);
        }
        okayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReviewStatsFragment.showReviewStatsFragment(getActivity(), exam,
                        courseAttempt.getAssessment());
            }
        });
        return view;
    }

}
