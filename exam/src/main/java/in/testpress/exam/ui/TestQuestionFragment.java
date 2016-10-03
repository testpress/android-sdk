package in.testpress.exam.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import in.testpress.exam.R;
import in.testpress.exam.models.AttemptAnswer;
import in.testpress.exam.models.AttemptItem;
import in.testpress.exam.models.AttemptQuestion;
import in.testpress.util.UILImageGetter;
import in.testpress.util.ViewUtils;
import in.testpress.util.ZoomableImageString;

public class TestQuestionFragment extends Fragment {

    static final String PARAM_ATTEMPT_ITEM = "attemptItem";
    static final String PARAM_QUESTION_INDEX = "questionIndex";
    private AttemptItem attemptItem;
    private Integer index;
    private RadioButton selectedRadioButton;
    private RadioGroup answersView;
    private LinearLayout answersCheckboxView;
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
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        List<AttemptAnswer> attemptAnswers = attemptItem.getAttemptQuestion().getAttemptAnswers();
        final AttemptQuestion attemptQuestion = attemptItem.getAttemptQuestion();
        if (view == null) {
            view = inflater.inflate(R.layout.testpress_fragment_test_question, container, false);
            answersView = (RadioGroup) view.findViewById(R.id.answers);
            answersCheckboxView = (LinearLayout) view.findViewById(R.id.answers_checkbox);
            final ImageView directionArrow = (ImageView) view.findViewById(R.id.direction_arrow);
            final TextView directionHeading = (TextView) view.findViewById(R.id.direction_heading);
            TextView questionsView = (TextView) view.findViewById(R.id.question);
            TextView questionIndex = (TextView) view.findViewById(R.id.question_index);
            CheckBox review = (CheckBox) view.findViewById(R.id.review);
            TextView direction = (TextView) view.findViewById(R.id.direction_text);
            final LinearLayout expandLayout = (LinearLayout) view.findViewById(R.id.expand_layout);
            CardView directionLayout = (CardView) view.findViewById(R.id.direction_layout);
            questionIndex.setText(index + ".");
            Spanned htmlSpan = Html.fromHtml(attemptQuestion.getQuestionHtml(),
                    new UILImageGetter(questionsView, getActivity()), null);
            ZoomableImageString zoomableImageQuestion = new ZoomableImageString(getActivity());
            questionsView.setText(zoomableImageQuestion.convertString(htmlSpan));
            questionsView.setMovementMethod(LinkMovementMethod.getInstance());
            String type = attemptItem.getAttemptQuestion().getType();
            switch (type) {
                case "R":
                    createRadioButtonView(attemptAnswers, attemptQuestion);
                    break;
                case "C":
                    createCheckBoxView(attemptAnswers, attemptQuestion);
                    break;
                default:
                    break;
            }
            review.setChecked(attemptItem.getReview());
            attemptItem.saveAnswers(attemptItem.getSelectedAnswers());
            attemptItem.setCurrentReview(attemptItem.getReview());
            if (attemptQuestion.getDirection() == null || attemptQuestion.getDirection().isEmpty()) {
                // If direction is empty remove slider
                directionLayout.setVisibility(View.GONE);
            } else {
                Spanned directionHtmlSpan = Html.fromHtml(attemptQuestion.getDirection(),
                        new UILImageGetter(direction, getActivity()), null);
                ZoomableImageString zoomableImageDirection = new ZoomableImageString(getActivity());
                direction.setText(zoomableImageDirection.convertString(directionHtmlSpan));
                direction.setMovementMethod(LinkMovementMethod.getInstance());
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

    private void createCheckBoxView(final List<AttemptAnswer> attemptAnswers, AttemptQuestion attemptQuestion) {
        final List<Integer> savedAnswers = new ArrayList<Integer>();
        for(int i = 0 ; i < attemptQuestion.getAttemptAnswers().size() ; i++) {
            final AppCompatCheckBox option = new AppCompatCheckBox(getActivity());
            option.setId(i);
            LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0, 5, 0, 5);
            option.setLayoutParams(layoutParams);
            Spanned htmlSpan = Html.fromHtml(attemptAnswers.get(i).getTextHtml(),
                    new UILImageGetter(option, getActivity()), null);
            ZoomableImageString zoomableImageOption = new ZoomableImageString(getActivity());
            option.setText(zoomableImageOption.convertString(htmlSpan));
            option.setMovementMethod(LinkMovementMethod.getInstance());
                 option.setPadding(0, 10, 0, 10);
            List<Integer> selectedAnswers = attemptItem.getSelectedAnswers();
            if(!selectedAnswers.isEmpty()) {
                if (selectedAnswers.get(0).equals(attemptAnswers.get(i).getId())) {
                    option.setChecked(true);
                    option.setBackgroundColor(ContextCompat.getColor(getActivity(),
                            R.color.testpress_blue_light_background_transparent));
                }
            }
            answersCheckboxView.addView(option);
            option.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton option, boolean checked) {
                    List<AttemptAnswer> attemptAnswers = attemptItem.getAttemptQuestion().getAttemptAnswers();
                    if(checked) {
                        option.setBackgroundColor(ContextCompat.getColor(getActivity(),
                                R.color.testpress_blue_light_background_transparent));
                        savedAnswers.add(attemptAnswers.get(option.getId()).getId());
                    } else {
                        option.setBackgroundColor(ContextCompat.getColor(getActivity(),
                                android.R.color.transparent));
                        savedAnswers.remove(attemptAnswers.get(option.getId()).getId());
                    }
                    attemptItem.saveAnswers(savedAnswers);
                }
            });
        }
    }

    @SuppressLint("InflateParams")
    private void createRadioButtonView(List<AttemptAnswer> attemptAnswers, AttemptQuestion attemptQuestion) {
        for(int i = 0 ; i < attemptQuestion.getAttemptAnswers().size() ; i++) {
            LayoutInflater inflater = (LayoutInflater) getActivity()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final RadioButton option = (RadioButton) inflater.inflate(
                    R.layout.testpress_radio_button, null);
            option.setId(i);
            option.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            Spanned htmlSpan = Html.fromHtml(attemptAnswers.get(i).getTextHtml(),
                    new UILImageGetter(option, getActivity()), null);

            ZoomableImageString zoomableImageOption = new ZoomableImageString(getActivity());
            option.setText(zoomableImageOption.convertString(htmlSpan));
            option.setMovementMethod(LinkMovementMethod.getInstance());
            final List<Integer> selectedAnswers = attemptItem.getSelectedAnswers();
            if(!selectedAnswers.isEmpty()) {
                if (selectedAnswers.get(0).equals(attemptAnswers.get(i).getId())) {
                    option.setChecked(true);
                    selectedRadioButton = option;
                    option.setBackgroundColor(ContextCompat.getColor(getContext(),
                            R.color.testpress_blue_light_background_transparent));
                }
            }
            answersView.addView(option);
            option.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (option == selectedRadioButton) {
                        answersView.clearCheck();
                        selectedRadioButton = null;
                        option.setBackgroundColor(ContextCompat.getColor(getContext(),
                                android.R.color.transparent));
                        attemptItem.saveAnswers(Collections.<Integer>emptyList());
                    } else {
                        if (selectedRadioButton != null) {
                            selectedRadioButton.setBackgroundColor(ContextCompat.getColor(
                                    getContext(), android.R.color.transparent));
                        }
                        selectedRadioButton = option;
                        option.setBackgroundColor(ContextCompat.getColor(getContext(),
                                R.color.testpress_blue_light_background_transparent));
                        List<AttemptAnswer> attemptAnswers = attemptItem.getAttemptQuestion()
                                .getAttemptAnswers();
                        List<Integer> savedAnswers = new ArrayList<Integer>();
                        savedAnswers.add(attemptAnswers.get(option.getId()).getId());
                        attemptItem.saveAnswers(savedAnswers);
                    }
                }
            });
        }
    }

}
