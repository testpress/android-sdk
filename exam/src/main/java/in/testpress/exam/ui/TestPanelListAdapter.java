package in.testpress.exam.ui;

import android.graphics.Color;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;

import java.util.List;

import in.testpress.exam.R;
import in.testpress.exam.models.AttemptItem;
import in.testpress.exam.models.AttemptQuestion;
import in.testpress.models.greendao.Language;
import in.testpress.util.SingleTypeAdapter;

/**
 * Adapter that used to show the list of questions in test engine
 */
class TestPanelListAdapter extends SingleTypeAdapter<AttemptItem> {

    private int currentAttemptItemIndex = 1;
    private Language selectedLanguage;

    TestPanelListAdapter(final LayoutInflater inflater, final List<AttemptItem> items, int layout) {
        super(inflater, layout);
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
    protected void update(final int position, final AttemptItem item) {
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
        String question = Html.fromHtml(attemptQuestion.getQuestionHtml()).toString();
        if(item.getReview() || item.getCurrentReview()) {
            // Marked question
            updater.view.findViewById(R.id.all_question).setVisibility(View.GONE);
            updater.view.findViewById(R.id.answered_question).setVisibility(View.GONE);
            updater.view.findViewById(R.id.marked_question).setVisibility(View.VISIBLE);
            setNumber(3, item.getIndex());
            setText(1, question.trim());
        } else if(!item.getSelectedAnswers().isEmpty() || !item.getSavedAnswers().isEmpty()
                || (item.getShortText() != null && !item.getShortText().isEmpty())
                || (item.getCurrentShortText() != null && !item.getCurrentShortText().isEmpty())) {

            // Answered question
            updater.view.findViewById(R.id.marked_question).setVisibility(View.GONE);
            updater.view.findViewById(R.id.answered_question).setVisibility(View.VISIBLE);
            updater.view.findViewById(R.id.all_question).setVisibility(View.GONE);
            setNumber(5, item.getIndex());
            setText(4, question.trim());
        } else {
            // Unanswered question
            updater.view.findViewById(R.id.marked_question).setVisibility(View.GONE);
            updater.view.findViewById(R.id.answered_question).setVisibility(View.GONE);
            updater.view.findViewById(R.id.all_question).setVisibility(View.VISIBLE);
            setNumber(2, item.getIndex());
            setText(0, question.trim());
        }
        updater.view.findViewById(R.id.panel_item_layout).setBackgroundColor(
                (item.getIndex() == currentAttemptItemIndex) ? Color.parseColor("#80b6dcfb") :
                        Color.parseColor("#f9f9f9")
        );
    }

    public void setCurrentAttemptItemIndex(int currentAttemptItemIndex) {
        this.currentAttemptItemIndex = currentAttemptItemIndex;
    }

    public void setSelectedLanguage(Language selectedLanguage) {
        this.selectedLanguage = selectedLanguage;
    }
}