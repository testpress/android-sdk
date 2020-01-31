package in.testpress.course.ui.fragments.content_detail_fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSDKDatabase;
import in.testpress.core.TestpressSdk;
import in.testpress.course.R;
import in.testpress.course.ui.ContentAttemptListAdapter;
import in.testpress.exam.TestpressExam;
import in.testpress.exam.network.TestpressExamApiClient;
import in.testpress.exam.util.MultiLanguagesUtil;
import in.testpress.exam.util.RetakeExamUtil;
import in.testpress.models.TestpressApiResponse;
import in.testpress.models.greendao.Attempt;
import in.testpress.models.greendao.AttemptDao;
import in.testpress.models.greendao.Content;
import in.testpress.models.greendao.CourseAttempt;
import in.testpress.models.greendao.CourseAttemptDao;
import in.testpress.models.greendao.Exam;
import in.testpress.models.greendao.ExamDao;
import in.testpress.models.greendao.Language;
import in.testpress.util.FormatDate;
import in.testpress.util.ViewUtils;

import static in.testpress.course.ui.ContentActivity.FORCE_REFRESH;
import static in.testpress.course.ui.ContentActivity.TESTPRESS_CONTENT_SHARED_PREFS;
import static in.testpress.exam.network.TestpressExamApiClient.STATE_PAUSED;

public class ExamContentFragment extends BaseContentDetailFragment {
    private ExamDao examDao;
    private LinearLayout examContentLayout;
    private LinearLayout examDetailsLayout;
    private RecyclerView attemptList;
    private TextView titleView;
    private LinearLayout titleLayout;
    private List<CourseAttempt> courseAttemptsFromDB = new ArrayList<>();
    private String attemptsUrl;
    private List<CourseAttempt> courseAttemptsFromNetwork = new ArrayList<>();
    private TestpressExamApiClient examApiClient;
    private CourseAttemptDao courseAttemptDao;
    private AttemptDao attemptDao;
    private Button startButton;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        examDao = TestpressSDKDatabase.getExamDao(getActivity());
        examApiClient = new TestpressExamApiClient(getActivity());
        courseAttemptDao = TestpressSDKDatabase.getCourseAttemptDao(getActivity());
        attemptDao = TestpressSDKDatabase.getAttemptDao(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.exam_content_detail, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        examContentLayout = (LinearLayout) view.findViewById(R.id.exam_content_layout);
        examDetailsLayout = (LinearLayout) view.findViewById(R.id.exam_details_layout);
        attemptList = (RecyclerView) view.findViewById(R.id.attempt_list);
        titleView = (TextView) view.findViewById(R.id.title);
        titleLayout = (LinearLayout) view.findViewById(R.id.title_layout);
        startButton = (Button) view.findViewById(R.id.start_exam);

        if (content != null) {
            loadContent();
        }
    }

    @Override
    void hideContents() {
        examDetailsLayout.setVisibility(View.GONE);
    }

    @Override
    void loadContent() {
        Log.d("ContentDetailFragment", "loadContent: ");
        titleView.setText(content.getTitle());
        titleLayout.setVisibility(View.VISIBLE);

        if (content.getRawExam() == null || content.getAttemptsUrl() == null) {
            updateContent();
            return;
        }

        // forceRefresh if already attempts is listed(courseAttemptsFromDB is populated)
        boolean forceRefresh = !courseAttemptsFromDB.isEmpty();
        courseAttemptsFromDB.clear();
        if (content.getAttemptsCount() > 0) {
            attemptsUrl = content.getAttemptsUrl();
            courseAttemptsFromNetwork.clear();
            loadAttempts(forceRefresh);
        } else {
            fetchLanguages(null);
        }
    }

    private void loadAttempts(final boolean forceRefresh) {
        Log.d("ContentDetailFragment", "loadAttempts: ");

        courseAttemptsFromDB = getCourseAttemptsFromDB();
        if (courseAttemptsFromDB.size() > 0 && !forceRefresh) {
            onCourseAttemptsLoaded();
        } else {
            showLoadingProgress();
        }
        examApiClient.getContentAttempts(attemptsUrl)
                .enqueue(new TestpressCallback<TestpressApiResponse<CourseAttempt>>() {
                    @Override
                    public void onSuccess(TestpressApiResponse<CourseAttempt> response) {
                        courseAttemptsFromNetwork.addAll(response.getResults());
                        if (response.getNext() != null) {
                            attemptsUrl = response.getNext();
                            loadAttempts(forceRefresh);
                        } else {
                            clearContentAttemptsInDB();
                            saveCourseAttemptInDB(courseAttemptsFromNetwork);
                            courseAttemptsFromDB = getCourseAttemptsFromDB();
                            onCourseAttemptsLoaded();
                        }
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        handleError(exception, false);
                    }
                });
    }


    private void saveCourseAttemptInDB(List<CourseAttempt> courseAttemptList) {
        for(CourseAttempt courseAttempt : courseAttemptList) {
            Attempt attempt = courseAttempt.getRawAssessment();
            attemptDao.insertOrReplace(attempt);
            courseAttempt.setAssessmentId(attempt.getId());
            courseAttempt.setChapterContentId(content.getId());
            courseAttemptDao.insertOrReplace(courseAttempt);
        }
    }

    private void clearContentAttemptsInDB() {
        courseAttemptDao.queryBuilder()
                .where(CourseAttemptDao.Properties.ChapterContentId.eq(content.getId()))
                .buildDelete()
                .executeDeleteWithoutDetachingEntities();
    }


    private void onCourseAttemptsLoaded() {
        if (courseAttemptsFromDB.size() == 1 &&
                courseAttemptsFromDB.get(0).getAssessment().getState().equals(STATE_PAUSED)) {
            // Only one paused attempt
            fetchLanguages(courseAttemptsFromDB.get(0));
        } else {
            displayAttemptsList();
        }
    }

    private void displayAttemptsList() {
        courseAttemptsFromDB = getCourseAttemptsFromDB();
        final Exam exam = content.getRawExam();
        final List<CourseAttempt> pausedAttempts = new ArrayList<>();
        if (exam.getPausedAttemptsCount() > 0) {
            for (CourseAttempt attempt : courseAttemptsFromDB) {
                if (attempt.getAssessment().getState().equals(STATE_PAUSED)) {
                    pausedAttempts.add(attempt);
                }
            }
        }
        updateStartButton(exam, pausedAttempts.isEmpty() ? null : pausedAttempts.get(0), false);
        attemptList.setNestedScrollingEnabled(false);
        attemptList.setHasFixedSize(true);
        attemptList.setLayoutManager(new LinearLayoutManager(getActivity()));
        ArrayList<CourseAttempt> attempts = new ArrayList<>(courseAttemptsFromDB);
        attemptList.setAdapter(new ContentAttemptListAdapter(getActivity(), content, attempts));
        attemptList.setVisibility(View.VISIBLE);
        examDetailsLayout.setVisibility(View.GONE);
        examContentLayout.setVisibility(View.VISIBLE);
        swipeRefresh.setRefreshing(false);
    }


    void fetchLanguages(final CourseAttempt pausedCourseAttempt) {
        showLoadingProgress();
        examApiClient.getLanguages(content.getRawExam().getSlug())
                .enqueue(new TestpressCallback<TestpressApiResponse<Language>>() {
                    @Override
                    public void onSuccess(TestpressApiResponse<Language> apiResponse) {
                        Exam exam = content.getRawExam();
                        List<Language> languages = exam.getRawLanguages();
                        languages.addAll(apiResponse.getResults());
                        Map<String, Language> uniqueLanguages = new HashMap<>();
                        for (Language language : languages) {
                            uniqueLanguages.put(language.getCode(), language);
                        }
                        content.getRawExam().setLanguages(new ArrayList<>(uniqueLanguages.values()));
                        if (apiResponse.hasMore()) {
                            fetchLanguages(pausedCourseAttempt);
                        } else {
                            exam.saveLanguages(getContext());
                            displayStartExamScreen(pausedCourseAttempt);
                        }
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        handleError(exception, false);
                    }
                });
    }

    @SuppressLint("SetTextI18n")
    private void displayStartExamScreen(CourseAttempt pausedCourseAttempt) {
        final Exam exam = content.getRawExam();
        TextView numberOfQuestions = (TextView) getView().findViewById(R.id.number_of_questions);
        TextView examDuration = (TextView) getView().findViewById(R.id.exam_duration);
        TextView markPerQuestion = (TextView) getView().findViewById(R.id.mark_per_question);
        TextView negativeMarks = (TextView) getView().findViewById(R.id.negative_marks);
        TextView date = (TextView) getView().findViewById(R.id.date);
        LinearLayout description = (LinearLayout) getView().findViewById(R.id.description);
        LinearLayout dateLayout = (LinearLayout) getView().findViewById(R.id.date_layout);
        TextView descriptionContent = (TextView) getView().findViewById(R.id.descriptionContent);
        TextView questionsLabel = (TextView) getView().findViewById(R.id.questions_label);
        TextView durationLabel = (TextView) getView().findViewById(R.id.duration_label);
        TextView markLabel = (TextView) getView().findViewById(R.id.mark_per_question_label);
        TextView negativeMarkLabel = (TextView) getView().findViewById(R.id.negative_marks_label);
        TextView dateLabel = (TextView) getView().findViewById(R.id.date_label);
        TextView languageLabel = (TextView) getView().findViewById(in.testpress.exam.R.id.language_label);
        ViewUtils.setTypeface(new TextView[] {numberOfQuestions, examDuration, markPerQuestion,
                negativeMarks, date }, TestpressSdk.getRubikMediumFont(getActivity()));
        ViewUtils.setTypeface(
                new TextView[] {
                        descriptionContent, questionsLabel, languageLabel, durationLabel, markLabel,
                        negativeMarkLabel, dateLabel
                },
                TestpressSdk.getRubikRegularFont(getActivity())
        );
        numberOfQuestions.setText(exam.getNumberOfQuestions().toString());
        if (pausedCourseAttempt == null) {
            examDuration.setText(exam.getDuration());
        } else {
            durationLabel.setText(getString(R.string.testpress_time_remaining));
            examDuration.setText(pausedCourseAttempt.getAssessment().getRemainingTime());
        }
        markPerQuestion.setText(exam.getMarkPerQuestion());
        negativeMarks.setText(exam.getNegativeMarks());
        if (exam.getFormattedStartDate().equals("forever")) {
            dateLayout.setVisibility(View.GONE);
        } else {
            date.setText(exam.getFormattedStartDate() + " -\n" + exam.getFormattedEndDate());
            dateLayout.setVisibility(View.VISIBLE);
        }
        if ((exam.getDescription() != null) && !exam.getDescription().trim().isEmpty()) {
            description.setVisibility(View.VISIBLE);
            descriptionContent.setText(exam.getDescription());
        }
        updateStartButton(exam, pausedCourseAttempt, true);
        attemptList.setVisibility(View.GONE);
        examDetailsLayout.setVisibility(View.VISIBLE);
        examContentLayout.setVisibility(View.VISIBLE);
        swipeRefresh.setRefreshing(false);
    }

    private void updateStartButton(final Exam exam, final CourseAttempt pausedCourseAttempt,
                                   final boolean discardExamDetails) {

        if (pausedCourseAttempt == null && canAttemptExam(exam)) {
            if (courseAttemptsFromDB.isEmpty()) {
                startButton.setText(R.string.testpress_start);
            } else {
                startButton.setText(R.string.testpress_retake);
            }
            if (discardExamDetails) {
                MultiLanguagesUtil.supportMultiLanguage(getActivity(), exam, startButton,
                        new MultiLanguagesUtil.LanguageSelectionListener() {
                            @Override
                            public void onLanguageSelected() {
                                startCourseExam(true, false);
                            }});
            } else {
                startButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        RetakeExamUtil.showRetakeOptions(getContext(),
                                new RetakeExamUtil.SelectionListener() {
                                    @Override
                                    public void onOptionSelected(boolean isPartial) {
                                        startCourseExam(false, isPartial);
                                    }
                                });
                    }
                });
            }
            startButton.setVisibility(View.VISIBLE);
        } else if (pausedCourseAttempt != null && !isWebOnlyExam(exam)) {
            startButton.setText(R.string.testpress_resume);
            if (discardExamDetails) {
                MultiLanguagesUtil.supportMultiLanguage(getActivity(), exam, startButton,
                        new MultiLanguagesUtil.LanguageSelectionListener() {
                            @Override
                            public void onLanguageSelected() {
                                resumeCourseExam(true, pausedCourseAttempt);
                            }});
            } else {
                startButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        resumeCourseExam(false, pausedCourseAttempt);
                    }
                });
            }
            startButton.setVisibility(View.VISIBLE);
        } else {
            startButton.setVisibility(View.GONE);
        }
    }

    private void startCourseExam(boolean discardExamDetails, boolean isPartial) {
        //noinspection ConstantConditions
        TestpressExam.startCourseExam(getActivity(), content, discardExamDetails, isPartial,
                TestpressSdk.getTestpressSession(getActivity()));
    }

    private void resumeCourseExam(boolean discardExamDetails, CourseAttempt pausedCourseAttempt) {
        //noinspection ConstantConditions
        TestpressExam.resumeCourseAttempt(getActivity(), content, pausedCourseAttempt, discardExamDetails,
                TestpressSdk.getTestpressSession(getActivity()));
    }

    private boolean canAttemptExam(Exam exam) {
        if (exam.getAttemptsCount() == 0 ||
                ((exam.getAllowRetake()) &&
                        ((exam.getAttemptsCount() + exam.getPausedAttemptsCount()) <= exam.getMaxRetakes() ||
                                exam.getMaxRetakes() < 0))) {

            if (content.getIsLocked() || (content.getHasStarted() != null && !content.getHasStarted()) || exam.isEnded()) {
                if (courseAttemptsFromDB.isEmpty()) {
                    TextView webOnlyLabel = (TextView) getView().findViewById(R.id.web_only_label);
                    if (content.getHasStarted() != null && !content.getHasStarted()) {
                        webOnlyLabel.setText(String.format(
                                getString(R.string.testpress_can_start_exam_only_after),
                                FormatDate.formatDateTime(exam.getStartDate())
                        ));
                    } else if (exam.isEnded()) {
                        webOnlyLabel.setText(R.string.testpress_exam_ended);
                    } else {
                        webOnlyLabel.setText(R.string.testpress_score_good_in_previous_exam);
                    }
                    webOnlyLabel.setTypeface(TestpressSdk.getRubikRegularFont(getActivity()));
                    webOnlyLabel.setVisibility(View.VISIBLE);
                }
                return false;
            } else {
                return !isWebOnlyExam(exam);
            }
        } else {
            return false;
        }
    }

    private boolean isWebOnlyExam(Exam exam) {
        if (exam.getDeviceAccessControl() != null &&
                exam.getDeviceAccessControl().equals("web")) {
            TextView webOnlyLabel;
            if (courseAttemptsFromDB.isEmpty()) {
                webOnlyLabel = (TextView) getView().findViewById(R.id.web_only_label);
            } else {
                webOnlyLabel = (TextView) getView().findViewById(R.id.attempt_web_only_label);
            }
            webOnlyLabel.setTypeface(TestpressSdk.getRubikRegularFont(getActivity()));
            webOnlyLabel.setVisibility(View.VISIBLE);
            return true;
        }
        return false;
    }

    private List<CourseAttempt> getCourseAttemptsFromDB() {
        return courseAttemptDao.queryBuilder()
                .where(CourseAttemptDao.Properties.ChapterContentId.eq(content.getId())).list();
    }

    @Override
    void onUpdateContent(Content fetchedContent) {
        Exam exam = fetchedContent.getRawExam();
        if (exam != null) {
            exam.saveLanguages(getContext());
            examDao.insertOrReplace(exam);
            fetchedContent.setExamId(exam.getId());
        }
        contentDao.insertOrReplace(fetchedContent);
        content = fetchedContent;
    }

    @Override
    void onCreateContentAttempt(CourseAttempt courseAttempt) {
        if (content.getAttemptsCount() == 0) {
            SharedPreferences prefs = getActivity().getSharedPreferences(
                    TESTPRESS_CONTENT_SHARED_PREFS, Context.MODE_PRIVATE);
            prefs.edit().putBoolean(FORCE_REFRESH, true).apply();
        }
    }

}
