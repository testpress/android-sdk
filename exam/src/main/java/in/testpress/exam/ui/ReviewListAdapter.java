package in.testpress.exam.ui;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import in.testpress.exam.R;
import in.testpress.exam.models.ReviewAnswer;
import in.testpress.exam.models.ReviewItem;
import in.testpress.exam.util.SingleTypeAdapter;
import in.testpress.exam.util.UILImageGetter;
import in.testpress.exam.util.ZoomableImageString;

class ReviewListAdapter extends SingleTypeAdapter<ReviewItem> {

    private LayoutInflater inflater;
    private Activity activity;
    
    ReviewListAdapter(final int layoutId, final LayoutInflater inflater,
                             final List<ReviewItem> items, Activity activity) {
        super(inflater, layoutId);
        this.inflater = inflater;
        this.activity = activity;
        setItems(items);
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[] { R.id.question, R.id.answer, R.id.explanation };
    }

    @Override
    protected void update(final int position, final ReviewItem item) {
        TextView questionsView = textView(0);
        Log.d("ReviewList", "Using new UILImageGetter " + questionsView.getHeight());
        Log.d("ReviewList", "Measured Height " + questionsView.getMeasuredHeight());
        Spanned html = Html.fromHtml(item.getReviewQuestion().getQuestionHtml().replaceAll("\n", ""),
                new UILImageGetter(questionsView, this.activity), null);
        ZoomableImageString zoomableImageHtml = new ZoomableImageString(activity);
        questionsView.setText(zoomableImageHtml.convertString(html), TextView.BufferType.SPANNABLE);
        questionsView.setMovementMethod(LinkMovementMethod.getInstance());
        ((TextView)updater.view.findViewById(R.id.question_index)).setText((position + 1) + ".");
        LinearLayout correctAnswersView = (LinearLayout)updater.view.findViewById(R.id.correct_answer);
        // Clear all children first else it keeps appending old items
        correctAnswersView.removeAllViews();
        LinearLayout answersView = (LinearLayout)updater.view.findViewById(R.id.answer);
        // Clear all children first else it keeps appending old items
        answersView.removeAllViews();
        List<ReviewAnswer> answers = item.getReviewQuestion().getAnswers();
        for(int i = 0 ; i < answers.size() ; i++) {
            final ReviewAnswer answer = answers.get(i);
            View option = inflater.inflate(R.layout.testpress_review_answer, null);
            final TextView answerTextView = (TextView) option.findViewById(R.id.answer_text);
            html = Html.fromHtml(answer.getTextHtml(),
                    new UILImageGetter(answerTextView, this.activity), null);
            ZoomableImageString zoomableImageAnswer = new ZoomableImageString(activity);
            answerTextView.setText(zoomableImageAnswer.convertString(html), TextView.BufferType.SPANNABLE);
            answerTextView.setMovementMethod(LinkMovementMethod.getInstance());
            TextView optionTextView = (TextView) option.findViewById(R.id.option);
            optionTextView.setText("" + (char) (i + 97));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 0, 20);
            answersView.addView(option);
            option.setLayoutParams(params);
            if (item.getSelectedAnswers().contains(answer.getId())) {
                if (answer.getIsCorrect()) {
                    if(Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                        optionTextView.setBackgroundResource(R.drawable.testpress_round_green_background);
                    } else {
                        optionTextView.setBackground(inflater.getContext().getResources()
                                .getDrawable(R.drawable.testpress_round_green_background));
                    }
                } else {
                    if(Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                        optionTextView.setBackgroundResource(R.drawable.testpress_round_red_background);
                    } else {
                        optionTextView.setBackground(inflater.getContext().getResources()
                                .getDrawable(R.drawable.testpress_round_red_background));
                    }
                }
            }
            if (answer.getIsCorrect()) {
                TextView correctOption = new TextView(inflater.getContext());
                int hw = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30,
                        inflater.getContext().getResources().getDisplayMetrics());
                LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(hw, hw);
                textParams.setMargins(0, 0, 5, 0);
                correctOption.setLayoutParams(textParams);
                correctOption.setGravity(Gravity.CENTER);
                correctOption.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                correctOption.setTextColor(Color.WHITE);
                if(Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    correctOption.setBackgroundResource(R.drawable.testpress_round_background);
                } else {
                    correctOption.setBackground(inflater.getContext().getResources().getDrawable(
                            R.drawable.testpress_round_background));
                }
                correctOption.setTypeface(Typeface.DEFAULT_BOLD);
                correctOption.setText("" + (char) (i + 97));
                correctOption.setVisibility(View.VISIBLE);
                correctAnswersView.addView(correctOption);
            }
        }
        final String explanation = item.getReviewQuestion().getExplanationHtml().replaceAll("\n", "");
        TextView explanationsView = textView(2);
        if (explanation.equals("")) {
            updater.view.findViewById(R.id.explanation_heading).setVisibility(View.GONE);
            updater.view.findViewById(R.id.explanation).setVisibility(View.GONE);
        } else {
            updater.view.findViewById(R.id.explanation_heading).setVisibility(View.VISIBLE);
            updater.view.findViewById(R.id.explanation).setVisibility(View.VISIBLE);
            html = Html.fromHtml(explanation, new UILImageGetter(explanationsView, this.activity), null);
            ZoomableImageString zoomableImageExplanation = new ZoomableImageString(activity);
            explanationsView.setText(zoomableImageExplanation.convertString(html),
                    TextView.BufferType.SPANNABLE);
            explanationsView.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }
}
