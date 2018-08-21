package in.testpress.exam.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SlidingPaneLayout;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import in.testpress.exam.R;
import in.testpress.exam.models.AttemptItem;
import in.testpress.exam.network.TestQuestionsPager;
import in.testpress.exam.network.TestpressExamApiClient;
import in.testpress.exam.ui.view.NonSwipeableViewPager;
import in.testpress.models.greendao.Attempt;
import in.testpress.models.greendao.AttemptSection;
import in.testpress.models.greendao.Content;
import in.testpress.models.greendao.CourseAttempt;
import in.testpress.models.greendao.Exam;
import in.testpress.models.greendao.Language;
import in.testpress.ui.ExploreSpinnerAdapter;
import in.testpress.util.ThrowableLoader;
import in.testpress.util.UIUtils;
import in.testpress.util.ViewUtils;

import static in.testpress.exam.ui.TestActivity.PARAM_COURSE_ATTEMPT;
import static in.testpress.exam.ui.TestActivity.PARAM_COURSE_CONTENT;
import static in.testpress.models.greendao.Attempt.RUNNING;

public class TestFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<AttemptItem>> {

    static final String PARAM_EXAM = "exam";
    static final String PARAM_ATTEMPT = "attempt";
    SlidingPaneLayout slidingPaneLayout;
    private TestpressExamApiClient apiClient;
    private TextView previous;
    private TextView next;
    private ListView questionsListView;
    private TextView timer;
    private Spinner panelQuestionsFilter;
    private Spinner primaryQuestionsFilter;
    private RelativeLayout questionFilterContainer;
    private NonSwipeableViewPager pager;
    private TestQuestionPagerAdapter pagerAdapter;
    private List<AttemptItem> filterItems = new ArrayList<>();
    private TestPanelListAdapter panelListAdapter;
    private ProgressDialog progressDialog;
    private AlertDialog endExamAlertDialog;
    private AlertDialog pauseExamAlertDialog;
    private AlertDialog sectionSwitchAlertDialog;
    private Attempt attempt;
    private Exam exam;
    private Content courseContent;
    private CourseAttempt courseAttempt;
    private int currentPosition;
    private int currentSection;
    private boolean unlockedSections;
    private List<AttemptSection> sections = new ArrayList<>();
    private TestQuestionsPager questionsPager;
    private List<AttemptItem> attemptItemList = new ArrayList<>();
    private CountDownTimer countDownTimer;
    private long millisRemaining;
    private LockableSpinnerItemAdapter sectionSpinnerAdapter;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        courseContent = getArguments().getParcelable(PARAM_COURSE_CONTENT);
        if (courseContent != null) {
            courseAttempt = getArguments().getParcelable(PARAM_COURSE_ATTEMPT);
            exam = courseContent.getRawExam();
            attempt = courseAttempt.getRawAssessment();
        } else {
            attempt = getArguments().getParcelable(PARAM_ATTEMPT);
            exam = getArguments().getParcelable(PARAM_EXAM);
        }
        String questionUrl = attempt.getQuestionsUrlFrag();
        sections = attempt.getSections();
        if (sections.size() > 1) {
            for (int i = 0; i < sections.size(); i++) {
                if (sections.get(i).getState().equals(RUNNING)) {
                    currentSection = i;
                }
                if (sections.get(i).getDuration() == null ||
                        sections.get(i).getDuration().equals("0:00:00")) {

                    unlockedSections = true;
                }
            }
            if (!unlockedSections) {
                questionUrl = sections.get(currentSection).getQuestionsUrlFrag();
            }
        }
        apiClient = new TestpressExamApiClient(getActivity());
        questionsPager = new TestQuestionsPager(questionUrl, apiClient);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.testpress_fragment_test_engine, container, false);
        previous = view.findViewById(R.id.previous);
        next = view.findViewById(R.id.next);
        questionsListView = view.findViewById(R.id.questions_list);
        timer = view.findViewById(R.id.timer);
        panelQuestionsFilter = view.findViewById(R.id.questions_filter);
        primaryQuestionsFilter = view.findViewById(R.id.primary_questions_filter);
        questionFilterContainer = view.findViewById(R.id.questions_filter_container);
        pager = view.findViewById(R.id.pager);
        slidingPaneLayout = view.findViewById(R.id.sliding_layout);
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getResources().getString(R.string.testpress_loading_questions));
        progressDialog.setCancelable(false);
        UIUtils.setIndeterminateDrawable(getActivity(), progressDialog, 4);
        previous.setVisibility(View.VISIBLE);
        next.setVisibility(View.VISIBLE);
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
                pager.setSwipeEnabled(true);
                previous.setVisibility(View.VISIBLE);
                next.setVisibility(View.VISIBLE);
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
                pager.setCurrentItem(index - 1);
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
        ViewUtils.setDrawableColor(timer, R.color.testpress_actionbar_text);
        timer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPauseExamAlert();
            }
        });
        panelListAdapter = new TestPanelListAdapter(getLayoutInflater(), filterItems,
                R.layout.testpress_test_panel_list_item);

        if (sections.size() > 1 && !unlockedSections) {
            sectionSpinnerAdapter = new LockableSpinnerItemAdapter(getActivity());
            for (AttemptSection section : sections) {
                sectionSpinnerAdapter.addItem(section.getName(), section.getName(), true, 0);
            }
            primaryQuestionsFilter.setAdapter(sectionSpinnerAdapter);
            primaryQuestionsFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> spinner, View view, int position,
                                           long itemId) {

                    if (!fistTimeCallback) {
                        fistTimeCallback = true;
                        return;
                    }

                    if (position == currentSection) {
                        return;
                    }
                    primaryQuestionsFilter.setSelection(currentSection);
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),
                            R.style.TestpressAppCompatAlertDialogStyle);

                    if ((courseContent != null && courseContent.getAttemptsCount() <= 1) ||
                            (courseContent == null && (exam.getAttemptsCount() == 0 ||
                                    (exam.getAttemptsCount() == 1 && exam.getPausedAttemptsCount() == 1)))) {

                        builder.setTitle(R.string.testpress_cannot_switch);
                        builder.setMessage(R.string.testpress_cannot_switch_section);
                        builder.setPositiveButton(getString(R.string.testpress_ok), null);
                    } else if (currentSection > position) {
                        builder.setTitle(R.string.testpress_cannot_switch);
                        builder.setMessage(R.string.testpress_already_submitted);
                        builder.setPositiveButton(getString(R.string.testpress_ok), null);
                    } else if (currentSection + 1 < position) {
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
            sectionSpinnerAdapter.setSelectedItem(currentSection);
            primaryQuestionsFilter.setSelection(currentSection);
            questionFilterContainer.setVisibility(View.VISIBLE);
        } else if (exam.getTemplateType() == 2 || unlockedSections) {
            plainSpinnerAdapter = new PlainSpinnerItemAdapter(getActivity());
            primaryQuestionsFilter.setAdapter(plainSpinnerAdapter);
            primaryQuestionsFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> spinner, View view, int position,
                                           long itemId) {

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
                        pager.setCurrentItem(plainSpinnerItemOffsets.get(selectedSpinnerItem));
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
        }
        Spinner languageSpinner = view.findViewById(R.id.language_spinner);
        final ArrayList<Language> languages = new ArrayList<>(exam.getLanguages());
        if (languages.size() > 1) {
            ExploreSpinnerAdapter languageSpinnerAdapter =
                    new ExploreSpinnerAdapter(getLayoutInflater(), getResources(), false);

            for (Language language : languages) {
                languageSpinnerAdapter.addItem(language.getCode(), language.getTitle(), true, 0);
            }
            languageSpinnerAdapter.hideSpinner(true);
            languageSpinner.setAdapter(languageSpinnerAdapter);
            languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    // Update existing object so that update will reflect in TestQuestionFragment also
                    selectedLanguage.update(languages.get(position));
                    exam.setSelectedLanguage(selectedLanguage.getCode());
                    if (pagerAdapter != null) {
                        pagerAdapter.notifyDataSetChanged();

                    }
                    panelListAdapter.notifyDataSetChanged();
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
                panelListAdapter.setSelectedLanguage(selectedLanguage);
                languageSpinner.setSelection(selectedPosition);
            }
            languageSpinner.setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams layoutParams =
                    (RelativeLayout.LayoutParams) timer.getLayoutParams();

            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
        } else {
            languageSpinner.setVisibility(View.GONE);
            RelativeLayout.LayoutParams layoutParams =
                    (RelativeLayout.LayoutParams) timer.getLayoutParams();

            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, 0);
        }
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (attemptItemList.isEmpty()) {
            getLoaderManager().initLoader(0, null, this);
        }
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
        } else if (pager.getCurrentItem() < (pagerAdapter.getCount() - 1)) {
            pager.setCurrentItem(pager.getCurrentItem() + 1);
        }
    }

    private void goToQuestion(int position) {
        if (attemptItemList.isEmpty()) {
            return;
        }
        saveResult(currentPosition, Action.UPDATE_ANSWER);
        currentPosition = position;
        panelListAdapter.setCurrentAttemptItemIndex(position + 1);
        if (slidingPaneLayout.isOpen()) {
            slidingPaneLayout.closePane();
        }
        if(plainSpinnerAdapter != null && plainSpinnerAdapter.getCount() > 1) {
            String currentSpinnerItem;
            AttemptItem currentAttemptItem = attemptItemList.get(pager.getCurrentItem());
            if (unlockedSections) {
                currentSpinnerItem = currentAttemptItem.getAttemptSection().getName();
            } else {
                currentSpinnerItem = currentAttemptItem.getAttemptQuestion().getSubject();
            }
            if (selectedPlainSpinnerItemOffset != plainSpinnerItemOffsets.get(currentSpinnerItem)) {
                //  Navigated to prev subject, so change the spinner item
                navigationButtonPressed = true;
                primaryQuestionsFilter
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
            if (sections.size() > 1) {
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
        if (pager.getCurrentItem() != 0) {
            pager.setCurrentItem(pager.getCurrentItem() - 1);
        }
    }

    private void endExamAlert() {
        AlertDialog.Builder dialogBuilder =
                new AlertDialog.Builder(getActivity(), R.style.TestpressAppCompatAlertDialogStyle)
                        .setTitle(R.string.testpress_end_title)
                        .setMessage(R.string.testpress_end_message);

        if (sections.size() <= 1) {
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
        countDownTimer.cancel();
        saveResult(pager.getCurrentItem(), Action.PAUSE);
    }

    @NonNull
    @Override
    public Loader<List<AttemptItem>> onCreateLoader(int id, final Bundle args) {
        if (sections.size() > 1) {
            progressDialog.setMessage(getString(R.string.testpress_loading_section_questions,
                    sections.get(currentSection).getName()));

            progressDialog.show();
        } else {
            showProgress(R.string.testpress_loading_questions);
        }
        return new AttemptItemsLoader(getActivity(), this);
    }

    private static class AttemptItemsLoader extends ThrowableLoader<List<AttemptItem>> {

        private TestFragment fragment;

        AttemptItemsLoader(Context context, TestFragment fragment) {
            super(context, null);
            this.fragment = fragment;
        }

        @Override
        public List<AttemptItem> loadData() throws TestpressException {
            do {
                fragment.questionsPager.next();
            } while (fragment.questionsPager.hasNext());
            return fragment.questionsPager.getResources();
        }
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
            builder.show();
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
        if (sections.size() <= 1 && exam.getTemplateType() == 2 || unlockedSections) {
            // Used to get items in order as it fetched
            List<String> spinnerItemsList = new ArrayList<>();
            HashMap<String, List<AttemptItem>> groupedAttemptItems = new HashMap<>();
            for (AttemptItem attemptItem : items) {
                if (unlockedSections) {
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
                primaryQuestionsFilter.setSelection(0); // Set 1st item as default selection
                questionFilterContainer.setVisibility(View.VISIBLE);
                selectedPlainSpinnerItemOffset = 0;
            }
        }

        pagerAdapter =
                new TestQuestionPagerAdapter(getFragmentManager(), attemptItemList, selectedLanguage);

        pager.setAdapter(pagerAdapter);
        for (int i = 0; i< attemptItemList.size(); i++) {
            attemptItemList.get(i).setIndex(i + 1);
        }
        panelListAdapter.setItems(attemptItemList);
        questionsListView.setAdapter(panelListAdapter);
        if (pager.getCurrentItem() != 0) {
            pager.setCurrentItem(0);
        } else {
            goToQuestion(0);
        }
        String remainingTime = attempt.getRemainingTime();
        if (sections.size() > 1 && !unlockedSections) {
            remainingTime = sections.get(currentSection).getRemainingTime();
        }
        startCountDownTimer(formatMillisecond(remainingTime));
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

    private void saveResult(final int position, final Action action) {
        final AttemptItem attemptItem = attemptItemList.get(position);
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
                            attemptItemList.set(position, attemptItem);
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
                            countDownTimer.cancel();
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
                            alertDialog.show();
                        }
                    });
        } else if (action.equals(Action.PAUSE)) {
            progressDialog.dismiss();
            returnToHistory();
        }
    }

    private void sendHeartBeat() {
        apiClient.heartbeat(attempt.getHeartBeatUrlFrag()).enqueue(new TestpressCallback<Attempt>() {
            @Override
            public void onSuccess(Attempt result) {
                if (getActivity() == null) {
                    return;
                }
                if (progressDialog.isShowing()) {
                    startCountDownTimer(millisRemaining);
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onException(TestpressException exception) {
                if (getActivity() == null) {
                    return;
                }
                countDownTimer.cancel();
                TestEngineAlertDialog alertDialog = new TestEngineAlertDialog(exception) {
                    @Override
                    protected void onRetry() {
                        showProgress(R.string.testpress_please_wait);
                        sendHeartBeat();
                    }
                };
                alertDialog.show();
            }
        });
    }

    private void endSection() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        // Save attemptItem, if option or review is changed
        final AttemptItem attemptItem = attemptItemList.get(pager.getCurrentItem());
        if (attemptItem.hasChanged()) {
            saveResult(pager.getCurrentItem(), Action.END_SECTION);
            return;
        }
        showProgress(R.string.testpress_ending_section);
        apiClient.updateSection(sections.get(currentSection).getEndUrlFrag())
                .enqueue(new TestpressCallback<AttemptSection>() {
                    @Override
                    public void onSuccess(AttemptSection attemptSection) {
                        if (getActivity() == null) {
                            return;
                        }
                        sections.set(currentSection, attemptSection);
                        if (++currentSection == sections.size()) {
                            endExam();
                        } else {
                            sectionSpinnerAdapter.setSelectedItem(currentSection);
                            sectionSpinnerAdapter.notifyDataSetChanged();
                            primaryQuestionsFilter.setSelection(currentSection);
                            startSection();
                        }
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        if (getActivity() == null) {
                            return;
                        }
                        TestEngineAlertDialog alertDialog = new TestEngineAlertDialog(exception);
                        if (exception.isNetworkError()) {
                            alertDialog.setMessage(R.string.testpress_exam_paused_check_internet_to_end);
                            alertDialog.setPositiveButton(R.string.testpress_end,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            endSection();
                                        }
                                    });
                        }
                        alertDialog.show();
                    }
                });
    }

    private void startSection() {
        showProgress(R.string.testpress_starting_section);
        apiClient.updateSection(sections.get(currentSection).getStartUrlFrag())
                .enqueue(new TestpressCallback<AttemptSection>() {
                    @Override
                    public void onSuccess(AttemptSection section) {
                        if (getActivity() == null) {
                            return;
                        }
                        sections.set(currentSection, section);
                        questionsPager =
                                new TestQuestionsPager(section.getQuestionsUrlFrag(), apiClient);

                        attemptItemList.clear();
                        getLoaderManager().restartLoader(0, null, TestFragment.this);
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        if (getActivity() == null) {
                            return;
                        }
                        TestEngineAlertDialog alertDialog = new TestEngineAlertDialog(exception);
                        if (exception.isNetworkError()) {
                            alertDialog.setMessage(R.string.testpress_exam_paused_check_internet);
                            alertDialog.setPositiveButton(R.string.testpress_resume,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            startSection();
                                        }
                                    });
                        }
                        alertDialog.show();
                    }
                });
    }

    private void endExam() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        // Save attemptItem, if option or review is changed
        final AttemptItem attemptItem = attemptItemList.get(pager.getCurrentItem());
        if (attemptItem.hasChanged()) {
            saveResult(pager.getCurrentItem(), Action.END);
            return;
        }
        showProgress(R.string.testpress_ending_exam);
        if (courseContent != null) {
            apiClient.endContentAttempt(courseAttempt.getEndAttemptUrl())
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
                            if (getActivity() == null) {
                                return;
                            }
                            TestEngineAlertDialog alertDialog = new TestEngineAlertDialog(exception);
                            if (exception.isNetworkError()) {
                                alertDialog.setMessage(R.string.testpress_exam_paused_check_internet_to_end);
                                alertDialog.setPositiveButton(R.string.testpress_end,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                endExam();
                                            }
                                        });
                            }
                            alertDialog.show();
                        }
                    });
        } else {
            apiClient.endExam(attempt.getEndUrlFrag())
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
                            if (getActivity() == null) {
                                return;
                            }
                            TestEngineAlertDialog alertDialog = new TestEngineAlertDialog(exception);
                            if (exception.isNetworkError()) {
                                alertDialog.setMessage(R.string.testpress_exam_paused_check_internet_to_end);
                                alertDialog.setPositiveButton(R.string.testpress_end,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                endExam();
                                            }
                                        });
                            }
                            alertDialog.show();
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
    private long formatMillisecond(String inputString) {
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
        if (!attemptItemList.isEmpty() && attemptItemList.get(pager.getCurrentItem()).hasChanged()) {
            saveResult(pager.getCurrentItem(), Action.UPDATE_ANSWER);
        }
        pager.setSwipeEnabled(false);
        previous.setVisibility(View.INVISIBLE);
        next.setVisibility(View.INVISIBLE);
        updatePanel();
    }

    private void updatePanel() {
        if (panelQuestionsFilter.getAdapter() == null) {
            String[] types = {"All", "Answered", "Unanswered", "Marked for review"};
            ExploreSpinnerAdapter typeSpinnerAdapter = new ExploreSpinnerAdapter(
                    getLayoutInflater(), getResources(), false);

            typeSpinnerAdapter.addItems(Arrays.asList(types));
            panelQuestionsFilter.setAdapter(typeSpinnerAdapter);
            panelQuestionsFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                    filterQuestions(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
        } else if (panelQuestionsFilter.getSelectedItemPosition() != 0) {
            filterQuestions(panelQuestionsFilter.getSelectedItemPosition());
        } else {
            panelListAdapter.notifyDataSetChanged();
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
        panelListAdapter.setItems(filterItems.toArray());
    }

    private void startCountDownTimer(long millisInFuture) {
        countDownTimer = new CountDownTimer(millisInFuture, 1000) {

            public void onTick(long millisUntilFinished) {
                millisRemaining = millisUntilFinished;
                final String formattedTime = formatTime(millisUntilFinished);
                timer.setText(formattedTime);
                if(((millisUntilFinished / 1000) % 60) == 0) {
                    sendHeartBeat();
                }
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
                if (sections.size() > 1) {
                    endSection();
                } else {
                    endExam();
                }
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
}
