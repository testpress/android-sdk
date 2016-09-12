package in.testpress.exam.ui;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;

import java.util.ArrayList;

import in.testpress.exam.R;
import in.testpress.exam.models.Attempt;
import in.testpress.exam.models.Exam;
import in.testpress.exam.util.ViewUtils;

public class ReviewStatsFragment extends Fragment {

    public static final String PRAM_EXAM = "exam";
    public static final String PRAM_ATTEMPT = "attempt";

    public static ReviewStatsFragment getInstance(Exam exam, Attempt attempt) {
        ReviewStatsFragment fragment = new ReviewStatsFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(PRAM_EXAM, exam);
        bundle.putParcelable(PRAM_ATTEMPT, attempt);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.testpress_review_stats, container, false);
        TextView examTitle = (TextView) view.findViewById(R.id.exam_title);
        TextView totalCorrect = (TextView) view.findViewById(R.id.total_correct);
        TextView totalIncorrect = (TextView) view.findViewById(R.id.total_incorrect);
        TextView totalUnanswered = (TextView) view.findViewById(R.id.total_unanswered);
        TextView timeTaken = (TextView) view.findViewById(R.id.time_taken);
        TextView score = (TextView) view.findViewById(R.id.score);
        TextView rank = (TextView) view.findViewById(R.id.rank);
        TextView percentile = (TextView) view.findViewById(R.id.percentile);
        TextView subPercentile = (TextView) view.findViewById(R.id.sub_percentile);
        TextView subScore = (TextView) view.findViewById(R.id.sub_score);
        PieChart chart = (PieChart) view.findViewById(R.id.chart);
        View emailPdfContainer = view.findViewById(R.id.email_pdf_container);
        Button emailPdfButton = (Button) view.findViewById(R.id.email_pdf);
        Exam exam = getArguments().getParcelable(PRAM_EXAM);
        final Attempt attempt = getArguments().getParcelable(PRAM_ATTEMPT);
        if (attempt != null) {
            ViewUtils.setLeftDrawable(getActivity(), emailPdfButton, R.drawable.ic_email_white_18dp);
            emailPdfButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new EmailPdfDialog(getActivity(), R.style.TestpressAppCompatAlertDialogStyle,
                            true, attempt.getUrlFrag()).show();
                }
            });
            totalCorrect.setText(attempt.getCorrectCount().toString());
            totalIncorrect.setText(attempt.getIncorrectCount().toString());
            Integer unanswered = attempt.getTotalQuestions() - (attempt.getCorrectCount() +
                    attempt.getIncorrectCount());
            totalUnanswered.setText(unanswered.toString());
            timeTaken.setText(attempt.getTimeTaken());
            rank.setText(attempt.getRank());
            score.setText(attempt.getScore());
            percentile.setText(attempt.getPercentile());
            subScore.setText(attempt.getScore());
            subPercentile.setText(attempt.getPercentile());
            ArrayList<Entry> entries = new ArrayList<>();
            entries.add(new Entry(attempt.getCorrectCount(), 0));
            entries.add(new Entry(attempt.getIncorrectCount(), 1));
            entries.add(new Entry(unanswered, 2));
            PieDataSet dataset = new PieDataSet(entries, "");
            ArrayList<String> labels = new ArrayList<String>();
            labels.add(getResources().getString(R.string.testpress_page_correct) + ": " +
                    attempt.getCorrectCount());
            labels.add(getResources().getString(R.string.testpress_page_incorrect) + ": " +
                    attempt.getIncorrectCount());
            labels.add(getResources().getString(R.string.testpress_page_unanswered) + ": " + unanswered);
            ArrayList<Integer> colors = new ArrayList<Integer>();
            colors.add(Color.parseColor("#46BFBD"));
            colors.add(Color.parseColor("#F7464A"));
            colors.add(Color.parseColor("#FDB45C"));
            dataset.setColors(colors);
            dataset.setSliceSpace(2f);
            PieData data = new PieData(labels, dataset);
            data.setDrawValues(false);
            chart.setData(data);
            chart.setDescription("");
            chart.setDrawSliceText(false);
            chart.setDrawHoleEnabled(false);
            chart.setTouchEnabled(false);
            chart.invalidate();
        }
        if (exam != null) {
            examTitle.setText(exam.getTitle());
            if (exam.getAllowPdf()) {
                emailPdfContainer.setVisibility(View.VISIBLE);
            } else {
                emailPdfContainer.setVisibility(View.GONE);
            }
        }
        return view;
    }

}
