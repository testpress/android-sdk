package in.testpress.exam.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import junit.framework.Assert;

import java.util.List;

import in.testpress.exam.R;
import in.testpress.exam.TestpressExam;
import in.testpress.exam.models.greendao.ReviewAnswer;
import in.testpress.exam.models.greendao.ReviewItem;
import in.testpress.exam.models.greendao.ReviewItemDao;
import in.testpress.exam.models.greendao.ReviewQuestion;
import in.testpress.util.UIUtils;
import in.testpress.util.WebViewUtils;

public class ReviewQuestionsFragment extends Fragment {

    static final String PARAM_REVIEW_ITEM_ID = "reviewItemId";
    private ReviewItem reviewItem;
    private View emptyView;
    private TextView emptyTitleView;
    private TextView emptyDescView;
    private Button retryButton;
    private ProgressBar progressBar;

    public static ReviewQuestionsFragment getInstance(long reviewItemId) {
        ReviewQuestionsFragment reviewQuestionsFragment = new ReviewQuestionsFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(ReviewQuestionsFragment.PARAM_REVIEW_ITEM_ID, reviewItemId);
        reviewQuestionsFragment.setArguments(bundle);
        return reviewQuestionsFragment;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        long reviewItemId = getArguments().getLong(PARAM_REVIEW_ITEM_ID);
        Assert.assertNotNull("PARAM_REVIEW_ITEM_ID must not be null", reviewItemId);
        ReviewItemDao reviewItemDao= TestpressExam.getReviewItemDao(getContext());
        List<ReviewItem> reviewItems = reviewItemDao.queryBuilder()
                .where(ReviewItemDao.Properties.Id.eq(reviewItemId)).list();
        if (!reviewItems.isEmpty()) {
            reviewItem = reviewItems.get(0);
        } else {
            setEmptyText(R.string.testpress_error_loading_questions,
                    R.string.testpress_some_thing_went_wrong_try_again);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.testpress_fragment_review_question, container, false);
        progressBar = (ProgressBar) view.findViewById(R.id.pb_loading);
        emptyView = view.findViewById(R.id.empty_container);
        emptyTitleView = (TextView) view.findViewById(R.id.empty_title);
        emptyDescView = (TextView) view.findViewById(R.id.empty_description);
        retryButton = (Button) view.findViewById(R.id.retry_button);
        UIUtils.setIndeterminateDrawable(getContext(), progressBar, 4);
        WebView webView = (WebView) view.findViewById(R.id.web_view);
        WebViewUtils webViewUtils = new WebViewUtils(webView) {
            @Override
            protected void onLoadFinished() {
                super.onLoadFinished();
                progressBar.setVisibility(View.GONE);
            }
        };
        webViewUtils.initWebView(getHtml(), getActivity());
        return view;
    }

    /**
     * Convert review item to HTML
     *
     * @return HTML as String
     */
    private String getHtml() {
        String html = "<div style='padding-left: 5px; padding-right: 5px;'>";

        // Add index
        html += "<div>" +
                "<div class='review-question-index'>" +
                reviewItem.getIndex() +
                "</div>";
        ReviewQuestion reviewQuestion = reviewItem.getQuestion();

        // Add direction/passage
        String directionHtml = reviewQuestion.getDirection();
        if (directionHtml != null && !directionHtml.isEmpty()) {
            html += "<div class='review-question' style='padding-bottom: 0px;'>" +
                        directionHtml +
                    "</div>";
        }

        // Add question
        html += "<div class='review-question'>" +
                reviewQuestion.getQuestionHtml() +
                "</div>";

        // Add options
        List<ReviewAnswer> reviewAnswers = reviewQuestion.getAnswers();
        String correctAnswerHtml = "";
        for (int j = 0; j < reviewAnswers.size(); j++) {
            ReviewAnswer attemptAnswer = reviewAnswers.get(j);
            int optionColor;
            if (reviewItem.getSelectedAnswers().contains(attemptAnswer.getId().intValue())) {
                if (attemptAnswer.getIsCorrect()) {
                    optionColor = R.color.testpress_green;
                } else {
                    optionColor = R.color.testpress_red;
                }
            } else {
                optionColor = android.R.color.white;
            }
            html += "\n" + WebViewUtils.getOptionWithTags(attemptAnswer.getTextHtml(), j,
                    optionColor, getContext());
            if (attemptAnswer.getIsCorrect()) {
                correctAnswerHtml += "\n" + WebViewUtils.getCorrectAnswerIndexWithTags(j);
            }
        }

        // Add correct answer
        html += "<div style='display:block;'>" +
                    WebViewUtils.getHeadingTags(getString(R.string.testpress_correct_answer)) +
                    correctAnswerHtml +
                "</div>";

        // Add explanation
        String explanationHtml = reviewQuestion.getExplanationHtml();
        if (explanationHtml != null && !explanationHtml.isEmpty()) {
            html += WebViewUtils.getHeadingTags(getString(R.string.testpress_explanation));
            html += "<div class='review-explanation'>" +
                        explanationHtml +
                    "</div>";
        }
        return html + "</div>";
    }

    protected void setEmptyText(final int title, final int description) {
        emptyView.setVisibility(View.VISIBLE);
        emptyTitleView.setText(title);
        emptyDescView.setText(description);
        retryButton.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
    }

}
