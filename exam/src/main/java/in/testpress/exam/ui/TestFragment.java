package in.testpress.exam.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.os.OperationCanceledException;
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

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import in.testpress.exam.R;
import in.testpress.exam.models.Attempt;
import in.testpress.exam.models.AttemptItem;
import in.testpress.exam.models.Exam;
import in.testpress.exam.models.TestpressApiResponse;
import in.testpress.exam.network.TestpressExamApiClient;
import in.testpress.exam.util.ThrowableLoader;
import in.testpress.util.SafeAsyncTask;

public class TestFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<AttemptItem>> {

    static final String PARAM_EXAM = "exam";
    static final String PARAM_ATTEMPT = "attempt";
    SlidingPaneLayout slidingPaneLayout;
    private TestpressExamApiClient apiClient;
    private TextView previous;
    private TextView next;
    private ListView questionsListView;
    private TextView timer;
    private Spinner questionsFilter;
    private Spinner subjectFilter;
    private ViewPager pager;
    private RelativeLayout spinnerContainer;
    private TestQuestionPagerAdapter pagerAdapter;
    private List<AttemptItem> filterItems = new ArrayList<>();
    private TestPanelListAdapter panelListAdapter;
    private ProgressDialog progressDialog;
    private Attempt attempt;
    private Exam exam;
    private List<AttemptItem> attemptItemList = new ArrayList<AttemptItem>();
    private CountDownTimer countDownTimer;
    private long millisRemaining;
    private ExploreSpinnerAdapter subjectSpinnerAdapter;
    private Boolean fistTimeCallback = false;
    private int selectedSubjectOffset;
    private boolean navigationButtonPressed;
    /*
     * Map of subjects & its starting point(first question index)
     */
    private HashMap<String, Integer> subjectsOffset = new HashMap<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        attempt = getArguments().getParcelable(PARAM_ATTEMPT);
        exam = getArguments().getParcelable(PARAM_EXAM);
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
        pager = (ViewPager) view.findViewById(R.id.pager);
        slidingPaneLayout = (SlidingPaneLayout) view.findViewById(R.id.sliding_layout);
        spinnerContainer = (RelativeLayout) view.findViewById(R.id.spinner_container);
        apiClient = new TestpressExamApiClient();
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle(R.string.testpress_loading);
        progressDialog.setMessage(getResources().getString(R.string.testpress_please_wait));
        progressDialog.setCancelable(false);
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
                previous.setVisibility(View.VISIBLE);
                next.setVisibility(View.VISIBLE);
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
                        goToQuestion(((AttemptItem) questionsListView.getItemAtPosition(position))
                                .getIndex() - 1);
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
                    goToQuestion(subjectsOffset.get(subject));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        spinnerContainer.setVisibility(View.GONE);
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
            if (attemptItemList.isEmpty()) {
                return;
            }
            if (attemptItemList.get(pager.getCurrentItem()).hasChanged()) {
                saveResult(pager.getCurrentItem());
            }
            endExamAlert();
        } else if (pager.getCurrentItem() < (pagerAdapter.getCount() - 1)) {
            goToQuestion(pager.getCurrentItem() + 1);
        }
    }

    private void goToQuestion(int position) {
        if (attemptItemList.isEmpty()) {
            return;
        }

        if (attemptItemList.get(pager.getCurrentItem()).hasChanged()) {
            saveResult(pager.getCurrentItem());
        }
        pager.setCurrentItem(position);
        panelListAdapter.setCurrentAttemptItemIndex(position + 1);
        if (slidingPaneLayout.isOpen()) {
            slidingPaneLayout.closePane();
        }
        if(exam.getTemplateType() == 2) {
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
            previous.setTextColor(getResources().getColor(R.color.testpress_nav_button_disabled));
        } else {
            previous.setClickable(true);
            previous.setTextColor(getResources().getColor(R.color.testpress_color_primary));
        }

        if ((position + 1) == attemptItemList.size()) {
            // Reached last question
            next.setTextColor(getResources().getColor(R.color.testpress_red));
            next.setText(R.string.testpress_end);
        } else {
            next.setTextColor(getResources().getColor(R.color.testpress_color_primary));
            next.setText(R.string.testpress_next);
        }
    }

    private void showPreviousQuestion() {
        if (pager.getCurrentItem() != 0) {
            goToQuestion(pager.getCurrentItem() - 1);
        }
    }

    private void endExamAlert() {
        new AlertDialog.Builder(getActivity(), R.style.TestpressAppCompatAlertDialogStyle)
                .setTitle(R.string.testpress_end_message)
                .setPositiveButton(R.string.testpress_end, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                       endExam.execute();
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
                        returnToHistory();
                    }
                })
                .setNegativeButton(R.string.testpress_cancel, null)
                .show();
    }

    @Override
    public Loader<List<AttemptItem>> onCreateLoader(int id, final Bundle args) {
        return new ThrowableLoader<List<AttemptItem>>(getActivity(), attemptItemList) {
            @Override
            public List<AttemptItem> loadData() throws Exception {
                List<AttemptItem> attemptItems = null;
                TestpressApiResponse<AttemptItem> response;
                String fragment;
                URL url = new URL(attempt.getQuestionsUrl());
                do {
                    try {
                        fragment = url.getFile().substring(1);
                    } catch (Exception e) {
                        return null;
                    }
                    try {
                        response = apiClient.getQuestions(fragment);
                        if (attemptItems != null) {
                            attemptItems.addAll(response.getResults());
                        } else {
                            attemptItems = response.getResults();
                        }
                        String next = response.getNext();
                        if (next != null) {
                            url = new URL(response.getNext());
                        }
                    } catch (OperationCanceledException e) {
                        return null;
                    }
                } while (response.getNext() != null);
                return attemptItems;
            }
        };
    }

    @Override
    public void onLoadFinished(final Loader<List<AttemptItem>> loader, final List<AttemptItem> items) {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        if(exam.getTemplateType() == 2) {  // For IBPS templates only
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
                        .getSubject().isEmpty()) { // If subject is empty, subject = "Uncategorized"

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
            // Store each set of subject items to attemptItemList
            for (String subject : subjectsList) {
                subjectsOffset.put(subject, attemptItemList.size()); // Add subjects & it starting point
                attemptItemList.addAll(subjectsWiseItems.get(subject));
                subjectSpinnerAdapter.addItem(subject, subject, true, 0);
            }
            if ((spinnerContainer.getVisibility() == View.GONE) && subjectsWiseItems.size() > 1) {
                // Show spinner only if #subjects > 1
                subjectSpinnerAdapter.notifyDataSetChanged();
                spinnerContainer.setVisibility(View.VISIBLE);
            }
            subjectFilter.setSelection(0); // Set 1st item as default selection
            selectedSubjectOffset = 0;
        } else {
            attemptItemList = items;
        }
        pagerAdapter = new TestQuestionPagerAdapter(getFragmentManager(), attemptItemList);
        pagerAdapter. setCount(attemptItemList.size());
        pager.setAdapter(pagerAdapter);
        pagerAdapter.notifyDataSetChanged();
        for (int i = 0; i< attemptItemList.size(); i++) {
            attemptItemList.get(i).setIndex(i + 1);
        }
        panelListAdapter.setItems(attemptItemList);
        questionsListView.setAdapter(panelListAdapter);
        startCountDownTimer(formatMillisecond(attempt.getRemainingTime()));
    }

    @Override
    public void onLoaderReset(final Loader<List<AttemptItem>> loader) {
    }

    private  void returnToHistory() {
        getActivity().setResult(Activity.RESULT_OK);
        getActivity().finish();
    }

    private  void showReview() {
        // Todo: Goto ReviewActivity
    }

    private void saveResult(final int position) {
        final AttemptItem attemptItem = attemptItemList.get(position);
        new SafeAsyncTask<AttemptItem>() {
            @Override
            public AttemptItem call() throws Exception {
                String fragment= null;
                try {
                    URL urlFrag = new URL(attemptItem.getUrl());
                    fragment = urlFrag.getFile().substring(1);
                } catch (MalformedURLException e) {
                }
                return apiClient.postAnswer(fragment, attemptItem.getSavedAnswers(),
                        attemptItem.getCurrentReview());
            }

            @Override
            protected void onException(Exception e) {
                super.onException(e);
                countDownTimer.cancel();
                new AlertDialog.Builder(getActivity(), R.style.TestpressAppCompatAlertDialogStyle)
                        .setTitle(R.string.testpress_no_internet_connection)
                        .setMessage(R.string.testpress_exam_paused_check_internet)
                        .setCancelable(false)
                        .setPositiveButton(R.string.testpress_resume,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        progressDialog.show();
                                        saveResult(position);
                                    }
                        })
                        .show();
            }

            @Override
            protected void onSuccess(AttemptItem newAttemptItem) throws Exception {
                super.onSuccess(newAttemptItem);
                if (progressDialog.isShowing()) {
                    startCountDownTimer(millisRemaining);
                    progressDialog.dismiss();
                }
                attemptItem.setSelectedAnswers(newAttemptItem.getSelectedAnswers());
                attemptItem.setReview(newAttemptItem.getReview());
                attemptItemList.set(position, attemptItem);
                pagerAdapter.notifyDataSetChanged();
                updatePanel();
            }
        }.execute();
    }

    private SafeAsyncTask<Attempt> sendHeartBeat = new SafeAsyncTask<Attempt>() {
        @Override
        public Attempt call() throws Exception {
            return  apiClient.heartbeat(attempt.getHeartBeatUrlFrag());
        }

        @Override
        protected void onException(Exception e) {
            super.onException(e);
            countDownTimer.cancel();
            new AlertDialog.Builder(getActivity(), R.style.TestpressAppCompatAlertDialogStyle)
                    .setTitle(R.string.testpress_no_internet_connection)
                    .setMessage(R.string.testpress_exam_paused_check_internet)
                    .setCancelable(false)
                    .setPositiveButton(R.string.testpress_resume,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    progressDialog.show();
                                    sendHeartBeat.execute();
                                }
                            })
                    .show();
        }

        @Override
        protected void onSuccess(Attempt attempt) throws Exception {
            super.onSuccess(attempt);
            if (progressDialog.isShowing()) {
                startCountDownTimer(millisRemaining);
                progressDialog.dismiss();
            }
        }
    };

    private SafeAsyncTask<Attempt> endExam = new SafeAsyncTask<Attempt>() {
        @Override
        public Attempt call() throws Exception {
            countDownTimer.cancel();
            attempt = apiClient.endExam(attempt.getUrlFrag() + TestpressExamApiClient.END_EXAM_PATH);
            return attempt;
        }

        @Override
        protected void onException(Exception e) {
            super.onException(e);
            new AlertDialog.Builder(getActivity(), R.style.TestpressAppCompatAlertDialogStyle)
                    .setTitle(R.string.testpress_no_internet_connection)
                    .setMessage(R.string.testpress_exam_paused_check_internet_to_end)
                    .setCancelable(false)
                    .setPositiveButton(R.string.testpress_try_again,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    progressDialog.show();
                                    endExam.execute();
                                }
                            })
                    .show();
        }

        @Override
        protected void onSuccess(Attempt attempt) throws Exception {
            super.onSuccess(attempt);
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            showReview();
        }
    };

    private static String formatTime(final long millis) {
        return String.format("%02d:%02d:%02d",
                millis / (1000 * 60 * 60),
                (millis / (1000 * 60)) % 60,
                (millis / 1000) % 60
        );
    }

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
        if (attemptItemList.get(pager.getCurrentItem()).hasChanged()) {
            saveResult(pager.getCurrentItem());
        }
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
                    sendHeartBeat.execute();
                }
            }

            public void onFinish() {
                endExam.execute();
            }
        }.start();
    }

}
