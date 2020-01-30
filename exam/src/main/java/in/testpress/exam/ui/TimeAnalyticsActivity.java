package in.testpress.exam.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.core.view.MenuItemCompat;
import androidx.slidingpanelayout.widget.SlidingPaneLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import org.greenrobot.greendao.query.QueryBuilder;
import org.greenrobot.greendao.query.WhereCondition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSDKDatabase;
import in.testpress.exam.R;
import in.testpress.exam.network.TestpressExamApiClient;
import in.testpress.models.TestpressApiResponse;
import in.testpress.models.greendao.Attempt;
import in.testpress.models.greendao.Exam;
import in.testpress.models.greendao.Language;
import in.testpress.models.greendao.LanguageDao;
import in.testpress.models.greendao.ReviewAnswer;
import in.testpress.models.greendao.ReviewAnswerDao;
import in.testpress.models.greendao.ReviewAnswerTranslation;
import in.testpress.models.greendao.ReviewAnswerTranslationDao;
import in.testpress.models.greendao.ReviewAttempt;
import in.testpress.models.greendao.ReviewAttemptDao;
import in.testpress.models.greendao.ReviewItem;
import in.testpress.models.greendao.ReviewItemDao;
import in.testpress.models.greendao.ReviewQuestion;
import in.testpress.models.greendao.ReviewQuestionDao;
import in.testpress.models.greendao.ReviewQuestionTranslation;
import in.testpress.models.greendao.ReviewQuestionTranslationDao;
import in.testpress.models.greendao.SelectedAnswer;
import in.testpress.models.greendao.SelectedAnswerDao;
import in.testpress.network.RetrofitCall;
import in.testpress.ui.BaseToolBarActivity;
import in.testpress.ui.ExploreSpinnerAdapter;
import in.testpress.util.CommonUtils;
import in.testpress.util.UIUtils;
import in.testpress.util.ViewUtils;
import in.testpress.util.WebViewUtils;

public class TimeAnalyticsActivity extends BaseToolBarActivity {

    public static Exam exam;
    public static Attempt attempt;
    public static final String FILTER_ALL = "all";
    public static final String FILTER_CORRECT = "correct";
    public static final String FILTER_INCORRECT = "incorrect";
    public static final String FILTER_UNANSWERED = "unanswered";
    public static String CURRENT_FILTER = "all";
    public static String CURRENT_TIME_SORT = "NONE";
    public static final String timeFilters[] = {"NONE","DESC","ASC"};
    private ArrayList<String> subjects;
    private String CURRENT_SUBJECT_SORT = "All";
    private Activity activity;
    private ProgressBar progressBar;
    private Button retryButton;
    private View emptyView;
    private TextView emptyTitleView;
    private TextView emptyDescView;
    private View mainLayout;
    private WebView pager;
    private SlidingPaneLayout slidingPaneLayout;
    private MenuItem selectLanguageMenu;
    private Spinner languageSpinner;
    private Spinner timeSpinner;
    private Spinner subjectSpinner;
    private MenuItem filterMenu;
    private ReviewItemDao reviewItemDao;
    private ReviewAttemptDao attemptDao;
    private LanguageDao languageDao;
    private ReviewAttempt reviewAttempt;
    private List<ReviewItem> reviewItems = new ArrayList<>();
    protected Boolean spinnerDefaultCallback = true;
    protected int selectedItemPosition = -1;
    private QueryBuilder<ReviewItem> queryBuilder;
    private Language selectedLanguage;
    private List<Language> languageList;
    private ExploreSpinnerAdapter languageSpinnerAdapter;
    private ExploreSpinnerAdapter timeSpinnerAdapter;
    private ExploreSpinnerAdapter subjectSpinnerAdapter;
    private WebViewUtils webViewUtils;
    private WebView webView;
    private Button correctFilterButton;
    private Button incorrectFilterButton;
    private Button unansweredFilterButton;
    private Button applyFilterButton;
    private Button clearFilterButton;
    private ImageView filterIcon;
    private RetrofitCall<TestpressApiResponse<ReviewItem>> reviewItemsApiRequest;

    public static Intent createIntent(Activity activity, Exam exam, Attempt attempt) {
        Intent intent = new Intent(activity, TimeAnalyticsActivity.class);
        intent.putExtra("exam", exam);
        intent.putExtra("attempt", attempt);
        return intent;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testpress_activity_time_analytics);
        activity = this;
        CURRENT_FILTER = FILTER_ALL;
        CURRENT_SUBJECT_SORT = "All";
        CURRENT_TIME_SORT = "NONE";
        exam = getIntent().getParcelableExtra("exam");
        attempt = getIntent().getParcelableExtra("attempt");
        progressBar = (ProgressBar) findViewById(R.id.pb_loading);
        retryButton = (Button) findViewById(R.id.retry_button);
        emptyView = findViewById(R.id.empty_container);
        emptyTitleView = (TextView) findViewById(R.id.empty_title);
        emptyDescView = (TextView) findViewById(R.id.empty_description);
        UIUtils.setIndeterminateDrawable(this, progressBar, 4);
        mainLayout = findViewById(R.id.main_layout);
        pager = (WebView) findViewById(R.id.pager);
        slidingPaneLayout = (SlidingPaneLayout) findViewById(R.id.sliding_layout);
        timeSpinnerAdapter = new ExploreSpinnerAdapter(getLayoutInflater(), getResources(), false);
        subjectSpinnerAdapter = new ExploreSpinnerAdapter(getLayoutInflater(), getResources(), false);
        timeSpinner = (Spinner) findViewById(R.id.timeSpinner);
        subjectSpinner = (Spinner) findViewById(R.id.subjectSpinner);
        webView = (WebView) findViewById(R.id.web_view);
        correctFilterButton = (Button) findViewById(R.id.correct_filter);
        incorrectFilterButton = (Button) findViewById(R.id.incorrect_filter);
        unansweredFilterButton = (Button) findViewById(R.id.unanswered_filter);
        clearFilterButton = (Button) findViewById(R.id.clear_filter);
        applyFilterButton = (Button) findViewById(R.id.apply_filter);
        correctFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFilter(FILTER_CORRECT);
            }
        });
        incorrectFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFilter(FILTER_INCORRECT);
            }
        });
        unansweredFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFilter(FILTER_UNANSWERED);
            }
        });
        applyFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (slidingPaneLayout.isOpen()) {
                    showFilteredContent(CURRENT_FILTER);
                }
                setPanelOpen(!slidingPaneLayout.isOpen());
            }
        });
        clearFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetBackgroundsOfAllButton();
                CURRENT_TIME_SORT = "NONE";
                CURRENT_SUBJECT_SORT = "All";
                setFilter(FILTER_ALL);
                timeSpinner.setSelection(0);
                subjectSpinner.setSelection(0);
                showFilteredContent(CURRENT_FILTER);
                setPanelOpen(!slidingPaneLayout.isOpen());
            }
        });
        reviewItemDao= TestpressSDKDatabase.getReviewItemDao(this);
        attemptDao = TestpressSDKDatabase.getReviewAttemptDao(this);
        languageDao = TestpressSDKDatabase.getLanguageDao(this);
        reviewAttempt = getReviewAttempt();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //Inflate the languages menu
        getMenuInflater().inflate(R.menu.testpress_select_language_menu, menu);
        selectLanguageMenu = menu.findItem(R.id.select_language);
        View actionView = MenuItemCompat.getActionView(selectLanguageMenu);
        languageSpinner = (Spinner) actionView.findViewById(R.id.language_spinner);
        ViewUtils.setSpinnerIconColor(this, languageSpinner);

        getMenuInflater().inflate(R.menu.testpress_time_analytics_filter, menu);
        filterMenu = menu.findItem(R.id.options);
        actionView = MenuItemCompat.getActionView(filterMenu);
        filterIcon = ((ImageView) actionView.findViewById(R.id.filter));
        filterIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPanelOpen(!slidingPaneLayout.isOpen());
            }
        });
        spinnerDefaultCallback = true;
        // Check review items exists for the review attempt, load otherwise.
        if (reviewItemDao._queryReviewAttempt_ReviewItems(reviewAttempt.getId()).isEmpty()) {
            loadReviewItemsFromServer(reviewAttempt, reviewAttempt.getReviewUrl());
        } else {
            addSubjectFilterItemsInSpinner();
            addTimeFilterItemsInSpinner();
            showFilteredContent(FILTER_ALL);
        }
        timeSpinner.setAdapter(timeSpinnerAdapter);
        subjectSpinner.setAdapter(subjectSpinnerAdapter);
        subjectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Collections.sort(subjects);
                CURRENT_SUBJECT_SORT = subjects.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        timeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                CURRENT_TIME_SORT = timeFilters[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    private ArrayList<String> getSubjects() {
        subjects = new ArrayList<>();

        reviewItems = reviewItemDao.queryBuilder()
                .where(ReviewItemDao.Properties.AttemptId.eq(reviewAttempt.getId())).list();

        for(ReviewItem reviewItem : reviewItems) {
            if (!subjects.contains(reviewItem.getQuestion().getSubject())) {
                subjects.add(reviewItem.getQuestion().getSubject());
            }
        }
        return subjects;
    }

    private void setFilter(String filter) {
        resetBackgroundsOfAllButton();
        if (filter == FILTER_CORRECT && CURRENT_FILTER != FILTER_CORRECT) {
            setBackgroundToOnState(correctFilterButton);
        } else if (filter == FILTER_INCORRECT && CURRENT_FILTER != FILTER_INCORRECT) {
            setBackgroundToOnState(incorrectFilterButton);
        } else if (filter == FILTER_UNANSWERED && CURRENT_FILTER != FILTER_UNANSWERED) {
            setBackgroundToOnState(unansweredFilterButton);
        }
        if(filter != CURRENT_FILTER) {
            CURRENT_FILTER = filter;
        } else {
            CURRENT_FILTER = FILTER_ALL;
        }
        //showFilteredContent(CURRENT_FILTER);
    }

    private void resetBackgroundsOfAllButton() {
        setBackgroundToOffState(correctFilterButton);
        setBackgroundToOffState(incorrectFilterButton);
        setBackgroundToOffState(unansweredFilterButton);
    }

    private void setBackgroundToOffState(Button button) {
        button.setTextColor(getResources().getColor(R.color.testpress_black));
        if (button.getId() == correctFilterButton.getId()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                correctFilterButton.setBackground(getResources().getDrawable(R.drawable.testpress_filter_button_left_off));
            } else {
                correctFilterButton.setBackgroundColor(getResources().getColor(R.color.testpress_color_primary_blue));
            }
        } else if (button.getId() == incorrectFilterButton.getId()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                incorrectFilterButton.setBackground(getResources().getDrawable(R.drawable.testpress_filter_button_middle_off));
            } else {
                incorrectFilterButton.setBackgroundColor(getResources().getColor(R.color.testpress_color_primary_blue));
            }
        } else if (button.getId() == unansweredFilterButton.getId()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                unansweredFilterButton.setBackground(getResources().getDrawable(R.drawable.testpress_filter_button_right_off));
            } else {
                unansweredFilterButton.setBackgroundColor(getResources().getColor(R.color.testpress_color_primary_blue));
            }
        }
    }

    private void setBackgroundToOnState(Button button) {
        button.setTextColor(getResources().getColor(R.color.testpress_white));
        if (button.getId() == correctFilterButton.getId()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                correctFilterButton.setBackground(getResources().getDrawable(R.drawable.testpress_filter_button_left_on));
            } else {
                correctFilterButton.setBackgroundColor(getResources().getColor(R.color.testpress_text_gray_light));
            }
        } else if (button.getId() == incorrectFilterButton.getId()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                incorrectFilterButton.setBackground(getResources().getDrawable(R.drawable.testpress_filter_button_middle_on));
            } else {
                incorrectFilterButton.setBackgroundColor(getResources().getColor(R.color.testpress_text_gray_light));
            }
        } else if (button.getId() == unansweredFilterButton.getId()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                unansweredFilterButton.setBackground(getResources().getDrawable(R.drawable.testpress_filter_button_right_on));
            } else {
                unansweredFilterButton.setBackgroundColor(getResources().getColor(R.color.testpress_text_gray_light));
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(R.id.options == item.getItemId()) {
            mainLayout.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
            setPanelOpen(!slidingPaneLayout.isOpen());
        }
        return super.onOptionsItemSelected(item);
    }

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

    protected void showFilteredContent(String filter) {
        progressBar.setVisibility(View.VISIBLE);
        queryBuilder = getQueryBuilder(filter);
        filterReviewItems();
    }

    private void filterReviewItems() {
        if (reviewAttempt != null) {
            if (CURRENT_TIME_SORT == "ASC") {
                reviewItems = queryBuilder.distinct().orderAsc(ReviewItemDao.Properties.Duration).list();
            } else if (CURRENT_TIME_SORT == "DESC") {
                reviewItems = queryBuilder.distinct().orderDesc(ReviewItemDao.Properties.Duration).list();
            } else {
                reviewItems = queryBuilder.distinct().orderAsc(ReviewItemDao.Properties.Index).list();
            }
            if (reviewItems == null) {
                setEmptyText(R.string.testpress_error_loading_questions,
                        R.string.testpress_some_thing_went_wrong_try_again);
            } else {
                //pagerAdapter.setReviewItems(reviewItems);
                updateWebView();
                if (languageList != null && languageList.size() > 1) {
                    if (selectedLanguage == null) {
                        int selectedPosition = languageSpinnerAdapter
                                .getItemPositionFromTag(reviewItems.get(0).getQuestion().getLanguage());

                        initSelectedLanguage(languageList.get(selectedPosition));
                        languageSpinner.setSelection(selectedPosition);
                    }
                    selectLanguageMenu.setVisible(true);
                }
                //pagerAdapter.notifyDataSetChanged(true);
                filterMenu.setVisible(true);
                mainLayout.setVisibility(View.VISIBLE);
                emptyView.setVisibility(View.GONE);
            }
        }
        progressBar.setVisibility(View.GONE);
    }

    @SuppressLint("AddJavascriptInterface")
    private void updateWebView() {
        progressBar.setVisibility(View.VISIBLE);
        mainLayout.setVisibility(View.GONE);
        if(webViewUtils == null) {
            webView.addJavascriptInterface(new TimeAnalyticsListener(), "TimeAnalyticsListener");
            webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
            webView.setScrollbarFadingEnabled(true);
            webViewUtils = new WebViewUtils(webView) {
                @Override
                protected void onLoadFinished() {
                    progressBar.setVisibility(View.GONE);
                    mainLayout.setVisibility(View.VISIBLE);
                    super.onLoadFinished();
                }

                @Override
                public String getJavascript(Context context) {
                    return super.getJavascript(context) + CommonUtils.getStringFromAsset(context,
                            "TestpressTimeAnalytics.js");
                }
            };
            webViewUtils.initWebView(getHtmlForAll(), activity);
        } else {
            webViewUtils.loadHtml(getHtmlForAll());
        }
    }

    private void addTimeFilterItemsInSpinner() {
        timeSpinnerAdapter.clear();

        timeSpinnerAdapter.addItem("NONE", "Sort by question number", false, 0);
        timeSpinnerAdapter.addItem("DESC", "Sort by most time taken", true, 0);
        timeSpinnerAdapter.addItem("ASC", "Sort by least time taken", true, 0);

        if (selectedItemPosition == -1) {
            selectedItemPosition = 0;
            timeSpinnerAdapter.notifyDataSetChanged();
        } else {
            timeSpinnerAdapter.notifyDataSetChanged();
            timeSpinner.setSelection(selectedItemPosition);
        }
    }

    private void addSubjectFilterItemsInSpinner() {
        subjectSpinnerAdapter.clear();

        subjectSpinnerAdapter.addItem(FILTER_ALL, "All", false, 0);
        subjects = getSubjects();
        Collections.sort(subjects);
        subjectSpinnerAdapter.addItems(subjects);
        subjects.add("All");
        Collections.sort(subjects);

        if (selectedItemPosition == -1) {
            selectedItemPosition = 0;
            subjectSpinnerAdapter.notifyDataSetChanged();
        } else {
            subjectSpinnerAdapter.notifyDataSetChanged();
            subjectSpinner.setSelection(selectedItemPosition);
        }
    }

    private void initSelectedLanguage(Language language) {
        // Create new object so that we can update it without affecting original languages list
        selectedLanguage = new Language(language);
    }

    private void setPanelOpen(boolean open) {
        if(open) {
            slidingPaneLayout.openPane();
        } else {
            slidingPaneLayout.closePane();
        }
    }

    private void loadReviewItemsFromServer(final ReviewAttempt reviewAttempt, final String url) {
        reviewItemsApiRequest = new TestpressExamApiClient(this)
                .getReviewItems(url, new HashMap<String, Object>())
                .enqueue(new TestpressCallback<TestpressApiResponse<ReviewItem>>() {
                    @Override
                    public void onSuccess(TestpressApiResponse<ReviewItem> response) {
                        reviewItems.addAll(response.getResults());
                        // Load the next page if exists.
                        if (response.getNext() != null) {
                            loadReviewItemsFromServer(reviewAttempt, response.getNext());
                        } else {
                            saveReviewItems();
                            addSubjectFilterItemsInSpinner();
                            addTimeFilterItemsInSpinner();
                            showFilteredContent("all");
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

    private void saveReviewItems() {
        for (int i = 0; i < reviewItems.size(); i++) {
            ReviewItem reviewItem = reviewItems.get(i);
            // Store selected answers
            if (reviewItem.getSelectedAnswers() != null) {
                for (int selectedAnswerId : reviewItem.getSelectedAnswers()) {
                    SelectedAnswerDao selectedAnswersDao = TestpressSDKDatabase.getSelectedAnswerDao(this);
                    SelectedAnswer selectedAnswer = new SelectedAnswer();
                    selectedAnswer.setAnswerId(selectedAnswerId);
                    selectedAnswer.setReviewItemId(reviewItem.getId());
                    selectedAnswersDao.insertOrReplace(selectedAnswer);
                }
            }
            // Store question
            ReviewQuestionDao reviewQuestionDao = TestpressSDKDatabase.getReviewQuestionDao(this);
            ReviewAnswerDao reviewAnswerDao = TestpressSDKDatabase.getReviewAnswerDao(this);
            ReviewQuestion reviewQuestion = reviewItem.getRawQuestion();
            reviewQuestionDao.insertOrReplace(reviewQuestion);
            // Store answers
            for (ReviewAnswer reviewAnswer : reviewQuestion.getAnswers()) {
                reviewAnswer.setQuestionId(reviewQuestion.getId());
                reviewAnswerDao.insertOrReplace(reviewAnswer);
            }
            // Store translations
            for (ReviewQuestionTranslation translation : reviewQuestion.getTranslations()) {
                translation.setQuestionId(reviewQuestion.getId());
                ReviewQuestionTranslationDao translationDao =
                        TestpressSDKDatabase.getReviewQuestionTranslationDao(this);

                translationDao.insertOrReplace(translation);
                for (ReviewAnswerTranslation answerTranslation : translation.getAnswers()) {
                    ReviewAnswer answer = reviewAnswerDao.queryBuilder()
                            .where(ReviewAnswerDao.Properties.Id.eq(answerTranslation.getId()))
                            .list().get(0);
                    answerTranslation.setIsCorrect(answer.getIsCorrect());
                    answerTranslation.setQuestionTranslationId(translation.getId());
                    ReviewAnswerTranslationDao reviewAnswerTranslationDao =
                            TestpressSDKDatabase.getReviewAnswerTranslationDao(this);

                    reviewAnswerTranslationDao.insertOrReplace(answerTranslation);
                }
            }
            // Store review item
            reviewItem.setQuestionId(reviewQuestion.getId());
            reviewItem.setAttemptId(reviewAttempt.getId());
            reviewItem.setIndex(i + 1);
            reviewItemDao.insertOrReplace(reviewItem);
        }
    }

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
            default:
                queryBuilder = reviewItemDao.queryBuilder();
                break;
        }
        if (CURRENT_SUBJECT_SORT != "All") {
            queryBuilder = queryBuilder.where(
                    new WhereCondition.StringCondition("" +
                            " T.QUESTION_ID IN (SELECT ID FROM REVIEW_QUESTION Q" +
                            " WHERE Q.SUBJECT = '"+CURRENT_SUBJECT_SORT+"')"));
        }
        return queryBuilder;
    }

    protected void setEmptyText(final int title, final int description) {
        mainLayout.setVisibility(View.GONE);
        emptyTitleView.setText(title);
        emptyDescView.setText(description);
        retryButton.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    private String getHtmlForAll() {

        String html = getExamHeader();

        if(reviewItems.isEmpty()) {
            return html + "<div class='no-question-error'>No question in this filter</div>";
        }

        for(ReviewItem reviewItem : reviewItems) {
            html += getHtml(reviewItem.getIndex(), reviewItem.getQuestion().getQuestionHtml(),
                    reviewItem.getDuration(), reviewItem.getAverageDuration(),
                    reviewItem.getBestDuration());
        }
        return html;
    }

    private String getExamHeader() {
        String html = "<div class='exam-title'>" + exam.getTitle() + "</div>" +
                "<div class='exam-description'>" + exam.getDescription() + "</div>";
        return html;
    }

    private String getHtml(int index, String questionHtml,
                           String yourTime, String averageTime, String bestTime) {
        String html =
                "<div id='question"+index+"'>" +
                    "<div style='padding-left: 20px; padding-right: 20px;'>";
        html +=         "<div class='wrapper'>";
        //Add question index
        html +=             "<div class='time-analytics-question-index'>" +
                                "<b>" + index + "</b>" +
                            "</div>";
        //Add question
        html +=             "<span class='time-analytics-question'>" +
                                questionHtml +
                            "</span>" +
                        "</div>";

        html +=         "<div style='padding-top: 30px;'>";
        //Add times
        html +=             "<div class='wrapper' style='margin-bottom: 20px;'>" +
                                "<div class='time-heading'><b>Your time</b></div>" +
                                "<span class='time'>" + yourTime + "</span>" +
                            "</div>" +

                            "<div class='wrapper' style='margin-bottom: 20px;'>" +
                                "<div class='time-heading'><b>Average time</b></div>" +
                                "<span class='time'>" + averageTime + "</span>" +
                            "</div>" +

                            "<div class='wrapper'>" +
                                "<div class='time-heading'><b>Best time so far</b></div>" +
                                "<span class='time'>" + bestTime + "</span>" +
                            "</div>";
        html +=         "</div>";

        //Add Show answer button
        html +=         "<div class='show-answer' onClick=\"openReviewQuestionsActivity('"+index+"')\">" +
                            "Show answer" +
                        "</div>" +
                    "</div>" +
                    "<hr style='height:1px; color:#ddd;'>" +
                "</div>";

        return html;
    }

    @Override
    public RetrofitCall[] getRetrofitCalls() {
        return new RetrofitCall[] { reviewItemsApiRequest };
    }

    public class TimeAnalyticsListener {

        @JavascriptInterface
        public void openReviewQuestionsActivity(String index) {
            int position = Integer.parseInt(index) - 1;
            activity.startActivity(
                    ReviewQuestionsActivity.createIntent(activity, exam, attempt, position)
            );
        }
    }
}
