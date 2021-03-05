package in.testpress.exam.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.slidingpanelayout.widget.SlidingPaneLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import junit.framework.Assert;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSDKDatabase;
import in.testpress.core.TestpressSdk;
import in.testpress.exam.R;
import in.testpress.exam.api.TestpressExamApiClient;
import in.testpress.exam.ui.view.NonSwipeableViewPager;
import in.testpress.models.InstituteSettings;
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
import in.testpress.util.UIUtils;
import in.testpress.util.ViewUtils;

import static in.testpress.models.greendao.ReviewItem.ANSWERED_CORRECT;
import static in.testpress.models.greendao.ReviewItem.ANSWERED_INCORRECT;
import static in.testpress.models.greendao.ReviewItem.UNANSWERED;

public class ReviewQuestionsActivity extends BaseToolBarActivity implements ReviewPanelListAdapter.ListItemClickListener {

    static final String PARAM_ATTEMPT = "attempt";
    static final String PARAM_EXAM = "exam";
    static final String PARAM_POSITION = "position";
    public static final String FILTER_ALL = "all";
    public static final String FILTER_CORRECT = "correct";
    public static final String FILTER_INCORRECT = "incorrect";
    public static final String FILTER_UNANSWERED = "unanswered";
    public static final String FILTER_MARKED = "marked";
    private Exam exam;
    private Attempt attempt;
    private ReviewAttempt reviewAttempt;
    private List<ReviewItem> reviewItems = new ArrayList<>();
    private QueryBuilder<ReviewItem> queryBuilder;
    SlidingPaneLayout slidingPaneLayout;
    private NonSwipeableViewPager pager;
    private ReviewQuestionsPagerAdapter pagerAdapter;
    private ReviewPanelListAdapter panelListAdapter;
    private Spinner spinner;
    private Spinner languageSpinner;
    private View questionLayout;
    View buttonLayout;
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
    private MenuItem selectLanguageMenu;
    private ReviewItemDao reviewItemDao;
    private ReviewAttemptDao attemptDao;
    private Language selectedLanguage;
    private ExploreSpinnerAdapter languageSpinnerAdapter;
    protected ExploreSpinnerAdapter spinnerAdapter;
    private View questionsListProgressBar;
    /**
     * When spinnerAdapter is set to spinner, the spinner itself select first item as default,
     * so onItemSelected callback will be called with position 0, we need to omit this callback
     */
    protected Boolean spinnerDefaultCallback = true;
    protected int selectedItemPosition = -1;
    private RetrofitCall<TestpressApiResponse<ReviewItem>> reviewItemsLoader;
    private RetrofitCall<TestpressApiResponse<Language>> languageApiRequest;
    private TestpressExamApiClient apiClient;
    private Menu optionsMenu;
    String reviewUrl;
    int totalQuestions = 0;
    private boolean isNetworkRequestLoading = false;

    public static Intent createIntent(Activity activity, Exam exam, Attempt attempt) {
        Intent intent = new Intent(activity, ReviewQuestionsActivity.class);
        intent.putExtra(ReviewQuestionsActivity.PARAM_EXAM, exam);
        intent.putExtra(ReviewQuestionsActivity.PARAM_ATTEMPT, attempt);
        return intent;
    }

    public static Intent createIntent(Activity activity, Exam exam, Attempt attempt, int position) {
        Intent intent = new Intent(activity, ReviewQuestionsActivity.class);
        intent.putExtra(ReviewQuestionsActivity.PARAM_EXAM, exam);
        intent.putExtra(ReviewQuestionsActivity.PARAM_ATTEMPT, attempt);
        intent.putExtra(PARAM_POSITION, position);
        return intent;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testpress_activity_review_question);
        parseArguments();
        setTitle(exam.getTitle() + " Solutions");
        bindViews();
        initializeQuestionsListSidebar();
        initializeQuestionPager();
        addListeners();
        loadReviewAttempt();
        reviewUrl = reviewAttempt.getReviewUrl().replace("v2.3", "v2.2.1");
        // Check review items exists for the review attempt, load otherwise.
        totalQuestions = attempt.getTotalQuestions();
        displayQuestions();
        int position = getIntent().getIntExtra(PARAM_POSITION, -1);
        if (position != -1) {
            goToQuestion(position);
        }

        InstituteSettings instituteSettings = TestpressSdk.getTestpressSession(this).getInstituteSettings();
        if (instituteSettings.isGrowthHackEnabled()) {
            customiseToolbar();
        }
    }

    private void loadReviewAttempt() {
        reviewItemDao= TestpressSDKDatabase.getReviewItemDao(this);
        attemptDao = TestpressSDKDatabase.getReviewAttemptDao(this);
        reviewAttempt = getReviewAttempt();
    }

    private void displayQuestions() {
        if (reviewItemDao._queryReviewAttempt_ReviewItems(reviewAttempt.getId()).isEmpty()) {
            loadReviewItemsFromServer(reviewUrl);
        } else {
            displayReviewItems();
        }
    }

    private void initializeQuestionsListSidebar() {
        panelListAdapter = new ReviewPanelListAdapter(reviewItems,
                R.layout.testpress_test_panel_list_item, this, this);
        questionsListView.setAdapter(panelListAdapter);
        questionsListView.addFooterView(questionsListProgressBar);
    }

    private void initializeQuestionPager() {
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
    }


    private void bindViews() {
        questionsListView = (ListView) findViewById(R.id.questions_list_view);
        emptyView = findViewById(R.id.empty_container);
        emptyTitleView = (TextView) findViewById(R.id.empty_title);
        emptyDescView = (TextView) findViewById(R.id.empty_description);
        retryButton = (Button) findViewById(R.id.retry_button);
        previousButton = (Button) findViewById(R.id.previous);
        nextButton = (Button) findViewById(R.id.next);
        questionsListButton = (Button) findViewById(R.id.question_list_button);
        progressBar = (ProgressBar) findViewById(R.id.pb_loading);
        questionsListProgressBar = (View) LayoutInflater.from(this).inflate(R.layout.progress_bar, null);
        UIUtils.setIndeterminateDrawable(this, progressBar, 4);
        questionLayout = findViewById(R.id.question_layout);
        buttonLayout = findViewById(R.id.button_layout);
        pager = (NonSwipeableViewPager) findViewById(R.id.pager);
        slidingPaneLayout = (SlidingPaneLayout) findViewById(R.id.sliding_layout);
        spinnerAdapter = new ExploreSpinnerAdapter(getLayoutInflater(), getResources(), true);
        spinnerAdapter.hideSpinner(true);
    }

    private void parseArguments() {
        exam = getIntent().getParcelableExtra(PARAM_EXAM);
        Assert.assertNotNull("PARAM_EXAM must not be null", exam);
        attempt = getIntent().getParcelableExtra(PARAM_ATTEMPT);
        Assert.assertNotNull("PARAM_ATTEMPT must not be null", attempt);
    }

    private void customiseToolbar() {
        toolbar.setBackgroundColor(Color.WHITE);
        toolbar.getOverflowIcon().setColorFilter(getResources().getColor(R.color.testpress_color_primary), PorterDuff.Mode.SRC_ATOP);
        toolbar.setTitleTextColor(getResources().getColor(R.color.testpress_color_primary));
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        showLogoInToolbar();
    }

    private void addListeners() {
        questionsListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItems, int totalItems) {
                if (totalItems == totalQuestions || totalItems == 0) {
                    return;
                }
                if ((totalItems - firstVisibleItem) == visibleItems) {
                    if (totalItems < totalQuestions) {
                        loadReviewItemsFromServer(reviewUrl);
                    }
                }
            }
        });
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        optionsMenu = menu;
        setUpLanguageOptionsMenu();
        setupQuestionsFilterOptionsMenu();
        filterMenu.setVisible(false);

        if (!reviewItems.isEmpty()) {
//            filterMenu.setVisible(true);
        }
        return super.onCreateOptionsMenu(menu);
    }

    void setupQuestionsFilterOptionsMenu() {
        getMenuInflater().inflate(R.menu.testpress_filter, optionsMenu);
        filterMenu = optionsMenu.findItem(R.id.filter);
        final View circle = filterMenu.getActionView().findViewById(R.id.filter_applied_sticky_tick);
        spinner = filterMenu.getActionView().findViewById(R.id.spinner);
        ViewUtils.setSpinnerIconColor(this, spinner);
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
        addFilterItemsInSpinner();
    }

    void setUpLanguageOptionsMenu() {
        final ArrayList<Language> languages = new ArrayList<>(exam.getRawLanguages());
        if (languages.size() > 1 && optionsMenu != null && !reviewItems.isEmpty()) {
            getMenuInflater().inflate(R.menu.testpress_select_language_menu, optionsMenu);
            selectLanguageMenu = optionsMenu.findItem(R.id.select_language);
            languageSpinner = selectLanguageMenu.getActionView().findViewById(R.id.language_spinner);
            ViewUtils.setSpinnerIconColor(this, languageSpinner);
            languageSpinnerAdapter =
                    new ExploreSpinnerAdapter(getLayoutInflater(), getResources(), false);

            languageSpinnerAdapter.hideSpinner(true);
            for (Language language : languages) {
                languageSpinnerAdapter.addItem(language.getCode(), language.getTitle(), true, 0);
            }
            languageSpinner.setAdapter(languageSpinnerAdapter);
            initSelectedLanguage(languages);
            languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position,
                                           long id) {

                    Language language = languages.get(position);
                    if (!selectedLanguage.getCode().equals(language.getCode())) {
                        // Update existing object so that update will reflect in ReviewQuestionFragment also
                        selectedLanguage.update(language);
                        exam.setSelectedLanguage(selectedLanguage.getCode());
                        pagerAdapter.notifyDataSetChanged(false);
                        panelListAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }
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

    private void loadReviewItemsFromServer(final String url) {
        if (isNetworkRequestLoading) {
            return;
        }
        isNetworkRequestLoading = true;

        questionsListProgressBar.setVisibility(View.VISIBLE);
        apiClient = new TestpressExamApiClient(this);
        reviewItemsLoader = apiClient.getReviewItems(url, new HashMap<String, Object>())
                .enqueue(new TestpressCallback<TestpressApiResponse<ReviewItem>>() {
                    @Override
                    public void onSuccess(TestpressApiResponse<ReviewItem> response) {
                        reviewItems.addAll(response.getResults());
                        reviewUrl = response.getNext();
                        totalQuestions = response.getCount();
                        saveReviewItems();
                        displayReviewItems();
                        goToQuestion(pager.getCurrentItem());
                        isNetworkRequestLoading = false;
                        questionsListProgressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        handleError(exception, R.string.testpress_error_loading_questions);
                        isNetworkRequestLoading = false;
                        questionsListProgressBar.setVisibility(View.GONE);
                        retryButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                progressBar.setVisibility(View.VISIBLE);
                                emptyView.setVisibility(View.GONE);
                                loadReviewItemsFromServer(url);
                            }
                        });
                    }
                });
    }

    private void saveReviewItems() {
        for (int i = 0; i < reviewItems.size(); i++) {
            ReviewItem reviewItem = reviewItems.get(i);
            ReviewQuestion reviewQuestion = reviewItem.getRawQuestion();
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

    void displayReviewItems() {
        String rawQuery = "SELECT LANGUAGE FROM REVIEW_QUESTION AS Q " +
                "INNER JOIN REVIEW_ITEM AS I ON I.QUESTION_ID=Q.ID " +
                "WHERE I.ATTEMPT_ID=" + attempt.getId() + " " +
                "UNION " +
                "SELECT LANGUAGE FROM REVIEW_QUESTION_TRANSLATION AS T " +
                "INNER JOIN (SELECT AQ.ID AS AQ_ID FROM REVIEW_QUESTION AS AQ " +
                "   INNER JOIN REVIEW_ITEM AS RI ON RI.QUESTION_ID=AQ.ID" +
                "   WHERE RI.ATTEMPT_ID=" + attempt.getId() + ") AS C " +
                "ON T.QUESTION_ID=AQ_ID " +
                "ORDER BY LANGUAGE;";

        Cursor cursor =
                TestpressSDKDatabase.getDaoSession(this).getDatabase().rawQuery(rawQuery, null);

        ArrayList<String> languageCodes = new ArrayList<>();
        //noinspection TryFinallyCanBeTryWithResources
        try {
            if (cursor.moveToFirst()) {
                do {
                    languageCodes.add(cursor.getString(0));
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }
        Map<String, Language> uniqueLanguages = new HashMap<>();
        for (String languageCode : languageCodes) {
            List<Language> languagesFromDB = TestpressSDKDatabase.getLanguageDao(this).queryBuilder()
                    .where(LanguageDao.Properties.Code.eq(languageCode))
                    .list();

            if (languagesFromDB.isEmpty()) {
                fetchLanguages();
            } else {
                Language language = languagesFromDB.get(0);
                uniqueLanguages.put(language.getCode(), language);
            }
        }
        exam.setLanguages(new ArrayList<>(uniqueLanguages.values()));
        setUpLanguageOptionsMenu();
        onSpinnerItemSelected(0);
    }

    void fetchLanguages() {
        progressBar.setVisibility(View.VISIBLE);
        languageApiRequest = apiClient.getLanguages(exam.getSlug())
                .enqueue(new TestpressCallback<TestpressApiResponse<Language>>() {
                    @Override
                    public void onSuccess(TestpressApiResponse<Language> apiResponse) {
                        List<Language> languages = exam.getRawLanguages();
                        languages.addAll(apiResponse.getResults());
                        Map<String, Language> uniqueLanguages = new HashMap<>();
                        for (Language language : languages) {
                            uniqueLanguages.put(language.getCode(), language);
                        }
                        exam.setLanguages(new ArrayList<>(uniqueLanguages.values()));
                        if (apiResponse.hasMore()) {
                            fetchLanguages();
                        } else {
                            exam.saveLanguages(getBaseContext());
                            displayReviewItems();
                        }
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        handleError(exception, R.string.testpress_error_loading_languages);
                        retryButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                progressBar.setVisibility(View.VISIBLE);
                                emptyView.setVisibility(View.GONE);
                                fetchLanguages();
                            }
                        });
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
        QueryBuilder<ReviewItem> queryBuilder = reviewItemDao.queryBuilder()
                .where(ReviewItemDao.Properties.AttemptId.eq(reviewAttempt.getId()));

        switch (filterType) {
            case FILTER_CORRECT:
                queryBuilder.where(ReviewItemDao.Properties.Result.eq(ANSWERED_CORRECT));
                break;
            case FILTER_INCORRECT:
                queryBuilder.where(ReviewItemDao.Properties.Result.eq(ANSWERED_INCORRECT));
                break;
            case FILTER_UNANSWERED:
                queryBuilder.where(queryBuilder.or(
                        ReviewItemDao.Properties.Result.isNull(),
                        ReviewItemDao.Properties.Result.eq(UNANSWERED))
                );
                break;
            case FILTER_MARKED:
                queryBuilder.where(ReviewItemDao.Properties.Review.eq(true));
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
                pagerAdapter.notifyDataSetChanged(true);
                if (filterMenu != null) {
                    filterMenu.setVisible(true);
                }
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

        if ((pagerAdapter.getCount() < totalQuestions) && ((pagerAdapter.getCount() - position) <= 4)) {
            loadReviewItemsFromServer(reviewUrl);
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

    private void initSelectedLanguage(ArrayList<Language> languages) {
        String selectedLanguageCode = exam.getSelectedLanguage();
        if (selectedLanguageCode == null || selectedLanguageCode.isEmpty()) {
            selectedLanguageCode = reviewItems.get(0).getQuestion().getLanguage();
        }
        int selectedPosition =
                languageSpinnerAdapter.getItemPositionFromTag(selectedLanguageCode);

        if (selectedLanguage == null) {
            // Create new object so that we can update it without affecting original language list
            selectedLanguage = new Language(languages.get(selectedPosition));
            pagerAdapter.setSelectedLanguage(selectedLanguage);
            panelListAdapter.setSelectedLanguage(selectedLanguage);
        }
        languageSpinner.setSelection(selectedPosition);
        selectLanguageMenu.setVisible(true);
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

    void handleError(TestpressException exception, @StringRes int errorMessage) {
        if (exception.isUnauthenticated()) {
            setEmptyText(R.string.testpress_authentication_failed,
                    R.string.testpress_exam_no_permission);
        } else if (exception.isNetworkError()) {
            setEmptyText(R.string.testpress_network_error, R.string.testpress_no_internet_try_again);
            retryButton.setVisibility(View.VISIBLE);
        } else if (exception.getResponse() != null && exception.getResponse().code() == 404) {
            setEmptyText(R.string.testpress_exam_not_available,
                    R.string.testpress_exam_not_available_description);
        } else {
            setEmptyText(errorMessage, R.string.testpress_some_thing_went_wrong_try_again);
        }
    }

    @Override
    public RetrofitCall[] getRetrofitCalls() {
        return new RetrofitCall[] {
                reviewItemsLoader, languageApiRequest
        };
    }

    @Override
    public void onItemClicked(int position) {
        goToQuestion(position);
        setPanelOpen(false);
    }
}
