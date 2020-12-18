package in.testpress.exam.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;

import java.util.List;

import in.testpress.exam.R;
import in.testpress.exam.models.AttemptItem;
import in.testpress.exam.models.AttemptQuestion;
import in.testpress.exam.ui.view.WebView;
import in.testpress.models.greendao.Language;
import in.testpress.util.SingleTypeAdapter;
import in.testpress.util.WebViewUtils;

/**
 * Adapter that used to show the list of questions in test engine
 */
class TestPanelListAdapter extends SingleTypeAdapter<AttemptItem> {

    private int currentAttemptItemIndex = 1;
    private Language selectedLanguage;
    private final Activity activity;
    private final ListItemClickListener listItemClickListener;

    TestPanelListAdapter(final LayoutInflater inflater, final List<AttemptItem> items,
                         int layout, Activity activity, ListItemClickListener listItemClickListener) {
        super(inflater, layout);
        this.activity = activity;
        this.listItemClickListener = listItemClickListener;
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
    @SuppressLint("ClickableViewAccessibility")
    protected void update(final int position, final AttemptItem item) {
        WebView webView;
        AttemptQuestion attemptQuestion = item.getAttemptQuestion();
        List<AttemptQuestion> translations = attemptQuestion.getTranslations();
        if (translations.size() > 0 && selectedLanguage != null &&
                !selectedLanguage.getCode().equals(attemptQuestion.getLanguage())) {

            for (AttemptQuestion translation : translations) {
                if (translation.getLanguage().equals(selectedLanguage.getCode())) {
                    attemptQuestion = translation;
                }
            }
        }
        String question = attemptQuestion.getQuestionHtml();
        if(item.getReview() || item.getCurrentReview()) {
            // Marked question
            webView = updater.view.findViewById(R.id.question_marked);
            updater.view.findViewById(R.id.all_question).setVisibility(View.GONE);
            updater.view.findViewById(R.id.answered_question).setVisibility(View.GONE);
            updater.view.findViewById(R.id.marked_question).setVisibility(View.VISIBLE);
            setNumber(3, item.getIndex());
        } else if(!item.getSelectedAnswers().isEmpty() || !item.getSavedAnswers().isEmpty()
                || (item.getShortText() != null && !item.getShortText().isEmpty())
                || (item.getCurrentShortText() != null && !item.getCurrentShortText().isEmpty())) {

            // Answered question
            webView = updater.view.findViewById(R.id.question_answered);
            updater.view.findViewById(R.id.marked_question).setVisibility(View.GONE);
            updater.view.findViewById(R.id.answered_question).setVisibility(View.VISIBLE);
            updater.view.findViewById(R.id.all_question).setVisibility(View.GONE);
            setNumber(5, item.getIndex());
        } else {
            // Unanswered question
            webView = updater.view.findViewById(R.id.question_all);
            updater.view.findViewById(R.id.marked_question).setVisibility(View.GONE);
            updater.view.findViewById(R.id.answered_question).setVisibility(View.GONE);
            updater.view.findViewById(R.id.all_question).setVisibility(View.VISIBLE);
            setNumber(2, item.getIndex());
        }
        updater.view.findViewById(R.id.panel_item_layout).setBackgroundColor(
                (item.getIndex() == currentAttemptItemIndex) ? Color.parseColor("#80b6dcfb") :
                        Color.parseColor("#ffffff")
        );

        webView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction()==MotionEvent.ACTION_UP){
                    listItemClickListener.onItemClicked(item.getIndex() -1);
                }
                return false;
            }
        });

        updater.view.findViewById(R.id.panel_item_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listItemClickListener.onItemClicked(item.getIndex() - 1);
            }
        });

        initWebView(webView, question);
    }

    private void initWebView(WebView view, String questionHtml) {
        String question = "<div style='padding-left: 6px; padding-right: 6px; padding-top: 0px;'>";
        question = question + questionHtml;
        WebViewUtils webViewUtils = new WebViewUtils(view);
        webViewUtils.initWebView(question, activity);
    }

    public void setCurrentAttemptItemIndex(int currentAttemptItemIndex) {
        this.currentAttemptItemIndex = currentAttemptItemIndex;
    }

    public void setSelectedLanguage(Language selectedLanguage) {
        this.selectedLanguage = selectedLanguage;
    }

    public interface ListItemClickListener {
        void onItemClicked(int position);
    }
}
