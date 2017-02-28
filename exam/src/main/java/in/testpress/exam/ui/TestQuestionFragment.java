package in.testpress.exam.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import in.testpress.exam.R;
import in.testpress.exam.models.AttemptAnswer;
import in.testpress.exam.models.AttemptItem;
import in.testpress.exam.models.AttemptQuestion;
import in.testpress.util.ViewUtils;
import in.testpress.util.WebViewUtils;

public class TestQuestionFragment extends Fragment {

    static final String PARAM_ATTEMPT_ITEM = "attemptItem";
    static final String PARAM_QUESTION_INDEX = "questionIndex";
    private AttemptItem attemptItem;
    private Integer index;
    private List<Integer> selectedOptions;
    private View view;

    static TestQuestionFragment getInstance(AttemptItem attemptItem, int questionIndex) {
        TestQuestionFragment testQuestionFragment = new TestQuestionFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(TestQuestionFragment.PARAM_ATTEMPT_ITEM, attemptItem);
        bundle.putInt(TestQuestionFragment.PARAM_QUESTION_INDEX, questionIndex);
        testQuestionFragment.setArguments(bundle);
        return testQuestionFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        attemptItem = getArguments().getParcelable(PARAM_ATTEMPT_ITEM);
        index = getArguments().getInt(PARAM_QUESTION_INDEX);
        selectedOptions = new ArrayList<>(attemptItem.getSelectedAnswers());
    }

    @SuppressLint({"SetTextI18n", "AddJavascriptInterface"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final AttemptQuestion attemptQuestion = attemptItem.getAttemptQuestion();
        if (view == null) {
            view = inflater.inflate(R.layout.testpress_fragment_test_question, container, false);
            final ImageView directionArrow = (ImageView) view.findViewById(R.id.direction_arrow);
            final TextView directionHeading = (TextView) view.findViewById(R.id.direction_heading);
            WebView questionsView = (WebView) view.findViewById(R.id.question);
            TextView questionIndex = (TextView) view.findViewById(R.id.question_index);
            CheckBox review = (CheckBox) view.findViewById(R.id.review);
            WebView direction = (WebView) view.findViewById(R.id.direction_text);
            final LinearLayout expandLayout = (LinearLayout) view.findViewById(R.id.expand_layout);
            CardView directionLayout = (CardView) view.findViewById(R.id.direction_layout);
            questionIndex.setText(index + ".");
            final String optionType = attemptItem.getAttemptQuestion().getType();
            questionsView.addJavascriptInterface(new OptionsSelectionListener(),
                    "OptionsSelectionListener");
            WebViewUtils webViewUtils = new WebViewUtils(questionsView) {
                @Override
                public String getHeader() {
                    return super.getHeader() + getTestEngineHeader();
                }

                @Override
                public String getJavascript(Context context) {
                    String javascript = super.getJavascript(context);
                    List<Integer> selectedAnswers = attemptItem.getSelectedAnswers();
                    if (!selectedAnswers.isEmpty()) {
                        if (optionType.equals("R")) {
                            javascript += getRadioButtonInitializer(selectedAnswers.get(0));
                        } else {
                            javascript += getCheckBoxInitializer(selectedAnswers);
                        }
                    }
                    return javascript;
                }
            };
            webViewUtils.initWebView(getQuestionItemHtml(), getActivity());
            review.setChecked(attemptItem.getReview());
            attemptItem.saveAnswers(attemptItem.getSelectedAnswers());
            attemptItem.setCurrentReview(attemptItem.getReview());
            if (attemptQuestion.getDirection() == null || attemptQuestion.getDirection().isEmpty()) {
                // If direction is empty remove slider
                directionLayout.setVisibility(View.GONE);
            } else {
                WebViewUtils directionWebViewUtils = new WebViewUtils(direction);
                directionWebViewUtils.initWebView(getDirectionHtml(), getActivity());
            }
            ((CheckBox) view.findViewById(R.id.review)).setOnCheckedChangeListener(
                    new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                            attemptItem.setCurrentReview(checked);
                        }
                    });
            view.findViewById(R.id.direction_expand_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (expandLayout.getVisibility() == View.VISIBLE) {
                        expandLayout.setVisibility(View.GONE);
                        directionHeading.setText(R.string.testpress_show_passage);
                        directionArrow.setImageResource(R.drawable.ic_expand_more_black_24dp);
                    } else {
                        expandLayout.setVisibility(View.VISIBLE);
                        directionHeading.setText(R.string.testpress_hide_passage);
                        directionArrow.setImageResource(R.drawable.ic_expand_less_black_24dp);
                        ViewUtils.slide_down(getActivity(), expandLayout);
                    }
                }
            });
        }
        return view;
    }

    private String getQuestionItemHtml() {
        AttemptQuestion attemptQuestion = attemptItem.getAttemptQuestion();
        String htmlContent = "<div style='padding-left: 10px; padding-right: 10px;'>" +
                "<table width='100%' style='margin-top:0px;'>" +
                "<tr><td class='table-without-border'>" +
                attemptQuestion.getQuestionHtml() +
                "</td></tr>";
        for (int i = 0; i < attemptQuestion.getAttemptAnswers().size(); i++) {
            AttemptAnswer attemptAnswer = attemptQuestion.getAttemptAnswers().get(i);
            if (attemptItem.getAttemptQuestion().getType().equals("R")) {
                htmlContent += "\n" + WebViewUtils.getRadioButtonOptionWithTags(
                        attemptAnswer.getTextHtml(), attemptAnswer.getId());
            } else {
                htmlContent += "\n" + WebViewUtils.getCheckBoxOptionWithTags(
                        attemptAnswer.getTextHtml(), attemptAnswer.getId());
            }
        }
        return htmlContent + "</table></div>";
    }

    private String getDirectionHtml() {
        return "<div style='padding-left: 10px; padding-right: 10px;'>" +
                attemptItem.getAttemptQuestion().getDirection() +
                "</div>";

    }

    private class OptionsSelectionListener {

        @JavascriptInterface
        public void onCheckedChange(String id, boolean checked, boolean radioOption) {
            if (checked) {
                if (radioOption) {
                    selectedOptions = new ArrayList<Integer>();
                }
                selectedOptions.add(Integer.parseInt(id));
            } else {
                selectedOptions.remove((Integer) Integer.parseInt(id));
            }
            attemptItem.saveAnswers(selectedOptions);
        }
    }

}
