package in.testpress.exam.ui;

import android.app.Activity;
import android.graphics.Color;
import androidx.core.content.ContextCompat;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import java.lang.reflect.Array;
import java.util.List;

import in.testpress.exam.R;
import in.testpress.exam.ui.view.WebView;
import in.testpress.exam.util.TextUtil;
import in.testpress.models.greendao.Language;
import in.testpress.models.greendao.ReviewItem;
import in.testpress.models.greendao.ReviewQuestion;
import in.testpress.models.greendao.ReviewQuestionTranslation;
import in.testpress.util.SingleTypeAdapter;
import in.testpress.util.WebViewUtils;

import static in.testpress.models.greendao.ReviewItem.ANSWERED_INCORRECT;
import static in.testpress.models.greendao.ReviewItem.UNANSWERED;

/**
 * Adapter that used to show the list of questions in review
 */
class ReviewPanelListAdapter extends SingleTypeAdapter<ReviewItem> {

    private int currentItemPosition = 1;
    private Language selectedLanguage;
    private final Activity activity;

    ReviewPanelListAdapter(final LayoutInflater inflater, final List<ReviewItem> items, int layout, Activity activity) {
        super(inflater, layout);
        this.activity = activity;
        setItems(items);
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[] {
                R.id.question_all, R.id.question_marked, R.id.question_index_all,
                R.id.question_index_marked, R.id.question_answered, R.id.question_index_answered
        };
    }

    @Override
    protected void update(final int position, final ReviewItem item) {
        String question = null;
        ReviewQuestion reviewQuestion = item.getQuestion();
        List<ReviewQuestionTranslation> translations = reviewQuestion.getTranslations();
        if (translations.size() > 0 && selectedLanguage != null &&
                !selectedLanguage.getCode().equals(reviewQuestion.getLanguage())) {

            for (ReviewQuestionTranslation translation : translations) {
                if (translation.getLanguage().equals(selectedLanguage.getCode())) {
                    initWebView(translation.getQuestionHtml());
                }
            }
        }
        if (question == null) {
            initWebView(reviewQuestion.getQuestionHtml());
        }
        updater.view.findViewById(R.id.marked_question).setVisibility(View.GONE);
        updater.view.findViewById(R.id.answered_question).setVisibility(View.GONE);
        updater.view.findViewById(R.id.all_question).setVisibility(View.VISIBLE);
        View indexView = updater.view.findViewById(R.id.question_index_all);
        setNumber(2, item.getIndex());

        if (item.getResult() == null || item.getResult().equals(UNANSWERED)) {
            setBackgroundColor(indexView, R.color.testpress_text_gray);
        } else if (item.getResult().equals(ANSWERED_INCORRECT)) {
            setBackgroundColor(indexView, R.color.testpress_red);
        } else {
            setBackgroundColor(indexView, R.color.testpress_green);
        }
        updater.view.findViewById(R.id.panel_item_layout).setBackgroundColor(
                (position == currentItemPosition) ? Color.parseColor("#80b6dcfb") :
                        Color.parseColor("#ffffff")
        );
    }

    private void initWebView(String questionHtml) {
        WebView questionAll = updater.view.findViewById(R.id.question_all);
        String question = "<div style='padding-left: 6px; padding-right: 6px; padding-top: 0px;'>";
        question = question + questionHtml;
        WebViewUtils webViewUtils = new WebViewUtils(questionAll);
        webViewUtils.initWebView(question, activity);
    }

    private void setBackgroundColor(View view, int colorResId) {
        view.setBackgroundColor(ContextCompat.getColor(view.getContext(), colorResId));
    }

    public void setCurrentItemPosition(int currentItemPosition) {
        this.currentItemPosition = currentItemPosition;
    }

    public void setSelectedLanguage(Language selectedLanguage) {
        this.selectedLanguage = selectedLanguage;
    }
}