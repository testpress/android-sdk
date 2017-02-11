package in.testpress.exam.ui;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.testpress.mikephil.charting.animation.Easing;
import com.github.testpress.mikephil.charting.charts.PieChart;
import com.github.testpress.mikephil.charting.components.Legend;
import com.github.testpress.mikephil.charting.data.PieData;
import com.github.testpress.mikephil.charting.data.PieDataSet;
import com.github.testpress.mikephil.charting.data.PieEntry;

import java.util.ArrayList;

import in.testpress.exam.R;
import in.testpress.exam.models.Attempt;
import in.testpress.exam.models.Exam;

public class ReviewStatsFragment extends Fragment {

    public static final String PARAM_EXAM = "exam";
    public static final String PARAM_ATTEMPT = "attempt";

    public static ReviewStatsFragment getInstance(Exam exam, Attempt attempt) {
        ReviewStatsFragment fragment = new ReviewStatsFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(PARAM_EXAM, exam);
        bundle.putParcelable(PARAM_ATTEMPT, attempt);
        fragment.setArguments(bundle);
        return fragment;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.testpress_review_stats, container, false);
        TextView examTitle = (TextView) view.findViewById(R.id.exam_title);
        TextView timeTaken = (TextView) view.findViewById(R.id.time_taken);
        TextView score = (TextView) view.findViewById(R.id.score);
        TextView rank = (TextView) view.findViewById(R.id.rank);
        TextView percentile = (TextView) view.findViewById(R.id.percentile);
        TextView subPercentile = (TextView) view.findViewById(R.id.sub_percentile);
        TextView speed = (TextView) view.findViewById(R.id.speed);
        TextView accuracy = (TextView) view.findViewById(R.id.accuracy);
        PieChart chart = (PieChart) view.findViewById(R.id.chart);
        LinearLayout rankLayout = (LinearLayout) view.findViewById(R.id.rank_layout);
        LinearLayout percentileLayout = (LinearLayout) view.findViewById(R.id.percentile_layout);
        Exam exam = getArguments().getParcelable(PARAM_EXAM);
        final Attempt attempt = getArguments().getParcelable(PARAM_ATTEMPT);
        if (attempt != null) {
            Integer unanswered = attempt.getTotalQuestions() - (attempt.getCorrectCount() +
                    attempt.getIncorrectCount());
            timeTaken.setText(attempt.getTimeTaken());
            if (attempt.getRank().equals("NA")) {
                rankLayout.setVisibility(View.GONE);
            } else {
                rank.setText(attempt.getRank());
            }
            score.setText(attempt.getScore());
            if (attempt.getPercentile().equals("NA")) {
               percentileLayout.setVisibility(View.GONE);
            } else {
                percentile.setText(attempt.getPercentile());
                //noinspection SetTextI18n
                subPercentile.setText(attempt.getPercentile() +
                        getString(R.string.testpress_scored_less_than) + attempt.getScore());
            }
            speed.setText(attempt.getSpeed().toString());
            accuracy.setText(attempt.getAccuracy().toString());
            ArrayList<PieEntry> entries = new ArrayList<>();
            entries.add(new PieEntry(attempt.getCorrectCount(), 0));
            entries.add(new PieEntry(attempt.getIncorrectCount(), 1));
            entries.add(new PieEntry(unanswered, 2));
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
            PieData data = new PieData(dataset);
            data.setDrawValues(false);
            chart.setData(data);
            chart.setDescription("");
            chart.setDrawHoleEnabled(false);
            chart.setTouchEnabled(false);
            chart.animateY(1400, Easing.EasingOption.EaseInOutQuart);
            Legend l = chart.getLegend();
            l.setPosition(Legend.LegendPosition.RIGHT_OF_CHART_CENTER);
            l.setYEntrySpace(5);
            l.setFormToTextSpace(8);
            l.setTextSize(14f);
            l.setFormSize(9f);
            l.setCustom(colors,labels);
            chart.invalidate();
        }
        if (exam != null) {
            examTitle.setText(exam.getTitle());
        }
        return view;
    }

}
