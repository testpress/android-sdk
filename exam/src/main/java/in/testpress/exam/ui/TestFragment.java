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
import androidx.lifecycle.Observer;
import androidx.core.content.ContextCompat;
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
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressError;
import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSdk;
import in.testpress.exam.R;
import in.testpress.exam.models.AttemptItem;
import in.testpress.exam.network.NetworkAttempt;
import in.testpress.exam.network.NetworkAttemptKt;
import in.testpress.exam.network.NetworkAttemptSection;
import in.testpress.exam.network.NetworkAttemptSectionKt;
import in.testpress.exam.api.TestpressExamApiClient;
import in.testpress.exam.ui.view.NonSwipeableViewPager;
import in.testpress.exam.ui.viewmodel.AttemptViewModel;
import in.testpress.models.InstituteSettings;
import in.testpress.models.greendao.Attempt;
import in.testpress.models.greendao.AttemptSection;
import in.testpress.models.greendao.Content;
import in.testpress.models.greendao.CourseAttempt;
import in.testpress.models.greendao.Exam;
import in.testpress.models.greendao.Language;
import in.testpress.network.Resource;
import in.testpress.network.RetrofitCall;
import in.testpress.ui.BaseFragment;
import in.testpress.ui.ExploreSpinnerAdapter;
import in.testpress.util.CommonUtils;
import in.testpress.util.EventsTrackerFacade;
import in.testpress.util.UIUtils;
import in.testpress.util.ViewUtils;
import kotlin.Pair;
import kotlin.Triple;

import static in.testpress.exam.ui.TestActivity.PARAM_COURSE_ATTEMPT;
import static in.testpress.exam.ui.TestActivity.PARAM_COURSE_CONTENT;
import static in.testpress.models.greendao.Attempt.COMPLETED;
import static in.testpress.models.greendao.Attempt.NOT_STARTED;

public class TestFragment extends BaseFragment implements
        PlainSpinnerItemAdapter.SectionInfoClickListener, TestPanelListAdapter.ListItemClickListener {

    private static final int APP_BACKGROUND_DELAY = 60000; // 1m
    public static final String DEFAULT_EXAM_TIME = "24:00:00";
    public static final String INFINITE_EXAM_TIME = "0:00:00";

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

    public static enum Action { PAUSE, END, UPDATE_ANSWER, END_SECTION, START_SECTION }
    private RetrofitCall<Attempt> heartBeatApiRequest;
    private RetrofitCall<NetworkAttemptSection> endSectionApiRequest;
    private RetrofitCall<NetworkAttemptSection> startSectionApiRequest;
    private RetrofitCall<NetworkAttempt> resumeExamApiRequest;
    private Handler appBackgroundStateHandler;
    private Runnable stopTimerTask = new Runnable() {
        @Override
        public void run() {
            stopTimerOnAppWentBackground();
        }
    };
    private InstituteSettings instituteSettings;
    private EventsTrackerFacade eventsTrackerFacade;
    private AttemptViewModel attemptViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        attemptViewModel = AttemptViewModel.Companion.initializeViewModel(requireActivity());
        initializeAttemptAndExamVariables(savedInstanceState);
        instituteSettings = TestpressSdk.getTestpressSession(getContext()).getInstituteSettings();
        eventsTrackerFacade = new EventsTrackerFacade(getContext());
        logEvent(EventsTrackerFacade.STARTED_EXAM);
        apiClient = new TestpressExamApiClient(getActivity());
    }

    private void logEvent(String name) {
        if (exam != null){
            HashMap<String, Object> params = new HashMap<>();
            params.put("exam_name", exam.getTitle());
            params.put("id", exam.getId());
            eventsTrackerFacade.logEvent(name, params);
        }
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
        attemptViewModel.setExamAndAttempt(exam, attempt);
        if (savedInstanceState != null && savedInstanceState.getParcelable(PARAM_ATTEMPT) != null) {
            attempt = savedInstanceState.getParcelable(PARAM_ATTEMPT);
        }
        sections = attempt.getSections();
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

        if (exam != null && (attempt.hasSections() || exam.getTemplateType() == 2)) {
            initializeSectionsFilter();
        }

        if (exam != null && exam.hasMultipleLanguages()) {
            initializeLanguageFilter();
        }
        if (exam == null && attempt.getRemainingTime().equals(DEFAULT_EXAM_TIME)) {
            view.findViewById(R.id.timer).setVisibility(View.GONE);
        }
        observeAttemptItemResources();
        observeSaveAnswerResource();
        observeUpdateSectionResource();
        observeEndContentAttemptResources();
        observeEndAttemptResources();
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

                    if (courseContent != null && exam != null && !exam.isPreemptiveSectionEndingEnabled()) {
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
        if (sections.isEmpty()) {
            return;
        }
        AttemptSection section = sections.get(attempt.getCurrentSectionPosition());
        if (section.getInstructions() != null) {
            plainSpinnerAdapter.showSectionInfoButton(true);
        }
    }

    private void initializeQuestionsListAdapter() {
        questionsListAdapter = new TestPanelListAdapter(filterItems,
                R.layout.testpress_test_panel_list_item, this.requireActivity(), this);
    }

    @Override
    public void onItemClicked(int position) {
        viewPager.setCurrentItem(position);
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
                attemptViewModel.setCurrentQuestionPosition(position);
                if (
                        (viewPagerAdapter.getCount() < attemptViewModel.getTotalQuestions())
                                && ((viewPagerAdapter.getCount() - position) <= 4)
                                && !attemptViewModel.isNextPageQuestionsBeingFetched()
                ) {
                    attemptViewModel.setNextPageQuestionsBeingFetched(true);
                    questionsListProgressBar.setVisibility(View.VISIBLE);
                    fetchAttemptItems();
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

                boolean hasAllQuestionsFetched = (totalItems == attemptViewModel.getTotalQuestions());
                if (hasAllQuestionsFetched || attemptViewModel.isNextPageQuestionsBeingFetched()) {
                    return;
                }

                if ((totalItems - firstVisibleItem) == visibleItems) {
                    if (attemptItemList.size() < attemptViewModel.getTotalQuestions()) {
                        attemptViewModel.setNextPageQuestionsBeingFetched(true);
                        questionsListProgressBar.setVisibility(View.VISIBLE);
                        fetchAttemptItems();
                    }
                }
            }
        });
        timer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (exam == null){
                    endExamAlert();
                } else {
                    showPauseExamAlert();
                }
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
        if (slidingPaneLayout.isOpen() && !attemptViewModel.isNextPageQuestionsBeingFetched()) {
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
        if (exam == null || exam.isAttemptResumeDisabled()){
            showEndExamAlert();
        } else {
            AlertDialog.Builder dialogBuilder =
                    new AlertDialog.Builder(getActivity(), R.style.TestpressAppCompatAlertDialogStyle)
                            .setTitle(R.string.testpress_end_title)
                            .setMessage(R.string.testpress_end_message);

            if (exam.isPreemptiveSectionEndingEnabled() ||(attempt.hasNoSectionalLock() || sections.size() < 2)) {
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
    }

    void showPauseExamAlert() {
        if (exam.isAttemptResumeDisabled()) {
            showEndExamAlert();
            return;
        }
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

    public void showEndExamAlert() {
        pauseExamAlertDialog =
                new AlertDialog.Builder(getActivity(), R.style.TestpressAppCompatAlertDialogStyle)
                        .setMessage("Are you sure? Want to end the exam")
                        .setPositiveButton("Yes, End!",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        endExam();
                                    }
                                })
                        .setNegativeButton(R.string.testpress_cancel, null)
                        .show();
    }

    void pauseExam() {
        stopTimer();
        saveResult(viewPager.getCurrentItem(), Action.PAUSE);
    }

    private void observeAttemptItemResources(){
        attemptViewModel.getAttemptItemsResource().observe(requireActivity(), new Observer<Resource<List<AttemptItem>>>() {
            @Override
            public void onChanged(Resource<List<AttemptItem>> listResource) {
                switch (listResource.getStatus()){
                    case SUCCESS:{
                        if (getActivity() == null) {
                            return;
                        }
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }

                        if (listResource.getData() == null || listResource.getData().isEmpty()) { // Display alert if no questions exist
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

                        attemptItemList = listResource.getData();
                        if (attempt.getRemainingTime() == null || attempt.getRemainingTime().equals("0:00:00")) {
                            endExam();
                            return;
                        }
                        if (exam != null){
                            initializeSectionSpinner();
                        }

                        for (int i = 0; i< attemptItemList.size(); i++) {
                            attemptItemList.get(i).setIndex(i + 1);
                        }
                        questionsListAdapter.setItems(attemptItemList);

                        int currentQuestion = 0;
                        if (attemptViewModel.isNextPageQuestionsBeingFetched()) {
                            if (viewPager != null) {
                                currentQuestion = viewPager.getCurrentItem();
                            }
                        } else {
                            questionsListView.setAdapter(questionsListAdapter);
                            startCountDownTimer();
                        }
                        viewPagerAdapter =
                                new TestQuestionPagerAdapter(getFragmentManager(), attemptItemList, selectedLanguage, exam);

                        viewPager.setAdapter(viewPagerAdapter);

                        if (attemptViewModel.isNextPageQuestionsBeingFetched() || viewPager.getCurrentItem() != 0) {
                            viewPager.setCurrentItem(currentQuestion);
                        } else {
                            goToQuestion(0, false);
                        }

                        if (attempt.getLastViewedQuestionId() != null){
                            Integer position = getLastViewedQuestionIndex();
                            viewPager.setCurrentItem(position);
                        }

                        questionsListProgressBar.setVisibility(View.GONE);
                        attemptViewModel.setNextPageQuestionsBeingFetched(false);
                        break;
                    }
                    case LOADING:{
                        progressDialog.show();
                        break;
                    }
                    case ERROR:{
                        if (getActivity() == null) {
                            return;
                        }
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }

                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),
                                R.style.TestpressAppCompatAlertDialogStyle);
                        builder.setPositiveButton(R.string.testpress_retry_again, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                progressDialog.show();
                                fetchAttemptItems();
                            }
                        });
                        if (listResource.getException().isUnauthenticated()) {
                            builder.setTitle(R.string.testpress_authentication_failed);
                            builder.setMessage(R.string.testpress_please_login);
                        } else if (listResource.getException().isNetworkError()) {
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
                        break;
                    }
                }
            }
        });
    }

    private void fetchAttemptItems(){
        String questionUrl = "";
        if (!isOfflineExam()){
            questionUrl =  attempt.getQuestionsUrlFrag();
            if (attempt.hasSectionalLock()) {
                questionUrl = sections.get(attempt.getCurrentSectionPosition()).getQuestionsUrlFrag();
            }
            questionUrl = questionUrl.replace("v2.3", "v2.2.1");
        }

        if (attempt.hasSectionalLock()) {
            progressDialog.setMessage(getString(R.string.testpress_loading_section_questions,
                    sections.get(attempt.getCurrentSectionPosition()).getName()));

            progressDialog.show();
        } else {
            showProgress(R.string.testpress_loading_questions);
        }
        boolean fetchSinglePageOnly = attempt.hasSectionalLock();

        attemptViewModel.fetchAttemptItems(questionUrl, fetchSinglePageOnly);
    }

    private void initializeSectionSpinner() {
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
    }

    private Integer getLastViewedQuestionIndex(){
        for (AttemptItem attemptItem: attemptItemList) {
            if (attemptItem.getId().equals(attempt.getLastViewedQuestionId())){
                return attemptItemList.indexOf(attemptItem);
            }
        }
        return 0;
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
        return exam != null && (exam.getTemplateType() == 2 || attempt.hasNoSectionalLock());
    }

    private void saveResult(final int position, final Action action) {
        if (attemptItemList.size() <= position) {
            return;
        }
        final AttemptItem attemptItem = attemptItemList.get(position);
        if (attemptItem.hasChanged()) {
            if (action != Action.UPDATE_ANSWER) {
                showProgress(R.string.testpress_saving_last_change);
            }
            attemptViewModel.saveAnswer(position,attemptItem,action, formatTime(millisRemaining));
        } else if (action.equals(Action.PAUSE)) {
            if (isOfflineExam()){
                attemptViewModel.saveAnswer(position,attemptItem,action, formatTime(millisRemaining));
            }
            progressDialog.dismiss();
            returnToHistory();
        }
    }

    private void observeSaveAnswerResource() {
        attemptViewModel.getSaveResultResource().observe(requireActivity(), new Observer<Resource<Triple<Integer, AttemptItem, Action>>>() {
            @Override
            public void onChanged(Resource<Triple<Integer, AttemptItem, Action>> hashMapResource) {
                if (hashMapResource == null || getActivity() == null) {
                    return;
                }

                int position = hashMapResource.getData().getFirst();
                AttemptItem newAttemptItem = hashMapResource.getData().getSecond();
                Action action = hashMapResource.getData().getThird();
                TestpressException exception = hashMapResource.getException();

                switch (hashMapResource.getStatus()) {
                    case SUCCESS: {
                        handleSuccess(position, newAttemptItem, action);
                        break;
                    }
                    case LOADING: {
                        break;
                    }
                    case ERROR: {
                        handleError(exception, position, action);
                        break;
                    }
                }
            }
        });
    }

    private void handleSuccess(int position, AttemptItem newAttemptItem, Action action) {
        AttemptItem attemptItem = attemptItemList.get(position);
        updateAttemptItem(attemptItem, newAttemptItem);

        if (isNonSectionalOrIBPSExam() || (attempt.hasSectionalLock() && sections.get(attempt.getCurrentSectionPosition()).equals("Running"))) {
            attemptItemList.set(position, attemptItem);
        }

        if (action.equals(Action.PAUSE)) {
            progressDialog.dismiss();
            returnToHistory();
        } else if (action.equals(Action.END)) {
            endExam();
        } else if (action.equals(Action.END_SECTION)) {
            endSection();
        } else if(action.equals(Action.UPDATE_ANSWER)) {
            if (progressDialog.isShowing()) {
                startCountDownTimer(millisRemaining);
                progressDialog.dismiss();
            }
        }
    }

    private void updateAttemptItem(AttemptItem attemptItem, AttemptItem newAttemptItem) {
        attemptItem.setSelectedAnswers(newAttemptItem.getSelectedAnswers());
        attemptItem.setShortText(newAttemptItem.getShortText());
        attemptItem.setReview(newAttemptItem.getReview());
        attemptItem.setFiles(newAttemptItem.getFiles());
        attemptItem.setEssayText(newAttemptItem.getEssayText());
    }

    private void handleError(TestpressException exception, int position, Action action) {
        if (action.equals(Action.PAUSE)) {
            progressDialog.dismiss();
            returnToHistory();
            return;
        }

        TestpressError errorDetails = exception.getErrorBodyAs(exception.getResponse(), TestpressError.class);

        if (exception.isForbidden() && isMaxQuestionsAttemptedError(errorDetails)) {
            clearAndLoadSameQuestion(position);
            saveAnswerAlertDialog = showMaxQuestionsAttemptedError(errorDetails);
            progressDialog.dismiss();
        } else {
            stopTimer();
            progressDialog.dismiss();
            showAlertDialog(exception, position, action);
        }
    }

    private void showAlertDialog(TestpressException exception, int position, Action action) {
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

    private boolean isMaxQuestionsAttemptedError(TestpressError errorDetails) {
        return errorDetails.getDetail() != null && Objects.equals(errorDetails.getDetail().getErrorCode(), "max_attemptable_questions_limit_reached");
    }

    private void clearAndLoadSameQuestion(int position) {
        final AttemptItem attemptItem = attemptItemList.get(position);
        attemptItem.setSelectedAnswers(new ArrayList());
        attemptItem.saveAnswers(new ArrayList());
        attemptItem.setShortText(null);
        viewPager.setCurrentItem(position);
    }

    private AlertDialog showMaxQuestionsAttemptedError(TestpressError errorDetails) {
        String errorMessage = "You have attempted maximum number of questions. Please unanswer any question to attempt this question";
        if (errorDetails.getDetail().getMessage() != null) {
            errorMessage = errorDetails.getDetail().getMessage();
        }
        return new AlertDialog.Builder(getContext(), R.style.TestpressAppCompatAlertDialogStyle)
                .setTitle("Maximum questions attempted")
                .setMessage(errorMessage)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        viewPagerAdapter.notifyDataSetChanged();
                        dialogInterface.dismiss();
                    }
                })
                .show();
    }

    void observeUpdateSectionResource() {
        attemptViewModel.getUpdateSectionResource().observe(requireActivity(), new Observer<Resource<Pair<NetworkAttemptSection, Action>>>() {
            @Override
            public void onChanged(Resource<Pair<NetworkAttemptSection, Action>> pairResource) {
                if (pairResource == null || getActivity() == null) {
                    return;
                }

                switch (pairResource.getStatus()) {
                    case SUCCESS:
                        handleUpdateSectionSuccess(pairResource.getData().getFirst(), pairResource.getData().getSecond());
                        break;

                    case LOADING:
                        handleUpdateSectionLoading(pairResource.getData().getSecond());
                        break;

                    case ERROR:
                        handleUpdateSectionError(pairResource.getException(), pairResource.getData().getSecond());
                        break;
                }
            }
        });
    }

    private void handleUpdateSectionSuccess(NetworkAttemptSection networkAttemptSection, Action action) {
        AttemptSection greenDaoAttemptSection = NetworkAttemptSectionKt.createAttemptSection(networkAttemptSection);
        sections.set(greenDaoAttemptSection.getOrder(), greenDaoAttemptSection);
        attempt.setSections(sections);

        if (action == Action.END_SECTION) {
            attemptViewModel.resetPageCount();
            onSectionEnded();
        } else {
            String questionUrl = (isOfflineExam()) ? "" : greenDaoAttemptSection.getQuestionsUrlFrag().replace("2.3", "2.2");
            attemptViewModel.clearAttemptItem();
            attemptViewModel.fetchAttemptItems(questionUrl, true);
        }
    }

    private void handleUpdateSectionLoading(Action action) {
        if (action == Action.END_SECTION) {
            showProgress(R.string.testpress_ending_section);
        } else {
            showProgress(R.string.testpress_starting_section);
        }
    }

    private void handleUpdateSectionError(TestpressException exception, Action action) {
        if (action == Action.END_SECTION) {
            showException(
                    exception,
                    R.string.testpress_exam_paused_check_internet_to_end,
                    R.string.testpress_end,
                    "endSection"
            );
        } else {
            showException(
                    exception,
                    R.string.testpress_exam_paused_check_internet,
                    R.string.testpress_resume,
                    "startSection"
            );
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

        AttemptSection section = sections.get(attempt.getCurrentSectionPosition());
        if (section.getState().equals(COMPLETED)) {
            onSectionEnded();
            return;
        }
        String sectionEndUrlFrag = (isOfflineExam()) ? "" : section.getEndUrlFrag();
        attemptViewModel.updateSection(sectionEndUrlFrag,Action.END_SECTION);
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
        String sectionStartUrlFrag = (isOfflineExam()) ? "" : sections.get(attempt.getCurrentSectionPosition()).getStartUrlFrag();
        attemptViewModel.updateSection(sectionStartUrlFrag,Action.START_SECTION);
    }

    private void observeEndContentAttemptResources(){
        attemptViewModel.getEndContentAttemptResource().observe(requireActivity(), new Observer<Resource<CourseAttempt>>() {
            @Override
            public void onChanged(Resource<CourseAttempt> courseAttemptResource) {
                switch (courseAttemptResource.getStatus()){
                    case SUCCESS:{
                        courseAttempt = courseAttemptResource.getData();
                        if (getActivity() == null) {
                            return;
                        }
                        logEvent(EventsTrackerFacade.ENDED_EXAM);
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        if (isOfflineExam()){
                            returnToHistory();
                            return;
                        }
                        courseAttempt.saveInDB(getActivity(), courseContent);
                        showReview(ReviewStatsActivity.createIntent(getActivity(), exam,
                                courseAttempt));
                        break;
                    }
                    case LOADING:{
                        showProgress(R.string.testpress_ending_exam);
                        break;
                    }
                    case ERROR:{
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        showException(
                                courseAttemptResource.getException(),
                                R.string.testpress_exam_paused_check_internet_to_end,
                                R.string.testpress_end,
                                "endExam"
                        );
                        break;
                    }
                }
            }
        });
    }

    private void observeEndAttemptResources() {
        attemptViewModel.getEndAttemptResource().observe(requireActivity(), new Observer<Resource<Attempt>>() {
            @Override
            public void onChanged(Resource<Attempt> attemptResource) {
                switch (attemptResource.getStatus()){
                    case SUCCESS:{
                        attempt = attemptResource.getData();
                        if (getActivity() == null) {
                            return;
                        }
                        logEvent(EventsTrackerFacade.ENDED_EXAM);
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        if (isOfflineExam()){
                            returnToHistory();
                            return;
                        }
                        showReview(attempt);
                        break;
                    }
                    case LOADING:{
                        showProgress(R.string.testpress_ending_exam);
                        break;
                    }
                    case ERROR:{
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        showException(
                                attemptResource.getException(),
                                R.string.testpress_exam_paused_check_internet_to_end,
                                R.string.testpress_end,
                                "endExam"
                        );
                        break;
                    }
                }
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
            attemptViewModel.endContentAttempt(courseAttempt.getEndAttemptUrl());
        } else {
            attemptViewModel.endAttempt(attempt.getEndUrlFrag());
        }
    }

    private void showReview(Attempt attempt) {
        if (exam != null) {
            showReview(ReviewStatsActivity.createIntent(getActivity(), exam, attempt));
        } else {
            showReview(ReviewStatsActivity.createIntent(getActivity(), attempt));
            requireActivity().finish();
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
            fetchAttemptItems();
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

        if (countDownTimer != null){
            countDownTimer.cancel();
            countDownTimer = null;
        }
        countDownTimer = new CountDownTimer(millisInFuture, 1000) {

            public void onTick(long millisUntilFinished) {
                showExamEndingMessage(millisUntilFinished);
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

    void showExamEndingMessage(long millisUntilFinished) {
        final long WARNING_TIME_MS = 30000; // 30 seconds
        final long WARNING_RANGE_MS = 1000; // 1 second

        if (isOfflineExam() && instituteSettings.getShowOfflineExamEndingAlert() &&
                millisUntilFinished > WARNING_TIME_MS - WARNING_RANGE_MS && millisUntilFinished < WARNING_TIME_MS + WARNING_RANGE_MS) {
            Toast.makeText(requireContext(), "Please connect to the internet. The exam will end in 30 seconds.", Toast.LENGTH_SHORT).show();
        }
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
                        .setMessage(getResources().getString(R.string.testpress_some_thing_went_wrong_try_again)+" "+"(Error code: "+exception.getResponse().code()+")")
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
                .enqueue(new TestpressCallback<NetworkAttempt>() {
                    @Override
                    public void onSuccess(NetworkAttempt networkAttempt) {
                        TestFragment.this.attempt = NetworkAttemptKt.asGreenDaoModel(networkAttempt);
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
                resumeExamApiRequest
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
        AttemptSection section = sections.get(sectionsFilter.getSelectedItemPosition());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),
                R.style.TestpressAppCompatAlertDialogStyle);
        builder.setTitle("Instructions");
        builder.setMessage(Html.fromHtml(section.getInstructions()));
        builder.show();
    }

    private boolean isOfflineExam() {
        return exam != null && Boolean.TRUE.equals(exam.getIsOfflineExam());
    }
}
