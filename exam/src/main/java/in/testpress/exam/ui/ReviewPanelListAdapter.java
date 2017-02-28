package in.testpress.exam.ui;

import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import in.testpress.exam.R;
import in.testpress.exam.TestpressExam;
import in.testpress.exam.models.greendao.ReviewAnswer;
import in.testpress.exam.models.greendao.ReviewItem;
import in.testpress.exam.models.greendao.SelectedAnswer;
import in.testpress.exam.models.greendao.SelectedAnswerDao;
import in.testpress.util.SingleTypeAdapter;

/**
 * Adapter that used to show the list of questions in review
 */
class ReviewPanelListAdapter extends SingleTypeAdapter<ReviewItem> {

    private int currentItemPosition = 1;
    private SelectedAnswerDao selectedAnswerDao;

    ReviewPanelListAdapter(final LayoutInflater inflater, final List<ReviewItem> items, int layout) {
        super(inflater, layout);
        selectedAnswerDao = TestpressExam.getSelectedAnswerDao(inflater.getContext());
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
        String question = Html.fromHtml(item.getQuestion().getQuestionHtml()).toString();
        updater.view.findViewById(R.id.marked_question).setVisibility(View.GONE);
        updater.view.findViewById(R.id.answered_question).setVisibility(View.GONE);
        updater.view.findViewById(R.id.all_question).setVisibility(View.VISIBLE);
        View indexView = updater.view.findViewById(R.id.question_index_all);
        setNumber(2, item.getIndex());
        setText(0, question.trim());

        ArrayList<Integer> selectedAnswerIds = getSelectedAnswerIds(item.getId());
        boolean incorrectAnswer = false;
        for (ReviewAnswer answer : item.getQuestion().getAnswers()) {
            //noinspection SuspiciousMethodCalls
            if (!answer.getIsCorrect() &&
                    selectedAnswerIds.contains(Integer.parseInt(answer.getId()+""))) {
                incorrectAnswer = true;
            }
        }
        if (selectedAnswerIds.isEmpty()) {
            // Unanswered question
            indexView.setBackgroundColor(ContextCompat.getColor(indexView.getContext(),
                    R.color.testpress_text_gray));
        } else if(incorrectAnswer) {
            // Incorrect answer
            indexView.setBackgroundColor(ContextCompat.getColor(indexView.getContext(),
                    R.color.testpress_red));
        } else {
            // Correct answer
            indexView.setBackgroundColor(ContextCompat.getColor(indexView.getContext(),
                    R.color.testpress_green));
        }
        updater.view.findViewById(R.id.panel_item_layout).setBackgroundColor(
                (position == currentItemPosition) ? Color.parseColor("#80b6dcfb") :
                        Color.parseColor("#f9f9f9")
        );
    }

    private ArrayList<Integer> getSelectedAnswerIds(long reviewItemId) {
        List<SelectedAnswer> selectedAnswers = selectedAnswerDao.queryBuilder()
                .where(SelectedAnswerDao.Properties.ReviewItemId.eq(reviewItemId)).distinct().list();
        ArrayList<Integer> selectedAnswerIds = new ArrayList<>();
        for (SelectedAnswer selectedAnswer : selectedAnswers) {
            selectedAnswerIds.add(selectedAnswer.getAnswerId());
        }
        return selectedAnswerIds;
    }

    public void setCurrentItemPosition(int currentItemPosition) {
        this.currentItemPosition = currentItemPosition;
    }
}