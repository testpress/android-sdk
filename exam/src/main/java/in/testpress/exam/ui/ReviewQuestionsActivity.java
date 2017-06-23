package in.testpress.exam.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SlidingPaneLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import junit.framework.Assert;

import org.greenrobot.greendao.query.QueryBuilder;
import org.greenrobot.greendao.query.WhereCondition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.exam.R;
import in.testpress.exam.TestpressExam;
import in.testpress.exam.models.Attempt;
import in.testpress.exam.models.greendao.ReviewAnswer;
import in.testpress.exam.models.greendao.ReviewAnswerDao;
import in.testpress.exam.models.greendao.ReviewAttempt;
import in.testpress.exam.models.greendao.ReviewAttemptDao;
import in.testpress.exam.models.greendao.ReviewItem;
import in.testpress.exam.models.greendao.ReviewItemDao;
import in.testpress.exam.models.greendao.ReviewQuestion;
import in.testpress.exam.models.greendao.ReviewQuestionDao;
import in.testpress.exam.models.greendao.SelectedAnswer;
import in.testpress.exam.models.greendao.SelectedAnswerDao;
import in.testpress.exam.network.TestpressExamApiClient;
import in.testpress.exam.ui.view.NonSwipeableViewPager;
import in.testpress.model.TestpressApiResponse;
import in.testpress.ui.BaseToolBarActivity;
import in.testpress.ui.ExploreSpinnerAdapter;
import in.testpress.util.UIUtils;

public class ReviewQuestionsActivity extends BaseToolBarActivity {

    static final String PARAM_ATTEMPT = "attempt";
    public static final String FILTER_ALL = "all";
    public static final String FILTER_CORRECT = "correct";
    public static final String FILTER_INCORRECT = "incorrect";
    public static final String FILTER_UNANSWERED = "unanswered";
    public static final String FILTER_MARKED = "marked";
    private Attempt attempt;
    private ReviewAttempt reviewAttempt;
    private List<ReviewItem> reviewItems = new ArrayList<>();
    private QueryBuilder<ReviewItem> queryBuilder;
    SlidingPaneLayout slidingPaneLayout;
    private NonSwipeableViewPager pager;
    private ReviewQuestionsPagerAdapter pagerAdapter;
    private ReviewPanelListAdapter panelListAdapter;
    private Spinner spinner;
    private View questionLayout;
    private View buttonLayout;
    private ListView questionsListView;
    private View emptyView;
    private TextView emptyTitleView;
    private TextView emptyDescView;
    private Button retryButton;
    private Button previousButton;
    private Button nextButton;
    private Button questionsListButton;
    private ProgressBar progressBar;
    private MenuItem filterMenu;
    private ReviewItemDao reviewItemDao;
    private ReviewAttemptDao attemptDao;
    protected ExploreSpinnerAdapter spinnerAdapter;
    /**
     * When spinnerAdapter is set to spinner, the spinner itself select first item as default,
     * so onItemSelected callback will be called with position 0, we need to omit this callback
     */
    protected Boolean spinnerDefaultCallback = true;
    protected int selectedItemPosition = -1;

    public static Intent createIntent(Activity activity, Attempt attempt) {
        Intent intent = new Intent(activity, ReviewQuestionsActivity.class);
        intent.putExtra(ReviewQuestionsActivity.PARAM_ATTEMPT, attempt);
        return intent;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testpress_activity_review_question);
        attempt = getIntent().getParcelableExtra(PARAM_ATTEMPT);
        Assert.assertNotNull("PARAM_ATTEMPT must not be null", attempt);
        questionsListView = (ListView) findViewById(R.id.questions_list_view);
        emptyView = findViewById(R.id.empty_container);
        emptyTitleView = (TextView) findViewById(R.id.empty_title);
        emptyDescView = (TextView) findViewById(R.id.empty_description);
        retryButton = (Button) findViewById(R.id.retry_button);
        previousButton = (Button) findViewById(R.id.previous);
        nextButton = (Button) findViewById(R.id.next);
        questionsListButton = (Button) findViewById(R.id.question_list_button);
        progressBar = (ProgressBar) findViewById(R.id.pb_loading);
        UIUtils.setIndeterminateDrawable(this, progressBar, 4);
        questionLayout = findViewById(R.id.question_layout);
        buttonLayout = findViewById(R.id.button_layout);
        pager = (NonSwipeableViewPager) findViewById(R.id.pager);
        slidingPaneLayout = (SlidingPaneLayout) findViewById(R.id.sliding_layout);
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPreviousQuestion();
            }
        });
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNextQuestion();
            }
        });
        questionsListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setPanelOpen(!slidingPaneLayout.isOpen());
            }
        });
        ((ListView) findViewById(R.id.questions_list_view)).setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position,
                                            long id) {
                        goToQuestion(position);
                        setPanelOpen(false);
                    }
                });
        panelListAdapter = new ReviewPanelListAdapter(getLayoutInflater(), reviewItems,
                R.layout.testpress_test_panel_list_item);
        questionsListView.setAdapter(panelListAdapter);
        pagerAdapter = new ReviewQuestionsPagerAdapter(getSupportFragmentManager(), reviewItems);
        pager.setAdapter(pagerAdapter);
        slidingPaneLayout.setPanelSlideListener(new SlidingPaneLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
            }

            @Override
            public void onPanelOpened(View panel) {
                onExpandPanel();
            }

            @Override
            public void onPanelClosed(View panel) {
                onClosePanel();
            }
        });
        pager.setSwipeEnabled(true);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                goToQuestion(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        spinnerAdapter = new ExploreSpinnerAdapter(getLayoutInflater(), getResources(), true);
        spinnerAdapter.hideSpinner(true);
        reviewItemDao= TestpressExam.getReviewItemDao(this);
        attemptDao = TestpressExam.getReviewAttemptDao(this);
        reviewAttempt = getReviewAttempt();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(in.testpress.R.menu.testpress_filter, menu);
        filterMenu = menu.findItem(R.id.filter);
        View actionView = MenuItemCompat.getActionView(filterMenu);
        final View circle = actionView.findViewById(in.testpress.R.id.filter_applied_sticky_tick);
        spinner = (Spinner) actionView.findViewById(in.testpress.R.id.spinner);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            spinner.setBackgroundTintList(ContextCompat.getColorStateList(
                    this, in.testpress.R.color.testpress_actionbar_text));
        } else {
            ViewCompat.setBackgroundTintList(spinner, ContextCompat.getColorStateList(
                    this, in.testpress.R.color.testpress_actionbar_text));
        }
        spinnerDefaultCallback = true;
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> spinner, View view, int position, long itemId) {
                if (position == 0) {
                    circle.setVisibility(View.GONE);
                } else {
                    circle.setVisibility(View.VISIBLE);
                }
                if (spinnerDefaultCallback) {
                    spinnerDefaultCallback = false;
                } else if ((selectedItemPosition != position)) { // Omit callback if position is already selected position
                    selectedItemPosition = position;
                    onSpinnerItemSelected(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        // Check review items exists for the review attempt, load otherwise.
        if (reviewItemDao._queryReviewAttempt_ReviewItems(reviewAttempt.getId()).isEmpty()) {
            loadReviewItemsFromServer(reviewAttempt, reviewAttempt.getReviewUrl());
        } else {
            addFilterItemsInSpinner();
            onSpinnerItemSelected(0);
        }
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Check ReviewAttempt with current attempt id exist in DB, store otherwise.
     *
     * @return ReviewAttempt from DB.
     */
    private ReviewAttempt getReviewAttempt() {
        ReviewAttempt reviewAttempt;
        List<ReviewAttempt> reviewAttempts = attemptDao.queryBuilder()
                .where(ReviewAttemptDao.Properties.Id.eq(attempt.getId())).list();
        if (!reviewAttempts.isEmpty()) {
            reviewAttempt = reviewAttempts.get(0);
        } else {
            reviewAttempt = attempt.getReviewAttempt();
            attemptDao.insertOrReplace(reviewAttempt);
        }
        return reviewAttempt;
    }

    private void loadReviewItemsFromServer(final ReviewAttempt reviewAttempt, final String url) {
        new TestpressExamApiClient(this)
                .getReviewItems(url, new HashMap<String, Object>())
                .enqueue(new TestpressCallback<TestpressApiResponse<ReviewItem>>() {
                    @Override
                    public void onSuccess(TestpressApiResponse<ReviewItem> response) {
                        reviewItems.addAll(response.getResults());
                        // Load the next page if exists.
                        if (response.getNext() != null) {
                            loadReviewItemsFromServer(reviewAttempt, response.getNext());
                        } else {
                            for (int i = 0; i < reviewItems.size(); i++) {
                                ReviewItem reviewItem = reviewItems.get(i);
                                ReviewQuestion reviewQuestion = reviewItem.question;
                                // Store selected answers.
                                for (int selectedAnswerId : reviewItem.getSelectedAnswers()) {
                                    SelectedAnswerDao selectedAnswersDao =
                                            TestpressExam.getSelectedAnswerDao(ReviewQuestionsActivity.this);
                                    SelectedAnswer selectedAnswer = new SelectedAnswer();
                                    selectedAnswer.setAnswerId(selectedAnswerId);
                                    selectedAnswer.setReviewItemId(reviewItem.getId());
                                    selectedAnswersDao.insertOrReplace(selectedAnswer);
                                }
                                // Store question.
                                ReviewQuestionDao reviewQuestionDao =
                                        TestpressExam.getReviewQuestionDao(ReviewQuestionsActivity.this);
                                reviewQuestionDao.insertOrReplace(reviewQuestion);
                                // Store answers.
                                for (ReviewAnswer reviewAnswer : reviewQuestion.getAnswers()) {
                                    ReviewAnswerDao reviewAnswerDao =
                                            TestpressExam.getReviewAnswerDao(ReviewQuestionsActivity.this);
                                    reviewAnswer.setQuestionId(reviewQuestion.getId());
                                    reviewAnswerDao.insertOrReplace(reviewAnswer);
                                }
                                // Store review item.
                                reviewItem.setQuestionId(reviewQuestion.getId());
                                reviewItem.setAttemptId(reviewAttempt.getId());
                                reviewItem.setIndex(i + 1);
                                reviewItemDao.insertOrReplace(reviewItem);
                            }
                            addFilterItemsInSpinner();
                            onSpinnerItemSelected(0);
                        }
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        progressBar.setVisibility(View.GONE);
                        retryButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                progressBar.setVisibility(View.VISIBLE);
                                emptyView.setVisibility(View.GONE);
                                loadReviewItemsFromServer(reviewAttempt, url);
                            }
                        });
                        if(exception.isUnauthenticated()) {
                            setEmptyText(R.string.testpress_authentication_failed,
                                    R.string.testpress_please_login);
                        } else if (exception.isNetworkError()) {
                            setEmptyText(R.string.testpress_network_error,
                                    R.string.testpress_no_internet_try_again);
                        } else {
                            setEmptyText(R.string.testpress_error_loading_questions,
                                    R.string.testpress_some_thing_went_wrong_try_again);
                        }
                    }
                });
    }

    /**
     * Get QueryBuilder for the given filter type.
     *
     * @param filterType {@code FILTER_ALL, FILTER_CORRECT, FILTER_INCORRECT, FILTER_UNANSWERED,
     *                    FILTER_MARKED}
     */
    protected QueryBuilder<ReviewItem> getQueryBuilder(String filterType) {
        QueryBuilder<ReviewItem> queryBuilder;
        switch (filterType) {
            case FILTER_ALL:
                queryBuilder = reviewItemDao.queryBuilder()
                        .where(ReviewItemDao.Properties.AttemptId.eq(reviewAttempt.getId()));
                break;
            case FILTER_CORRECT:
                queryBuilder = reviewItemDao.queryBuilder().where(
                        ReviewItemDao.Properties.AttemptId.eq(reviewAttempt.getId()),
                        new WhereCondition.StringCondition("" +
                                " T.QUESTION_ID IN (SELECT QUESTION_ID FROM REVIEW_ANSWER A" +
                                " INNER JOIN SELECTED_ANSWER S ON T.ID = S.REVIEW_ITEM_ID" +
                                " WHERE A.IS_CORRECT = 1" +
                                "   AND S.ANSWER_ID = A.ID)")
                );
                break;
            case FILTER_INCORRECT:
                queryBuilder = reviewItemDao.queryBuilder().where(
                        ReviewItemDao.Properties.AttemptId.eq(reviewAttempt.getId()),
                        new WhereCondition.StringCondition("" +
                                " T.QUESTION_ID IN (SELECT QUESTION_ID FROM REVIEW_ANSWER A" +
                                " INNER JOIN SELECTED_ANSWER S ON T.ID = S.REVIEW_ITEM_ID" +
                                " WHERE A.IS_CORRECT = 0" +
                                "   AND S.ANSWER_ID = A.ID)")
                );
                break;
            case FILTER_UNANSWERED:
                queryBuilder = reviewItemDao.queryBuilder().where(
                        ReviewItemDao.Properties.AttemptId.eq(reviewAttempt.getId()),
                        ReviewItemDao.Properties.SelectedAnswers.eq(""));
                break;
            case FILTER_MARKED:
                queryBuilder = reviewItemDao.queryBuilder().where(
                        ReviewItemDao.Properties.AttemptId.eq(reviewAttempt.getId()),
                        ReviewItemDao.Properties.Review.eq(true));
                break;
            default:
                queryBuilder = reviewItemDao.queryBuilder();
                break;
        }
        return queryBuilder;
    }

    /**
     * Get the number of items in the given filter type.
     *
     * @param filterType {@code FILTER_ALL, FILTER_CORRECT, FILTER_INCORRECT, FILTER_UNANSWERED,
     *                    FILTER_MARKED}
     * @return count of the items.
     */
    private long getItemsCount(String filterType) {
        return getQueryBuilder(filterType).distinct().count();
    }

    /**
     * On select spinner item, create the query builder with respect to selected item.
     *
     * @param position position of the item.
     */
    protected void onSpinnerItemSelected(int position) {
        progressBar.setVisibility(View.VISIBLE);
        queryBuilder = getQueryBuilder(spinnerAdapter.getTag(position));
        filterReviewItems();
    }

    /**
     * Query items from DB using query builder created in {@code onSpinnerItemSelected} &
     * Display in the web view.
     */
    private void filterReviewItems() {
        if (reviewAttempt != null) {
            reviewItems = queryBuilder.distinct().orderAsc(ReviewItemDao.Properties.Index).list();
            if (reviewItems == null) {
                setEmptyText(R.string.testpress_error_loading_questions,
                        R.string.testpress_some_thing_went_wrong_try_again);
            } else if (reviewItems.isEmpty()) {
                setEmptyText(R.string.testpress_no_questions, R.string.testpress_no_questions_to_review);
                retryButton.setVisibility(View.GONE);
            } else {
                panelListAdapter.setItems(reviewItems);
                pagerAdapter.setReviewItems(reviewItems);
                pagerAdapter.notifyDataSetChanged();
                goToQuestion(0);
                filterMenu.setVisible(true);
                questionLayout.setVisibility(View.VISIBLE);
                buttonLayout.setVisibility(View.VISIBLE);
                emptyView.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
            }
        }
    }

    private void setPanelOpen(boolean open) {
        if(open) {
            slidingPaneLayout.openPane();
        } else {
            slidingPaneLayout.closePane();
        }
    }

    private void onExpandPanel() {
        pager.setSwipeEnabled(false);
        previousButton.setVisibility(View.INVISIBLE);
        nextButton.setVisibility(View.INVISIBLE);
        panelListAdapter.notifyDataSetChanged();
        questionsListButton.setText(getString(R.string.testpress_question));
        questionsListView.setSelection(pager.getCurrentItem());
    }

    private void onClosePanel() {
        pager.setSwipeEnabled(true);
        questionsListButton.setText(getString(R.string.testpress_questions_list));
        previousButton.setVisibility(View.VISIBLE);
        nextButton.setVisibility(View.VISIBLE);
    }

    private void showPreviousQuestion() {
        if (pager.getCurrentItem() != 0) {
            goToQuestion(pager.getCurrentItem() - 1);
        }
    }

    private void showNextQuestion() {
        if (pager.getCurrentItem() < (pagerAdapter.getCount() - 1)) {
            goToQuestion(pager.getCurrentItem() + 1);
        }
    }

    private void goToQuestion(int position) {
        if (reviewItems.isEmpty()) {
            return;
        }
        if (pager.getCurrentItem() != position) {
            pager.setCurrentItem(position);
        }
        panelListAdapter.setCurrentItemPosition(position);

        // Validate navigation buttons
        if (position == 0) {
            // Reached first question
            enableButton(previousButton, false);
        } else {
            enableButton(previousButton, true);
        }
        if ((position + 1) == pagerAdapter.getCount()) {
            // Reached last question
            enableButton(nextButton, false);
        } else {
            enableButton(nextButton, true);
        }
    }

    @SuppressLint("DefaultLocale")
    private void addFilterItemsInSpinner() {
        spinnerAdapter.clear();
        spinnerAdapter.addItem(FILTER_ALL, getString(R.string.testpress_page_all), false, 0);
        spinnerAdapter.addItem(FILTER_CORRECT, getString(R.string.testpress_page_correct) +
                String.format(" (%d)", getItemsCount(FILTER_CORRECT)), true, 0);
        spinnerAdapter.addItem(FILTER_INCORRECT, getString(R.string.testpress_page_incorrect) +
                String.format(" (%d)", getItemsCount(FILTER_INCORRECT)), true, 0);
        spinnerAdapter.addItem(FILTER_UNANSWERED, getString(R.string.testpress_page_unanswered) +
                String.format(" (%d)", getItemsCount(FILTER_UNANSWERED)), true, 0);
        spinnerAdapter.addItem(FILTER_MARKED, getString(R.string.testpress_page_review) +
                String.format(" (%d)", getItemsCount(FILTER_MARKED)), true, 0);
        if (selectedItemPosition == -1) {
            selectedItemPosition = 0;
            spinnerAdapter.notifyDataSetChanged();
        } else {
            spinnerAdapter.notifyDataSetChanged();
            spinner.setSelection(selectedItemPosition);
        }
    }

    /**
     * Set alpha to the navigation buttons.
     *
     * @param button Button, which need to be enable or disable.
     * @param enable True if enable, false otherwise.
     */
    private void enableButton(Button button, boolean enable) {
        if (enable) {
            button.setTextColor(ContextCompat.getColor(this, R.color.testpress_text_gray));
        } else {
            button.setTextColor(ContextCompat.getColor(this, R.color.testpress_gray_light));
        }
        button.setEnabled(enable);
    }

    void setNavigationBarVisible(boolean visible) {
        buttonLayout.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    protected void setEmptyText(final int title, final int description) {
        questionLayout.setVisibility(View.GONE);
        buttonLayout.setVisibility(View.GONE);
        emptyTitleView.setText(title);
        emptyDescView.setText(description);
        retryButton.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

}
