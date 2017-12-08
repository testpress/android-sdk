package in.testpress.exam.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
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
import android.widget.Spinner;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.TimeZone;

import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.exam.R;
import in.testpress.models.greendao.Attempt;
import in.testpress.exam.models.AttemptItem;
import in.testpress.models.greendao.AttemptDao;
import in.testpress.models.greendao.CourseAttempt;
import in.testpress.models.greendao.CourseAttemptDao;
import in.testpress.models.greendao.Exam;
import in.testpress.models.greendao.ExamDao;
import in.testpress.models.greendao.Language;
import in.testpress.exam.network.TestQuestionsPager;
import in.testpress.exam.network.TestpressExamApiClient;
import in.testpress.exam.ui.view.NonSwipeableViewPager;
import in.testpress.core.TestpressSDKDatabase;
import in.testpress.ui.ExploreSpinnerAdapter;
import in.testpress.util.ThrowableLoader;
import in.testpress.util.UIUtils;

public class TestFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<AttemptItem>> {

    static final String PARAM_EXAM = "exam";
    static final String PARAM_ATTEMPT = "attempt";
    static final String PARAM_CONTENT_ATTEMPT_END_URL = "contentAttemptEndUrl";
    SlidingPaneLayout slidingPaneLayout;
    private TestpressExamApiClient apiClient;
    private TextView previous;
    private TextView next;
    private ListView questionsListView;
    private TextView timer;
    private Spinner questionsFilter;
    private Spinner subjectFilter;
    private Spinner languageSpinner;
    private NonSwipeableViewPager pager;
    private TestQuestionPagerAdapter pagerAdapter;
    private List<AttemptItem> filterItems = new ArrayList<>();
    private TestPanelListAdapter panelListAdapter;
    private String contentAttemptEndUrl;
    private ProgressDialog progressDialog;
    private Attempt attempt;
    private Exam exam;
    private int currentPosition;
    private TestQuestionsPager questionsPager;
    private List<AttemptItem> attemptItemList = new ArrayList<AttemptItem>();
    private CountDownTimer countDownTimer;
    private long millisRemaining;
    private ExploreSpinnerAdapter subjectSpinnerAdapter;
    private ExploreSpinnerAdapter languageSpinnerAdapter;
    private Language selectedLanguage;
    private Boolean fistTimeCallback = false;
    private int selectedSubjectOffset;
    private boolean navigationButtonPressed;
    private List<Language> languages = new List<Language>() {
        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean contains(Object o) {
            return false;
        }

        @NonNull
        @Override
        public Iterator<Language> iterator() {
            return null;
        }

        @NonNull
        @Override
        public Object[] toArray() {
            return new Object[0];
        }

        @NonNull
        @Override
        public <T> T[] toArray(@NonNull T[] a) {
            return null;
        }

        @Override
        public boolean add(Language language) {
            return false;
        }

        @Override
        public boolean remove(Object o) {
            return false;
        }

        @Override
        public boolean containsAll(@NonNull Collection<?> c) {
            return false;
        }

        @Override
        public boolean addAll(@NonNull Collection<? extends Language> c) {
            return false;
        }

        @Override
        public boolean addAll(int index, @NonNull Collection<? extends Language> c) {
            return false;
        }

        @Override
        public boolean removeAll(@NonNull Collection<?> c) {
            return false;
        }

        @Override
        public boolean retainAll(@NonNull Collection<?> c) {
            return false;
        }

        @Override
        public void clear() {

        }

        @Override
        public Language get(int index) {
            return null;
        }

        @Override
        public Language set(int index, Language element) {
            return null;
        }

        @Override
        public void add(int index, Language element) {

        }

        @Override
        public Language remove(int index) {
            return null;
        }

        @Override
        public int indexOf(Object o) {
            return 0;
        }

        @Override
        public int lastIndexOf(Object o) {
            return 0;
        }

        @NonNull
        @Override
        public ListIterator<Language> listIterator() {
            return null;
        }

        @NonNull
        @Override
        public ListIterator<Language> listIterator(int index) {
            return null;
        }

        @NonNull
        @Override
        public List<Language> subList(int fromIndex, int toIndex) {
            return null;
        }
    };
    private Activity activity;
    /*
     * Map of subjects & its starting point(first question index)
     */
    private HashMap<String, Integer> subjectsOffset = new HashMap<>();
    private enum Action { PAUSE, END, UPDATE_ANSWER }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        attempt = getArguments().getParcelable(PARAM_ATTEMPT);
        exam = TestpressSDKDatabase.getExamDao(getContext()).queryBuilder().where(ExamDao.Properties.Id.eq(getArguments().getLong(PARAM_EXAM))).list().get(0);
        contentAttemptEndUrl = getArguments().getString(PARAM_CONTENT_ATTEMPT_END_URL);
        questionsPager = new TestQuestionsPager(attempt.getQuestionsUrlFrag(),
                new TestpressExamApiClient(getActivity()));
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.testpress_fragment_test_engine,
                container, false);
        previous = (TextView) view.findViewById(R.id.previous);
        next = (TextView) view.findViewById(R.id.next);
        questionsListView = (ListView) view.findViewById(R.id.questions_list);
        timer = (TextView) view.findViewById(R.id.timer);
        questionsFilter = (Spinner) view.findViewById(R.id.questions_filter);
        subjectFilter = (Spinner) view.findViewById(R.id.subject_filter);
        languageSpinner = (Spinner) view.findViewById(R.id.language_spinner);
        pager = (NonSwipeableViewPager) view.findViewById(R.id.pager);
        slidingPaneLayout = (SlidingPaneLayout) view.findViewById(R.id.sliding_layout);
        apiClient = new TestpressExamApiClient(getActivity());
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getResources().getString(R.string.testpress_loading_questions));
        progressDialog.setCancelable(false);
        UIUtils.setIndeterminateDrawable(getActivity(), progressDialog, 4);
        progressDialog.show();
        previous.setVisibility(View.VISIBLE);
        next.setVisibility(View.VISIBLE);
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
        view.findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNextQuestion();
            }
        });
        ((ListView) view.findViewById(R.id.questions_list)).setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position,
                                            long id) {

                        int index = ((AttemptItem) questionsListView.getItemAtPosition(position))
                                .getIndex();

                        pager.setCurrentItem(index - 1);
                    }
        });
        view.findViewById(R.id.previous).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPreviousQuestion();
            }
        });
        view.findViewById(R.id.end).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endExamAlert();
            }
        });
        view.findViewById(R.id.pause_exam).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pauseExam();
            }
        });
        panelListAdapter = new TestPanelListAdapter(getActivity().getLayoutInflater(), filterItems,
                R.layout.testpress_test_panel_list_item);

        subjectSpinnerAdapter = new ExploreSpinnerAdapter(getActivity().getLayoutInflater(),
                getActivity().getResources(), false);
        subjectFilter.setAdapter(subjectSpinnerAdapter);
        subjectFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> spinner, View view, int position, long itemId) {
                if (!fistTimeCallback) {
                    fistTimeCallback = true;
                    return;
                }
                String subject = subjectSpinnerAdapter.getTag(position);
                selectedSubjectOffset = subjectsOffset.get(subject);
                if (navigationButtonPressed) {
                    // Spinner item changed by clicking next or prev button
                    navigationButtonPressed = false;
                } else {
                    // Spinner item changed by selecting subject in spinner
                    pager.setCurrentItem(subjectsOffset.get(subject));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        return view;
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
        if(subjectSpinnerAdapter.getCount() > 1) {
            String currentSubject = attemptItemList.get(pager.getCurrentItem()).getAttemptQuestion()
                    .getSubject();
            if (selectedSubjectOffset != subjectsOffset.get(currentSubject)) {
                //  Navigated to prev subject, so change the spinner item
                navigationButtonPressed = true;
                subjectFilter.setSelection(subjectSpinnerAdapter.getItemPosition(currentSubject));
            }
        }

        if (position == 0) {
            // Reached first question
            previous.setClickable(false);
            previous.setTextColor(ContextCompat.getColor(getActivity(), R.color.testpress_nav_button_disabled));
        } else {
            previous.setClickable(true);
            previous.setTextColor(ContextCompat.getColor(getActivity(), R.color.testpress_color_primary));
        }

        updateNextButton(position);
    }

    private void updateNextButton(int position) {
        if ((position + 1) == attemptItemList.size()) {
            // Reached last question
            next.setTextColor(ContextCompat.getColor(getActivity(), R.color.testpress_red));
            next.setText(R.string.testpress_end);
        } else {
            next.setTextColor(ContextCompat.getColor(getActivity(), R.color.testpress_color_primary));
            next.setText(R.string.testpress_next);
        }
    }

    private void showPreviousQuestion() {
        if (pager.getCurrentItem() != 0) {
            pager.setCurrentItem(pager.getCurrentItem() - 1);
        }
    }

    private void endExamAlert() {
        new AlertDialog.Builder(getActivity(), R.style.TestpressAppCompatAlertDialogStyle)
                .setTitle(R.string.testpress_end_message)
                .setPositiveButton(R.string.testpress_end, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        endExam();
                    }
                })
                .setNegativeButton(R.string.testpress_cancel, null)
                .show();
    }

    void pauseExam() {
        new AlertDialog.Builder(getActivity(), R.style.TestpressAppCompatAlertDialogStyle)
                .setTitle(R.string.testpress_pause_message)
                .setMessage(R.string.testpress_pause_content)
                .setPositiveButton(R.string.testpress_pause, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        countDownTimer.cancel();
                        saveResult(pager.getCurrentItem(), Action.PAUSE);
                    }
                })
                .setNegativeButton(R.string.testpress_cancel, null)
                .show();
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public Loader<List<AttemptItem>> onCreateLoader(int id, final Bundle args) {
        return new ThrowableLoader<List<AttemptItem>>(getActivity(), attemptItemList) {
            @Override
            public List<AttemptItem> loadData() throws TestpressException {
                do {
                    questionsPager.next();
                    attemptItemList = questionsPager.getResources();
                } while (questionsPager.hasNext());
                return attemptItemList;
            }
        };
    }

    @Override
    public void onLoadFinished(final Loader<List<AttemptItem>> loader, final List<AttemptItem> items) {
        //TODO : exam is null
//        loadExamLanguage(exam.getSlug());

        if (getActivity() == null) {
            return;
        }
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
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

        if (attempt.getRemainingTime() == null || attempt.getRemainingTime().equals("00:00:00")) {
            endExam();
            return;
        }
        if (items.isEmpty()) { // Display alert if no questions exist
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
        /**
         * Used to get subjects in order as it fetched
         */
        List<String> subjectsList = new ArrayList<>();
        /*
         * To Populate the spinner with the subjects
         */
        HashMap<String, List<AttemptItem>> subjectsWiseItems = new HashMap<>();
        for (AttemptItem item : items) {
            if (item.getAttemptQuestion().getSubject() == null || item.getAttemptQuestion()
                    .getSubject().isEmpty()) {
                // If subject is empty, subject = "Uncategorized"
                item.getAttemptQuestion().setSubject(getResources()
                        .getString(R.string.testpress_uncategorized));
            }
            if (subjectsWiseItems.containsKey(item.getAttemptQuestion().getSubject())) {
                // Check subject is already added if added simply add the item it
                subjectsWiseItems.get(item.getAttemptQuestion().getSubject()).add(item);
            } else {
                // Add the subject & then add item to it
                subjectsWiseItems.put(item.getAttemptQuestion().getSubject(),
                        new ArrayList<AttemptItem>());
                subjectsWiseItems.get(item.getAttemptQuestion().getSubject()).add(item);
                subjectsList.add(item.getAttemptQuestion().getSubject());
            }
        }
        if (subjectsList.size() > 1) {
            // Clear the previous data stored while loading which might be unordered
            attemptItemList.clear();
            // Store each set of subject items to attemptItemList
            for (String subject : subjectsList) {
                subjectsOffset.put(subject, attemptItemList.size()); // Add subjects & it starting point
                attemptItemList.addAll(subjectsWiseItems.get(subject));
                subjectSpinnerAdapter.addItem(subject, subject, true, 0);
            }
            subjectSpinnerAdapter.notifyDataSetChanged();
            subjectFilter.setSelection(0); // Set 1st item as default selection
            subjectFilter.setVisibility(View.VISIBLE);
            selectedSubjectOffset = 0;
        }

        if (languages.size() > 1) {
            languageSpinnerAdapter = new ExploreSpinnerAdapter(getActivity().getLayoutInflater(),
                    getResources(), false);

            for (Language language : languages) {
                languageSpinnerAdapter.addItem(language.getCode(), language.getTitle(), true, 0);
            }
            languageSpinner.setAdapter(languageSpinnerAdapter);
            languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    // Update existing object so that update will reflect in TestQuestionFragment also
                    selectedLanguage.update(languages.get(position));
                    exam.setSelectedLanguage(selectedLanguage.getCode());
                    pagerAdapter.notifyDataSetChanged();
                    panelListAdapter.notifyDataSetChanged();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
            String selectedLanguageCode = exam.getSelectedLanguage();
            if(selectedLanguageCode.isEmpty() || selectedLanguageCode == null) {
                selectedLanguageCode = "en";
                exam.setSelectedLanguage("en");
            }
            if (selectedLanguageCode != null && !selectedLanguageCode.isEmpty()) {
                int selectedPosition =
                        languageSpinnerAdapter.getItemPositionFromTag(selectedLanguageCode);

                // Create new object so that we can update it without affecting original languageList list
                //TODO : fetch languageList from DB
                selectedLanguage = new Language(languages.get(selectedPosition));
                panelListAdapter.setSelectedLanguage(selectedLanguage);
                languageSpinner.setSelection(selectedPosition);
            }
            languageSpinner.setVisibility(View.VISIBLE);
        } else {
            selectedLanguage = new Language("en", "English", exam.getSlug());
        }

        pagerAdapter =
                new TestQuestionPagerAdapter(getFragmentManager(), attemptItemList, selectedLanguage);

        pagerAdapter.setCount(attemptItemList.size());
        pager.setAdapter(pagerAdapter);
        pagerAdapter.notifyDataSetChanged();
        for (int i = 0; i< attemptItemList.size(); i++) {
            attemptItemList.get(i).setIndex(i + 1);
        }
        panelListAdapter.setItems(attemptItemList);
        questionsListView.setAdapter(panelListAdapter);
        updateNextButton(pager.getCurrentItem());
        startCountDownTimer(formatMillisecond(attempt.getRemainingTime()));
    }

    @Override
    public void onLoaderReset(final Loader<List<AttemptItem>> loader) {
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
            apiClient.postAnswer(attemptItem.getUrlFrag(), attemptItem.getSavedAnswers(),
                    attemptItem.getCurrentReview())
                    .enqueue(new TestpressCallback<AttemptItem>() {
                        @Override
                        public void onSuccess(AttemptItem newAttemptItem) {
                            if (getActivity() == null) {
                            }
                            attemptItem.setSelectedAnswers(newAttemptItem.getSelectedAnswers());
                            attemptItem.setReview(newAttemptItem.getReview());
                            attemptItemList.set(position, attemptItem);
                            if (action.equals(Action.PAUSE)) {
                                progressDialog.dismiss();
                                returnToHistory();
                            } else if (action.equals(Action.END)) {
                                endExam();
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
                            TestEngineAlertDialog alertDialog = new TestEngineAlertDialog(exception) {
                                @Override
                                protected void onRetry() {
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
                        sendHeartBeat();
                    }
                };
                alertDialog.show();
            }
        });
    }

    private void endExam() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        showProgress();
        // Save attemptItem, if option or review is changed
        final AttemptItem attemptItem = attemptItemList.get(pager.getCurrentItem());
        if (attemptItem.hasChanged()) {
           saveResult(pager.getCurrentItem(), Action.END);
            return;
        }
        if (contentAttemptEndUrl != null) {
            apiClient.endContentAttempt(contentAttemptEndUrl)
                    .enqueue(new TestpressCallback<CourseAttempt>() {
                        @Override
                        public void onSuccess(CourseAttempt courseAttempt) {
                            if (getActivity() == null) {
                            }
                            if (progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                            saveCourseAttemptInDB(courseAttempt);
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
                                                showProgress();
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
                                                progressDialog.setMessage(getString(
                                                        R.string.testpress_loading));
                                                progressDialog.show();
                                                endExam();
                                            }
                                        });
                            }
                            alertDialog.show();
                        }
                    });
        }
    }

    private void saveCourseAttemptInDB(CourseAttempt courseAttempt) {
        CourseAttemptDao courseAttemptDao = TestpressSDKDatabase.getCourseAttemptDao(getActivity());
        AttemptDao attemptDao = TestpressSDKDatabase.getAttemptDao(getActivity());
        Attempt attempt = courseAttempt.assessment;
        attemptDao.insertOrReplace(attempt);
        courseAttempt.setAttemptId(attempt.getId());
        courseAttemptDao.insertOrReplace(courseAttempt);
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
        Date date = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            date = simpleDateFormat.parse(inputString);
        } catch (ParseException e) {
        }
        return date.getTime();
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
        if (questionsFilter.getAdapter() == null) {
            String[] types = {"All", "Answered", "Unanswered", "Marked for review"};
            ExploreSpinnerAdapter typeSpinnerAdapter = new ExploreSpinnerAdapter(getActivity()
                    .getLayoutInflater(), getActivity().getResources(), false);
            typeSpinnerAdapter.addItems(Arrays.asList(types));
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
            panelListAdapter.notifyDataSetChanged();
        }
    }

    private void filterQuestions(int type) {
        List<AttemptItem> answeredItems = new ArrayList<>();
        List<AttemptItem> unansweredItems = new ArrayList<>();
        List<AttemptItem> markedItems = new ArrayList<>();
        for(int i = 0; i< attemptItemList.size(); i++) {
            try {
                if (attemptItemList.get(i).getReview() || attemptItemList.get(i).getCurrentReview()) {
                    markedItems.add(attemptItemList.get(i));
                }
            } catch (Exception e) {
            }
            if(!attemptItemList.get(i).getSelectedAnswers().isEmpty() ||
                    !attemptItemList.get(i).getSavedAnswers().isEmpty()) {
                answeredItems.add(attemptItemList.get(i));
            } else {
                unansweredItems.add(attemptItemList.get(i));
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
                endExam();
            }
        }.start();
    }

    class TestEngineAlertDialog extends AlertDialog.Builder {

        public TestEngineAlertDialog(TestpressException exception) {
            super(getActivity(), R.style.TestpressAppCompatAlertDialogStyle);
            setCancelable(false);
            if (exception.isNetworkError()) {
                setTitle(R.string.testpress_no_internet_connection)
                        .setMessage(R.string.testpress_exam_paused_check_internet)
                        .setPositiveButton(R.string.testpress_resume,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        progressDialog.setMessage(getString(
                                                R.string.testpress_loading));
                                        progressDialog.show();
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

    private void showProgress() {
        if (!progressDialog.isShowing()) {
            progressDialog.setMessage(getString(R.string.testpress_please_wait));
            progressDialog.show();
        }
    }
}
