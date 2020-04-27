package in.testpress.exam.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.view.ViewCompat;
import androidx.loader.app.LoaderManager;
import androidx.core.content.ContextCompat;
import androidx.loader.content.Loader;
import androidx.viewpager.widget.ViewPager;
import androidx.slidingpanelayout.widget.SlidingPaneLayout;
import androidx.appcompat.app.AlertDialog;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSdk;
import in.testpress.exam.R;
import in.testpress.exam.models.AttemptItem;
import in.testpress.exam.pager.TestQuestionsPager;
import in.testpress.exam.api.TestpressExamApiClient;
import in.testpress.exam.ui.loaders.AttemptItemsLoader;
import in.testpress.exam.ui.view.NonSwipeableViewPager;
import in.testpress.models.InstituteSettings;
import in.testpress.models.greendao.Attempt;
import in.testpress.models.greendao.AttemptSection;
import in.testpress.models.greendao.Content;
import in.testpress.models.greendao.CourseAttempt;
import in.testpress.models.greendao.Exam;
import in.testpress.models.greendao.Language;
import in.testpress.network.RetrofitCall;
import in.testpress.ui.BaseFragment;
import in.testpress.ui.ExploreSpinnerAdapter;
import in.testpress.util.CommonUtils;
import in.testpress.util.ThrowableLoader;
import in.testpress.util.UIUtils;
import in.testpress.util.ViewUtils;

import static in.testpress.exam.ui.TestActivity.PARAM_COURSE_ATTEMPT;
import static in.testpress.exam.ui.TestActivity.PARAM_COURSE_CONTENT;
import static in.testpress.models.greendao.Attempt.COMPLETED;
import static in.testpress.models.greendao.Attempt.NOT_STARTED;

public class TestFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<List<AttemptItem>>, PlainSpinnerItemAdapter.SectionInfoClickListener {

    private static final int APP_BACKGROUND_DELAY = 60000; // 1m

    static final String PARAM_EXAM = "exam";
    static final String PARAM_ATTEMPT = "attempt";
    SlidingPaneLayout slidingPaneLayout;
    private TestpressExamApiClient apiClient;
    private TextView previous;
    private TextView next;
    private ListView questionsListView;
    private TextView timer;
    private Spinner questionsFilter;
    Spinner sectionsFilter;
    private RelativeLayout sectionsFilterContainer;
    private NonSwipeableViewPager viewPager;
    private TestQuestionPagerAdapter viewPagerAdapter;
    private List<AttemptItem> filterItems = new ArrayList<>();
    private TestPanelListAdapter questionsListAdapter;
    private ProgressDialog progressDialog;
    private AlertDialog endExamAlertDialog;
    private AlertDialog pauseExamAlertDialog;
    private AlertDialog resumeExamDialog;
    private AlertDialog sectionSwitchAlertDialog;
    private AlertDialog heartBeatAlertDialog;
    private AlertDialog saveAnswerAlertDialog;
    private AlertDialog networkErrorAlertDialog;
    private AlertDialog sectionInfoAlertDialog;
    private View questionsListProgressBar;

    Attempt attempt;
    private Exam exam;
    private Content courseContent;
    private CourseAttempt courseAttempt;
    private int currentQuestionIndex;
    List<AttemptSection> sections = new ArrayList<>();
    public TestQuestionsPager questionsResourcePager;
    List<AttemptItem> attemptItemList = new ArrayList<>();
    CountDownTimer countDownTimer;
    long millisRemaining = -1;
    LockableSpinnerItemAdapter sectionSpinnerAdapter;
    private PlainSpinnerItemAdapter plainSpinnerAdapter;
    private Language selectedLanguage;
    private Boolean fistTimeCallback = false;
    private int selectedPlainSpinnerItemOffset;
    private boolean navigationButtonPressed;
    /*
     * Map of subjects/sections & its starting point(first question index)
     */
    private HashMap<String, Integer> plainSpinnerItemOffsets = new HashMap<>();
    private enum Action { PAUSE, END, UPDATE_ANSWER, END_SECTION }
    private RetrofitCall<Attempt> heartBeatApiRequest;
    private RetrofitCall<AttemptSection> endSectionApiRequest;
    private RetrofitCall<AttemptSection> startSectionApiRequest;
    private RetrofitCall<CourseAttempt> endContentAttemptApiRequest;
    private RetrofitCall<Attempt> endAttemptApiRequest;
    private RetrofitCall<Attempt> resumeExamApiRequest;
    private Handler appBackgroundStateHandler;
    private Runnable stopTimerTask = new Runnable() {
        @Override
        public void run() {
            stopTimerOnAppWentBackground();
        }
    };
    public int totalQuestions = 0;
    private boolean isNextPageQuestionsBeingFetched = false;
    private InstituteSettings instituteSettings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeAttemptAndExamVariables(savedInstanceState);
        initializeResourcePager();
        instituteSettings = TestpressSdk.getTestpressSession(getContext()).getInstituteSettings();
    }

    private void initializeAttemptAndExamVariables(Bundle savedInstanceState) {
        courseContent = getArguments().getParcelable(PARAM_COURSE_CONTENT);
        if (courseContent != null) {
            courseAttempt = getArguments().getParcelable(PARAM_COURSE_ATTEMPT);
            exam = courseContent.getRawExam();
            attempt = courseAttempt.getRawAssessment();
        } else {
            attempt = getArguments().getParcelable(PARAM_ATTEMPT);
            exam = getArguments().getParcelable(PARAM_EXAM);
        }

        if (savedInstanceState != null && savedInstanceState.getParcelable(PARAM_ATTEMPT) != null) {
            attempt = savedInstanceState.getParcelable(PARAM_ATTEMPT);
        }
        sections = attempt.getSections();
    }

    private void initializeResourcePager() {
        String questionUrl = attempt.getQuestionsUrlFrag();
        if (attempt.hasSectionalLock()) {
            questionUrl = sections.get(attempt.getCurrentSectionPosition()).getQuestionsUrlFrag();
        }
        questionUrl = questionUrl.replace("v2.3", "v2.2.1");
        apiClient = new TestpressExamApiClient(getActivity());
        questionsResourcePager = new TestQuestionsPager(questionUrl, apiClient);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.testpress_fragment_test_engine, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews();
        initializeProgressDialog();
        initializeListeners();
        initializeQuestionsListAdapter();

        if (attempt.hasSections() || exam.getTemplateType() == 2) {
            initializeSectionsFilter();
        }

        if (exam.hasMultipleLanguages()) {
            initializeLanguageFilter();
        }
    }

    private void initializeLanguageFilter() {
        Spinner languagesFilter = getView().findViewById(R.id.language_spinner);
        final ArrayList<Language> languages = new ArrayList<>(exam.getRawLanguages());
        ExploreSpinnerAdapter languageSpinnerAdapter =
                new ExploreSpinnerAdapter(getLayoutInflater(), getResources(), false);

        for (Language language : languages) {
            languageSpinnerAdapter.addItem(language.getCode(), language.getTitle(), true, 0);
        }
        languageSpinnerAdapter.hideSpinner(true);
        languagesFilter.setAdapter(languageSpinnerAdapter);
        languagesFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Update existing object so that update will reflect in TestQuestionFragment also
                selectedLanguage.update(languages.get(position));
                exam.setSelectedLanguage(selectedLanguage.getCode());
                if (viewPagerAdapter != null) {
                    viewPagerAdapter.notifyDataSetChanged();

                }
                questionsListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        String selectedLanguageCode = exam.getSelectedLanguage();
        if (selectedLanguageCode != null && !selectedLanguageCode.isEmpty()) {
            int selectedPosition =
                    languageSpinnerAdapter.getItemPositionFromTag(selectedLanguageCode);

            // Create new object so that we can update it without affecting original language list
            selectedLanguage = new Language(languages.get(selectedPosition));
            questionsListAdapter.setSelectedLanguage(selectedLanguage);
            languagesFilter.setSelection(selectedPosition);
        }
        languagesFilter.setVisibility(View.VISIBLE);
        RelativeLayout.LayoutParams layoutParams =
                (RelativeLayout.LayoutParams) timer.getLayoutParams();

        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
    }

    private void initializeSectionsFilter() {
        if (attempt.hasSectionalLock()) {
            sectionSpinnerAdapter = new LockableSpinnerItemAdapter(getActivity());
            for (AttemptSection section : sections) {
                sectionSpinnerAdapter.addItem(section.getName(), section.getName(), true, 0);
            }
            sectionsFilter.setAdapter(sectionSpinnerAdapter);
            sectionsFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> spinner, View view, int position,
                                           long itemId) {

                    if (!fistTimeCallback) {
                        fistTimeCallback = true;
                        return;
                    }

                    if (position == attempt.getCurrentSectionPosition()) {
                        return;
                    }
                    sectionsFilter.setSelection(attempt.getCurrentSectionPosition());
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),
                            R.style.TestpressAppCompatAlertDialogStyle);

                    if ((courseContent != null && courseContent.getAttemptsCount() <= 1) ||
                            (courseContent == null && (exam.getAttemptsCount() == 0 ||
                                    (exam.getAttemptsCount() == 1 && exam.getPausedAttemptsCount() == 1)))) {

                        builder.setTitle(R.string.testpress_cannot_switch);
                        builder.setMessage(R.string.testpress_cannot_switch_section);
                        builder.setPositiveButton(getString(R.string.testpress_ok), null);
                    } else if (attempt.getCurrentSectionPosition() > position) {
                        builder.setTitle(R.string.testpress_cannot_switch);
                        builder.setMessage(R.string.testpress_already_submitted);
                        builder.setPositiveButton(getString(R.string.testpress_ok), null);
                    } else if (attempt.getCurrentSectionPosition() + 1 < position) {
                        builder.setTitle(R.string.testpress_cannot_switch);
                        builder.setMessage(R.string.testpress_attempt_sections_in_order);
                        builder.setPositiveButton(getString(R.string.testpress_ok), null);
                    } else {
                        builder.setTitle(R.string.testpress_switch_section);
                        builder.setMessage(R.string.testpress_switch_section_message);
                        builder.setPositiveButton(getString(R.string.testpress_end_section),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        endSection();
                                    }
                                });
                    }
                    sectionSwitchAlertDialog = builder.show();
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
            sectionSpinnerAdapter.setSelectedItem(attempt.getCurrentSectionPosition());
            sectionsFilter.setSelection(attempt.getCurrentSectionPosition());
            sectionsFilterContainer.setVisibility(View.VISIBLE);
        } else if (exam.getTemplateType() == 2 || attempt.hasNoSectionalLock()) {
            plainSpinnerAdapter = new PlainSpinnerItemAdapter(getActivity());
            plainSpinnerAdapter.setSectionInfoClickListener(this);
            if (!sections.isEmpty()) {
                showSectionInstructionsButton();
            }
            sectionsFilter.setAdapter(plainSpinnerAdapter);
            sectionsFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> spinner, View view, int position,
                                           long itemId) {

                    showSectionInstructionsButton();
                    if (!fistTimeCallback) {
                        fistTimeCallback = true;
                        return;
                    }
                    String selectedSpinnerItem = plainSpinnerAdapter.getTag(position);
                    selectedPlainSpinnerItemOffset = plainSpinnerItemOffsets.get(selectedSpinnerItem);
                    if (navigationButtonPressed) {
                        // Spinner item changed by clicking next or prev button
                        navigationButtonPressed = false;
                    } else {
                        // Spinner item changed by selecting subject/section in spinner
                        viewPager.setCurrentItem(plainSpinnerItemOffsets.get(selectedSpinnerItem));
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
        }
    }

    private void showSectionInstructionsButton() {
        AttemptSection section = sections.get(attempt.getCurrentSectionPosition());
        if (section.getInstructions() != null) {
            plainSpinnerAdapter.showSectionInfoButton(true);
        }
    }

    private void initializeQuestionsListAdapter() {
        questionsListAdapter = new TestPanelListAdapter(getLayoutInflater(), filterItems,
                R.layout.testpress_test_panel_list_item);
    }

    private void bindViews() {
        View view = getView();
        previous = view.findViewById(R.id.previous);
        next = view.findViewById(R.id.next);
        questionsListView = view.findViewById(R.id.questions_list);
        timer = view.findViewById(R.id.timer);
        ViewUtils.setDrawableColor(timer, R.color.testpress_actionbar_text);
        questionsFilter = view.findViewById(R.id.questions_filter);
        sectionsFilter = view.findViewById(R.id.primary_questions_filter);
        sectionsFilterContainer = view.findViewById(R.id.questions_filter_container);
        viewPager = view.findViewById(R.id.pager);
        viewPager.setSwipeEnabled(true);
        slidingPaneLayout = view.findViewById(R.id.sliding_layout);
        questionsListProgressBar = (View) LayoutInflater.from(getActivity()).inflate(R.layout.progress_bar, null);
        questionsListView.addFooterView(questionsListProgressBar);
        if (instituteSettings.isGrowthHackEnabled()) {
            customiseToolbar();
        }
    }

    private void showLogoInToolbar() {
        ImageView logo = getView().findViewById(R.id.toolbar_logo);
        logo.setVisibility(View.VISIBLE);
        UIUtils.loadLogoInView(logo, getContext());
    }

    private void customiseToolbar() {
        showLogoInToolbar();
        RelativeLayout toolbar = getView().findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.testpress_white));

        // Change exit button color
        ImageButton exitButton = getView().findViewById(R.id.exit_button);
        exitButton.setColorFilter(ContextCompat.getColor(getContext(), R.color.testpress_color_primary));

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) exitButton.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        exitButton.setLayoutParams(params);

        Spinner languagesFilter = getView().findViewById(R.id.language_spinner);
        RelativeLayout.LayoutParams languagesFilterParams = (RelativeLayout.LayoutParams) languagesFilter.getLayoutParams();
        languagesFilterParams.addRule(RelativeLayout.LEFT_OF, R.id.exit_button);
        languagesFilterParams.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        languagesFilter.setLayoutParams(languagesFilterParams);
        languagesFilter.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.testpress_color_primary));

        // Change timer and pause icon color
        timer.setTextColor(ContextCompat.getColor(getContext(), R.color.testpress_color_primary));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            timer.setCompoundDrawableTintList(ContextCompat.getColorStateList(getContext(), R.color.testpress_color_primary));
        }
        ViewUtils.setTextViewDrawableColor(timer, R.color.testpress_color_primary, getContext());
    }


    private void initializeProgressDialog() {
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getResources().getString(R.string.testpress_loading_questions));
        progressDialog.setCancelable(false);
        UIUtils.setIndeterminateDrawable(getActivity(), progressDialog, 4);
    }

    private void initializeListeners() {
        View view = getView();
        slidingPaneLayout.setPanelSlideListener(new SlidingPaneLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(@NonNull View panel, float slideOffset) {
            }

            @Override
            public void onPanelOpened(@NonNull View panel) {
                onExpandPanel();
            }

            @Override
            public void onPanelClosed(@NonNull View panel) {
                viewPager.setSwipeEnabled(true);
                previous.setVisibility(View.VISIBLE);
                next.setVisibility(View.VISIBLE);
            }
        });
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (
                        (viewPagerAdapter.getCount() < totalQuestions)
                        && ((viewPagerAdapter.getCount() - position) <= 4)
                        && !isNextPageQuestionsBeingFetched
                ) {
                    isNextPageQuestionsBeingFetched = true;
                    questionsListProgressBar.setVisibility(View.VISIBLE);
                    getLoaderManager().restartLoader(0, null, TestFragment.this);
                }
                goToQuestion(position, true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        view.findViewById(R.id.question_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openPanel();
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNextQuestion();
            }
        });
        questionsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                int index = ((AttemptItem) questionsListView.getItemAtPosition(position)).getIndex();
                viewPager.setCurrentItem(index - 1);
            }
        });
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPreviousQuestion();
            }
        });
        view.findViewById(R.id.exit_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endExamAlert();
            }
        });
        questionsListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItems, int totalItems) {

                boolean hasAllQuestionsFetched = (totalItems == totalQuestions);
                if (hasAllQuestionsFetched || isNextPageQuestionsBeingFetched) {
                    return;
                }

                if ((totalItems - firstVisibleItem) == visibleItems) {
                    if (attemptItemList.size() < totalQuestions) {
                        isNextPageQuestionsBeingFetched = true;
                        questionsListProgressBar.setVisibility(View.VISIBLE);
                        getLoaderManager().restartLoader(0, null, TestFragment.this);
                    }
                }
            }
        });
        timer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPauseExamAlert();
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        startCountDownTimer();
    }

    private void openPanel() {
        if(slidingPaneLayout.isOpen()) {
            slidingPaneLayout.closePane();
        } else {
            slidingPaneLayout.openPane();
        }
    }

    private void showNextQuestion() {
        if (next.getText().equals(getResources().getString(R.string.testpress_end))) {
            endExamAlert();
        } else if (viewPager.getCurrentItem() < (viewPagerAdapter.getCount() - 1)) {
            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
        }
    }

    private void goToQuestion(int position, boolean saveCurrentOptions) {
        if (attemptItemList.isEmpty()) {
            return;
        }
        if (saveCurrentOptions) {
            saveResult(currentQuestionIndex, Action.UPDATE_ANSWER);
        }
        currentQuestionIndex = position;
        questionsListAdapter.setCurrentAttemptItemIndex(position + 1);
        if (slidingPaneLayout.isOpen() && !isNextPageQuestionsBeingFetched) {
            slidingPaneLayout.closePane();
        }
        if(plainSpinnerAdapter != null && plainSpinnerAdapter.getCount() > 1) {
            String currentSpinnerItem;
            AttemptItem currentAttemptItem = attemptItemList.get(viewPager.getCurrentItem());
            if (attempt.hasNoSectionalLock()) {
                currentSpinnerItem = currentAttemptItem.getAttemptSection().getName();
            } else {
                currentSpinnerItem = currentAttemptItem.getAttemptQuestion().getSubject();
            }
            if (selectedPlainSpinnerItemOffset != plainSpinnerItemOffsets.get(currentSpinnerItem)) {
                //  Navigated to prev subject, so change the spinner item
                navigationButtonPressed = true;
                sectionsFilter
                        .setSelection(plainSpinnerAdapter.getItemPosition(currentSpinnerItem));
            }
        }

        if (position == 0) {
            // Reached first question
            setEnable(false, previous);
        } else {
            setEnable(true, previous);
        }

        updateNextButton(position);
    }

    private void updateNextButton(int position) {
        if ((position + 1) == attemptItemList.size()) {
            // Reached last question
            if (attempt.hasSectionalLock()) {
                setEnable(false, next);
            } else {
                next.setTextColor(ContextCompat.getColor(next.getContext(), R.color.testpress_red));
                next.setText(R.string.testpress_end);
            }
        } else {
            setEnable(true, next);
            next.setText(R.string.testpress_next);
        }
    }

    private void showPreviousQuestion() {
        if (viewPager.getCurrentItem() != 0) {
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        }
    }

    private void endExamAlert() {
        AlertDialog.Builder dialogBuilder =
                new AlertDialog.Builder(getActivity(), R.style.TestpressAppCompatAlertDialogStyle)
                        .setTitle(R.string.testpress_end_title)
                        .setMessage(R.string.testpress_end_message);

        if (attempt.hasNoSectionalLock() || sections.size() < 2) {
            dialogBuilder
                    .setPositiveButton(R.string.testpress_end, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            endExam();
                        }
                    });
        }
        endExamAlertDialog = dialogBuilder
                .setNegativeButton(R.string.testpress_pause, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        pauseExam();
                    }
                })
                .setNeutralButton(R.string.testpress_cancel, null)
                .show();

        endExamAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(getActivity(), R.color.testpress_red_incorrect));
    }

    void showPauseExamAlert() {
        pauseExamAlertDialog =
                new AlertDialog.Builder(getActivity(), R.style.TestpressAppCompatAlertDialogStyle)
                        .setMessage(R.string.testpress_pause_message)
                        .setPositiveButton(R.string.testpress_yes_pause,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        pauseExam();
                                    }
                        })
                        .setNegativeButton(R.string.testpress_cancel, null)
                        .show();
    }

    void pauseExam() {
        stopTimer();
        saveResult(viewPager.getCurrentItem(), Action.PAUSE);
    }

    @NonNull
    @Override
    public Loader<List<AttemptItem>> onCreateLoader(int id, final Bundle args) {
        if (attempt.hasSectionalLock()) {
            progressDialog.setMessage(getString(R.string.testpress_loading_section_questions,
                    sections.get(attempt.getCurrentSectionPosition()).getName()));

            progressDialog.show();
        } else {
            showProgress(R.string.testpress_loading_questions);
        }
        boolean fetchSinglePageOnly = attempt.hasSectionalLock() || (attempt.hasNoSectionalLock() && exam.getTemplateType() != 2);
        return new AttemptItemsLoader(getActivity(), this, fetchSinglePageOnly);
    }

    @Override
    public void onLoadFinished(@NonNull final Loader<List<AttemptItem>> loader,
                               final List<AttemptItem> items) {

        if (getActivity() == null) {
            return;
        }
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        getLoaderManager().destroyLoader(loader.getId());
        //noinspection ThrowableResultOfMethodCallIgnored
        TestpressException exception = ((ThrowableLoader<List<AttemptItem>>) loader).clearException();
        if(exception != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),
                    R.style.TestpressAppCompatAlertDialogStyle);
            builder.setPositiveButton(R.string.testpress_retry_again, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    progressDialog.show();
                    getLoaderManager().restartLoader(loader.getId(), null, TestFragment.this);
                }
            });
            if (exception.isUnauthenticated()) {
                builder.setTitle(R.string.testpress_authentication_failed);
                builder.setMessage(R.string.testpress_please_login);
            } else if (exception.isNetworkError()) {
                builder.setTitle(R.string.testpress_network_error);
                builder.setMessage(R.string.testpress_no_internet_try_again);
            } else {
                builder.setTitle(R.string.testpress_error_loading_questions);
                builder.setMessage(R.string.testpress_some_thing_went_wrong_try_again);
            }
            builder.setNegativeButton(R.string.testpress_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    returnToHistory();
                }
            });
            builder.setCancelable(false);
            networkErrorAlertDialog = builder.show();
            return;
        }

        if (items == null || items.isEmpty()) { // Display alert if no questions exist
            new AlertDialog.Builder(getActivity(), R.style.TestpressAppCompatAlertDialogStyle)
                    .setTitle(R.string.testpress_no_questions)
                    .setMessage(R.string.testpress_no_questions_message)
                    .setCancelable(false)
                    .setNeutralButton(R.string.testpress_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            returnToHistory();
                        }
                    })
                    .show();
            return;
        }

        attemptItemList = items;
        if (attempt.getRemainingTime() == null || attempt.getRemainingTime().equals("0:00:00")) {
            endExam();
            return;
        }
        if (sections.size() <= 1 && exam.getTemplateType() == 2 || attempt.hasNoSectionalLock()) {
            // Used to get items in order as it fetched
            List<String> spinnerItemsList = new ArrayList<>();
            HashMap<String, List<AttemptItem>> groupedAttemptItems = new HashMap<>();
            for (AttemptItem attemptItem : attemptItemList) {
                if (attempt.hasNoSectionalLock()) {
                    String section = attemptItem.getAttemptSection().getName();
                    groupAttemptItems(section, attemptItem, spinnerItemsList, groupedAttemptItems);
                } else {
                    String subject = attemptItem.getAttemptQuestion().getSubject();
                    if (subject == null || subject.isEmpty()) {
                        // If subject is empty, subject = "Uncategorized"
                        attemptItem.getAttemptQuestion()
                                .setSubject(getString(R.string.testpress_uncategorized));
                    }
                    groupAttemptItems(subject, attemptItem, spinnerItemsList, groupedAttemptItems);
                }
            }
            if (spinnerItemsList.size() > 1) {
                // Clear the previous data stored while loading which might be unordered
                attemptItemList.clear();
                // Store each set of items to attemptItemList
                for (String spinnerItem : spinnerItemsList) {
                    // Add spinner item & it starting point
                    plainSpinnerItemOffsets.put(spinnerItem, attemptItemList.size());
                    attemptItemList.addAll(groupedAttemptItems.get(spinnerItem));
                    plainSpinnerAdapter.addItem(spinnerItem, spinnerItem, true, 0);
                }
                plainSpinnerAdapter.notifyDataSetChanged();
                sectionsFilter.setSelection(0); // Set 1st item as default selection
                sectionsFilterContainer.setVisibility(View.VISIBLE);
                selectedPlainSpinnerItemOffset = 0;
            }
        }

        for (int i = 0; i< attemptItemList.size(); i++) {
            attemptItemList.get(i).setIndex(i + 1);
        }
        questionsListAdapter.setItems(attemptItemList);

        int currentQuestion = 0;
        if (isNextPageQuestionsBeingFetched) {
            if (viewPager != null) {
                currentQuestion = viewPager.getCurrentItem();
            }
            updatePanel();
        } else {
            questionsListView.setAdapter(questionsListAdapter);
            startCountDownTimer();
        }
        viewPagerAdapter =
                new TestQuestionPagerAdapter(getFragmentManager(), attemptItemList, selectedLanguage);

        viewPager.setAdapter(viewPagerAdapter);

        if (isNextPageQuestionsBeingFetched || viewPager.getCurrentItem() != 0) {
            viewPager.setCurrentItem(currentQuestion);
        } else {
            goToQuestion(0, false);
        }
        questionsListProgressBar.setVisibility(View.GONE);
        isNextPageQuestionsBeingFetched = false;
    }

    void groupAttemptItems(String spinnerItem, AttemptItem attemptItem, List<String> spinnerItemsList,
                           HashMap<String, List<AttemptItem>> groupedAttemptItems) {

        if (groupedAttemptItems.containsKey(spinnerItem)) {
            // Check spinnerItem is already added if added simply add the item it
            groupedAttemptItems.get(spinnerItem).add(attemptItem);
        } else {
            // Add the spinnerItem & then add item to it
            groupedAttemptItems.put(spinnerItem, new ArrayList<AttemptItem>());
            groupedAttemptItems.get(spinnerItem).add(attemptItem);
            spinnerItemsList.add(spinnerItem);
        }
    }

    @Override
    public void onLoaderReset(@NonNull final Loader<List<AttemptItem>> loader) {
    }

    private  void returnToHistory() {
        if (getActivity() == null) {
            return;
        }
        getActivity().setResult(Activity.RESULT_OK);
        getActivity().finish();
    }

    private void showReview(Intent intent) {
        getActivity().startActivityForResult(intent, CarouselFragment.TEST_TAKEN_REQUEST_CODE);
    }

    private boolean isNonSectionalOrIBPSExam() {
        return exam.getTemplateType() == 2 || attempt.hasNoSectionalLock();
    }

    private void saveResult(final int position, final Action action) {
        if (attemptItemList.size() <= position) {
            return;
        }
        final AttemptItem attemptItem = attemptItemList.get(position);
        final int currentSectionPosition = attempt.getCurrentSectionPosition();

        if (attemptItem.hasChanged()) {
            if (action != Action.UPDATE_ANSWER) {
                showProgress(R.string.testpress_saving_last_change);
            }
            apiClient.postAnswer(attemptItem.getUrlFrag(), attemptItem.getSavedAnswers(),
                    attemptItem.getCurrentShortText(), attemptItem.getCurrentReview())
                    .enqueue(new TestpressCallback<AttemptItem>() {
                        @Override
                        public void onSuccess(AttemptItem newAttemptItem) {
                            if (getActivity() == null) {
                                return;
                            }
                            attemptItem.setSelectedAnswers(newAttemptItem.getSelectedAnswers());
                            attemptItem.setShortText(newAttemptItem.getShortText());
                            attemptItem.setReview(newAttemptItem.getReview());

                            if (isNonSectionalOrIBPSExam() || (attempt.hasSectionalLock() && sections.get(currentSectionPosition).equals("Running"))) {
                                attemptItemList.set(position, attemptItem);
                            }

                            if (action.equals(Action.PAUSE)) {
                                progressDialog.dismiss();
                                returnToHistory();
                            } else if (action.equals(Action.END)) {
                                endExam();
                            } else if (action.equals(Action.END_SECTION)) {
                                endSection();
                            } else {
                                if (progressDialog.isShowing()) {
                                    startCountDownTimer(millisRemaining);
                                    progressDialog.dismiss();
                                }
                                updatePanel();
                            }
                        }

                        @Override
                        public void onException(TestpressException exception) {
                            if (getActivity() == null) {
                                return;
                            }
                            if (action.equals(Action.PAUSE)) {
                                progressDialog.dismiss();
                                returnToHistory();
                                return;
                            }
                            stopTimer();
                            progressDialog.dismiss();
                            TestEngineAlertDialog alertDialog = new TestEngineAlertDialog(exception) {
                                @Override
                                protected void onRetry() {
                                    if (action == Action.UPDATE_ANSWER) {
                                        showProgress(R.string.testpress_saving_last_change);
                                    }
                                    saveResult(position, action);
                                }
                            };
                            saveAnswerAlertDialog = alertDialog.show();
                        }
                    });
        } else if (action.equals(Action.PAUSE)) {
            progressDialog.dismiss();
            returnToHistory();
        }
    }

    void endSection() {
        stopTimer();
        // Save attemptItem, if option or review is changed

        if (!attemptItemList.isEmpty()) {
            final AttemptItem attemptItem = attemptItemList.get(viewPager.getCurrentItem());
            if (attemptItem.hasChanged()) {
                saveResult(viewPager.getCurrentItem(), Action.END_SECTION);
                return;
            }
        }

        showProgress(R.string.testpress_ending_section);
        AttemptSection section = sections.get(attempt.getCurrentSectionPosition());
        if (section.getState().equals(COMPLETED)) {
            onSectionEnded();
            return;
        }
        endSectionApiRequest = apiClient.updateSection(section.getEndUrlFrag())
                .enqueue(new TestpressCallback<AttemptSection>() {
                    @Override
                    public void onSuccess(AttemptSection attemptSection) {
                        if (getActivity() == null) {
                            return;
                        }
                        sections.set(attemptSection.getOrder(), attemptSection);
                        attempt.setSections(sections);
                        onSectionEnded();
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        showException(
                                exception,
                                R.string.testpress_exam_paused_check_internet_to_end,
                                R.string.testpress_end,
                                "endSection"
                        );
                    }
                });
    }

    void onSectionEnded() {
        if (attempt.isAllSectionsCompleted()) {
            endExam();
        } else {
            sectionSpinnerAdapter.setSelectedItem(attempt.getCurrentSectionPosition());
            sectionSpinnerAdapter.notifyDataSetChanged();
            sectionsFilter.setSelection(attempt.getCurrentSectionPosition());
            startSection();
        }
    }

    void startSection() {
        showProgress(R.string.testpress_starting_section);
        String sectionStartUrlFrag = sections.get(attempt.getCurrentSectionPosition()).getStartUrlFrag();
        startSectionApiRequest = apiClient.updateSection(sectionStartUrlFrag)
                .enqueue(new TestpressCallback<AttemptSection>() {
                    @Override
                    public void onSuccess(AttemptSection section) {
                        if (getActivity() == null) {
                            return;
                        }
                        sections.set(section.getOrder(), section);
                        attempt.setSections(sections);
                        questionsResourcePager =
                                new TestQuestionsPager(section.getQuestionsUrlFrag(), apiClient);

                        attemptItemList.clear();
                        getLoaderManager().restartLoader(0, null, TestFragment.this);
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        showException(
                                exception,
                                R.string.testpress_exam_paused_check_internet,
                                R.string.testpress_resume,
                                "startSection"
                        );
                    }
                });
    }

    void endExam() {
        stopTimer();
        // Save attemptItem, if option or review is changed
        if (!attemptItemList.isEmpty() && attemptItemList.get(viewPager.getCurrentItem()).hasChanged()) {
            saveResult(viewPager.getCurrentItem(), Action.END);
            return;
        }
        showProgress(R.string.testpress_ending_exam);
        if (courseContent != null) {
            endContentAttemptApiRequest = apiClient.endContentAttempt(courseAttempt.getEndAttemptUrl())
                    .enqueue(new TestpressCallback<CourseAttempt>() {
                        @Override
                        public void onSuccess(CourseAttempt courseAttempt) {
                            if (getActivity() == null) {
                                return;
                            }
                            if (progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                            courseAttempt.saveInDB(getActivity(), courseContent);
                            showReview(ReviewStatsActivity.createIntent(getActivity(), exam,
                                    courseAttempt));
                        }

                        @Override
                        public void onException(TestpressException exception) {
                            showException(
                                    exception,
                                    R.string.testpress_exam_paused_check_internet_to_end,
                                    R.string.testpress_end,
                                    "endExam"
                            );
                        }
                    });
        } else {
            endAttemptApiRequest = apiClient.endExam(attempt.getEndUrlFrag())
                    .enqueue(new TestpressCallback<Attempt>() {
                        @Override
                        public void onSuccess(Attempt attempt) {
                            if (getActivity() == null) {
                                return;
                            }
                            if (progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                            TestFragment.this.attempt = attempt;
                            showReview(ReviewStatsActivity.createIntent(getActivity(), exam, attempt));
                        }

                        @Override
                        public void onException(TestpressException exception) {
                            showException(
                                    exception,
                                    R.string.testpress_exam_paused_check_internet_to_end,
                                    R.string.testpress_end,
                                    "endExam"
                            );
                        }
                    });
        }
    }

    @SuppressLint("DefaultLocale")
    private static String formatTime(final long millis) {
        return String.format("%02d:%02d:%02d",
                millis / (1000 * 60 * 60),
                (millis / (1000 * 60)) % 60,
                (millis / 1000) % 60
        );
    }

    @SuppressLint("SimpleDateFormat")
    long formatMillisecond(String inputString) {
        if (inputString == null) {
            return 0;
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            return simpleDateFormat.parse(inputString).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private void onExpandPanel() {
        if (!attemptItemList.isEmpty() && attemptItemList.get(viewPager.getCurrentItem()).hasChanged()) {
            saveResult(viewPager.getCurrentItem(), Action.UPDATE_ANSWER);
        }
        viewPager.setSwipeEnabled(false);
        previous.setVisibility(View.INVISIBLE);
        next.setVisibility(View.INVISIBLE);
        updatePanel();
    }

    private void updatePanel() {
        if (questionsFilter.getAdapter() == null) {
            String[] types = {"All", "Answered", "Unanswered", "Marked for review"};
            ExploreSpinnerAdapter typeSpinnerAdapter = new ExploreSpinnerAdapter(
                    getLayoutInflater(), getResources(), false);
            typeSpinnerAdapter.addItem(0, types[0], types[0], true, 0);
            typeSpinnerAdapter.addItem(1, types[1], types[1], true, Color.parseColor("#08AE9E"));
            typeSpinnerAdapter.addItem(2, types[2], types[2], true, Color.parseColor("#808080"));
            typeSpinnerAdapter.addItem(3, types[3], types[3], true, Color.parseColor("#ffa31a"));

            questionsFilter.setAdapter(typeSpinnerAdapter);
            questionsFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                    filterQuestions(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
        } else if (questionsFilter.getSelectedItemPosition() != 0) {
            filterQuestions(questionsFilter.getSelectedItemPosition());
        } else {
            questionsListAdapter.notifyDataSetChanged();
        }
    }

    private void filterQuestions(int type) {
        List<AttemptItem> answeredItems = new ArrayList<>();
        List<AttemptItem> unansweredItems = new ArrayList<>();
        List<AttemptItem> markedItems = new ArrayList<>();
        for(int i = 0; i< attemptItemList.size(); i++) {
            AttemptItem attemptItem = attemptItemList.get(i);
            try {
                if (attemptItem.getReview() || attemptItem.getCurrentReview()) {
                    markedItems.add(attemptItem);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(!attemptItem.getSelectedAnswers().isEmpty() || !attemptItem.getSavedAnswers().isEmpty()
                    || (attemptItem.getShortText() != null && !attemptItem.getShortText().isEmpty())
                    || (attemptItem.getCurrentShortText() != null
                        && !attemptItem.getCurrentShortText().isEmpty())) {

                answeredItems.add(attemptItem);
            } else {
                unansweredItems.add(attemptItem);
            }
        }
        switch (type) {
            case 1:
                filterItems = answeredItems;
                break;
            case 2:
                filterItems = unansweredItems;
                break;
            case 3:
                filterItems = markedItems;
                break;
            default:
                filterItems = attemptItemList;
                break;
        }
        questionsListAdapter.setItems(filterItems.toArray());
    }

    void startCountDownTimer() {
        String remainingTime = attempt.getRemainingTime();
        if (attempt.hasSectionalLock()) {
            AttemptSection section = sections.get(attempt.getCurrentSectionPosition());
            if (section.getState().equals(NOT_STARTED)) {
                startSection();
                return;
            }
            remainingTime = section.getRemainingTime();
        }
        long millisRemainingFetchedInAttempt = formatMillisecond(remainingTime);

        millisRemaining = evaluateRemainingMillisecond(attempt.hasSectionalLock(), millisRemaining, millisRemainingFetchedInAttempt);

        if (millisRemaining == 0) {
            onRemainingTimeOver();
        } else if (attemptItemList.isEmpty()) {
            getLoaderManager().restartLoader(0, null, this);
        } else {
            startCountDownTimer(millisRemaining);
        }
    }

    private void updateTimeRemaining(long millisRemaining) {
        this.millisRemaining = millisRemaining;
        String formattedTime = formatTime(millisRemaining);
        timer.setText(formattedTime);
    }

    public long evaluateRemainingMillisecond(boolean sectionLockedExam, long currentRemainingMillis, long fetchedAttemptRemainingMillis) {

        if (sectionLockedExam) {
            return fetchedAttemptRemainingMillis;
        } else if (currentRemainingMillis == -1 || currentRemainingMillis > fetchedAttemptRemainingMillis) {
            return fetchedAttemptRemainingMillis;
        }
        return currentRemainingMillis;
    }

    void startCountDownTimer(long millisInFuture) {
        updateTimeRemaining(millisInFuture);
        countDownTimer = new CountDownTimer(millisInFuture, 1000) {

            public void onTick(long millisUntilFinished) {
                updateTimeRemaining(millisUntilFinished);
            }

            public void onFinish() {
                if (getActivity() == null) {
                    return;
                }
                if (sectionSwitchAlertDialog != null && sectionSwitchAlertDialog.isShowing()) {
                    sectionSwitchAlertDialog.dismiss();
                } else if (pauseExamAlertDialog != null && pauseExamAlertDialog.isShowing()) {
                    pauseExamAlertDialog.dismiss();
                } else if (endExamAlertDialog != null && endExamAlertDialog.isShowing()) {
                    endExamAlertDialog.dismiss();
                }
                onRemainingTimeOver();
            }
        }.start();
    }

    class TestEngineAlertDialog extends AlertDialog.Builder {

        TestEngineAlertDialog(TestpressException exception) {
            super(getActivity(), R.style.TestpressAppCompatAlertDialogStyle);
            setCancelable(false);
            if (exception.isNetworkError()) {
                setTitle(R.string.testpress_no_internet_connection)
                        .setMessage(R.string.testpress_exam_paused_check_internet)
                        .setPositiveButton(R.string.testpress_resume,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        onRetry();
                                    }
                                })
                        .setNegativeButton(R.string.testpress_not_now,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        returnToHistory();
                                    }
                                });
            } else {
                setTitle(R.string.testpress_loading_failed)
                        .setMessage(R.string.testpress_some_thing_went_wrong_try_again)
                        .setPositiveButton(R.string.testpress_ok,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        returnToHistory();
                                    }
                                });
            }
        }

        protected void onRetry() {}
    }

    private void showProgress(@StringRes int stringResId) {
        progressDialog.setMessage(getString(stringResId));
        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    void setEnable(boolean enable, TextView textView) {
        int colorRes =
                enable ? R.color.testpress_text_gray : R.color.testpress_gray_light;

        textView.setTextColor(ContextCompat.getColor(textView.getContext(), colorRes));
        textView.setClickable(enable);
    }

    void onRemainingTimeOver() {
        if (attempt.hasSectionalLock()) {
            endSection();
        } else {
            endExam();
        }
    }

    void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    void showException(TestpressException exception,
                       @StringRes final int errorMessage,
                       @StringRes int positiveButtonText,
                       final String methodName) {

        if (getActivity() == null) {
            return;
        }
        TestEngineAlertDialog alertDialogBuilder = new TestEngineAlertDialog(exception);
        if (exception.isNetworkError()) {
            alertDialogBuilder.setMessage(errorMessage);
            alertDialogBuilder.setPositiveButton(positiveButtonText,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            switch (methodName) {
                                case "endSection":
                                    endSection();
                                    break;
                                case "startSection":
                                    startSection();
                                    break;
                                case "endExam":
                                    endExam();
                                    break;
                            }
                        }
                    });
        }
        networkErrorAlertDialog = alertDialogBuilder.show();
    }

    void showResumeExamDialog() {
        if (getActivity() == null) {
            return;
        }
        resumeExamDialog =
                new AlertDialog.Builder(getActivity(), R.style.TestpressAppCompatAlertDialogStyle)
                        .setCancelable(false)
                        .setMessage(R.string.testpress_exam_paused)
                        .setPositiveButton(R.string.testpress_resume,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        resumeExam();
                                    }
                                })
                        .setNegativeButton(R.string.testpress_not_now,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        returnToHistory();
                                    }
                                })
                        .show();
    }

    void resumeExam() {
        showProgress(R.string.testpress_please_wait);
        resumeExamApiRequest = apiClient.startAttempt(attempt.getStartUrlFrag())
                .enqueue(new TestpressCallback<Attempt>() {
                    @Override
                    public void onSuccess(Attempt attempt) {
                        TestFragment.this.attempt = attempt;
                        sections = attempt.getSections();
                        progressDialog.dismiss();
                        startCountDownTimer();
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        if (getActivity() == null) {
                            return;
                        }
                        progressDialog.dismiss();
                        TestEngineAlertDialog alertDialogBuilder = new TestEngineAlertDialog(exception) {
                            @Override
                            protected void onRetry() {
                                showProgress(R.string.testpress_please_wait);
                                resumeExam();
                            }
                        };
                        networkErrorAlertDialog = alertDialogBuilder.show();
                    }
                });
    }

    void stopTimerOnAppWentBackground() {
        CommonUtils.cancelAPIRequests(getRetrofitCalls());
        if (countDownTimer != null) {
            stopTimer();
        }
        CommonUtils.dismissDialogs(new Dialog[] {
                heartBeatAlertDialog, saveAnswerAlertDialog, networkErrorAlertDialog,
                sectionInfoAlertDialog
        });
        showResumeExamDialog();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        attempt.setSections(sections);
        outState.putParcelable(PARAM_ATTEMPT, attempt);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        removeAppBackgroundHandler();
    }

    void removeAppBackgroundHandler() {
        if (appBackgroundStateHandler != null) {
            appBackgroundStateHandler.removeCallbacks(stopTimerTask);
            appBackgroundStateHandler = null;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        saveResult(currentQuestionIndex, Action.UPDATE_ANSWER);
        appBackgroundStateHandler = new Handler();
        appBackgroundStateHandler.postDelayed(stopTimerTask, APP_BACKGROUND_DELAY);
    }

    @Override
    public RetrofitCall[] getRetrofitCalls() {
        return new RetrofitCall[] {
                heartBeatApiRequest, startSectionApiRequest, endSectionApiRequest,
                endContentAttemptApiRequest, endAttemptApiRequest, resumeExamApiRequest
        };
    }

    @Override
    public Dialog[] getDialogs() {
        return new Dialog[] {
                progressDialog, resumeExamDialog, heartBeatAlertDialog, saveAnswerAlertDialog,
                networkErrorAlertDialog
        };
    }

    @Override
    public void onDestroy() {
        stopTimer();
        removeAppBackgroundHandler();
        super.onDestroy();
    }


    @Override
    public void showInfo() {
        AttemptSection section = sections.get(attempt.getCurrentSectionPosition());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),
                R.style.TestpressAppCompatAlertDialogStyle);
        builder.setTitle("Instructions");
        builder.setMessage(Html.fromHtml(section.getInstructions()));
        builder.show();
    }
}
