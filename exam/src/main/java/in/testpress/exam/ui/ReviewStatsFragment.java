package in.testpress.exam.ui;

import static android.content.Context.DOWNLOAD_SERVICE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentActivity;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import junit.framework.Assert;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;

import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSdk;
import in.testpress.exam.R;
import in.testpress.exam.api.TestpressExamApiClient;
import in.testpress.exam.util.RetakeExamUtil;
import in.testpress.models.InstituteSettings;
import in.testpress.models.TestpressApiResponse;
import in.testpress.models.greendao.Attempt;
import in.testpress.models.greendao.Content;
import in.testpress.models.greendao.CourseAttempt;
import in.testpress.models.greendao.Exam;
import in.testpress.network.RetrofitCall;
import in.testpress.ui.BaseFragment;
import in.testpress.ui.WebViewActivity;
import in.testpress.util.FileDownloader;
import in.testpress.util.StringUtils;
import in.testpress.util.UIUtils;
import in.testpress.util.ViewUtils;

import static in.testpress.exam.ui.CarouselFragment.TEST_TAKEN_REQUEST_CODE;
import static in.testpress.exam.ui.ReviewStatsActivity.PARAM_ATTEMPT;
import static in.testpress.exam.ui.ReviewStatsActivity.PARAM_COURSE_ATTEMPT;
import static in.testpress.exam.ui.ReviewStatsActivity.PARAM_EXAM;
import static in.testpress.exam.ui.ReviewStatsActivity.PARAM_PREVIOUS_ACTIVITY;

import com.android.installreferrer.BuildConfig;

public class ReviewStatsFragment extends BaseFragment {

    static final String PARAM_SHOW_RETAKE_BUTTON = "showRetakeButton";
    static final String SHARE_TO_UNLOCK_SHARED_PREFERENCE_KEY = "shareToUnlockconstant";
    static final Integer SHARE_APP = 1000;
    static final String NO_OF_TIMES_SHARED = "NO_OF_TIMES_SHARED";
    static final String MESSAGE_TO_SHARE = "MESSAGE_TO_SHARE";

    private TextView examTitle;
    private TextView attemptDate;
    private TextView timeTaken;
    private TextView score;
    private TextView rank;
    private TextView maxRank;
    private TextView correct;
    private TextView incorrect;
    private TextView accuracy;
    private TextView percentile;
    private TextView percentage;
    private TextView totalQuestions;
    private TextView totalMarks;
    private TextView totalTime;
    private TextView cutoff;
    private LinearLayout scoreLayout;
    private LinearLayout percentileLayout;
    private LinearLayout percentageLayout;
    private LinearLayout rankLayout;
    private LinearLayout reviewStatLayout;
    private ProgressBar progressBar;
    private View emptyView;
    private ImageView emptyViewImage;
    private TextView emptyTitleView;
    private TextView emptyDescView;
    private Button retryButton;
    private TextView analyticsButton;
    private TextView advancedAnalyticsButton;
    private LinearLayout advancedAnalyticsLayout;
    private TextView retakeButton;
    private LinearLayout retakeButtonLayout;
    private LinearLayout timeAnalyticsButtonLayout;
    private LinearLayout shareButtonLayout;
    private LinearLayout statsButtonLayout;
    private LinearLayout totalTimeLayout;
    private LinearLayout cutoffLayout;
    private LinearLayout totalMarksLayout;
    private Button shareButton;
    private TextView reviewQuestionsButton;
    private TextView emailPdfButton;
    private LinearLayout emailPdfButtonLayout;
    private Button timeAnalyticsButton;
    private Attempt attempt;
    private Exam exam;
    private InstituteSettings instituteSettings;
    private RetrofitCall<TestpressApiResponse<Attempt>> attemptsApiRequest;
    private boolean isQuiz = false;

    public static void showReviewStatsFragment(FragmentActivity activity, Exam exam, Attempt attempt,
                                               boolean showRetakeButton) {

        ReviewStatsFragment fragment = new ReviewStatsFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(PARAM_EXAM, exam);
        bundle.putParcelable(PARAM_ATTEMPT, attempt);
        bundle.putBoolean(PARAM_SHOW_RETAKE_BUTTON, showRetakeButton);
        fragment.setArguments(bundle);
        activity.getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commitAllowingStateLoss();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        exam = getArguments().getParcelable(PARAM_EXAM);
        Assert.assertNotNull("PARAM_EXAM must not be null.", exam);
        instituteSettings = getInstituteSettings();
        attempt = getArguments().getParcelable(PARAM_ATTEMPT);
        CourseAttempt courseAttempt = getArguments().getParcelable(PARAM_COURSE_ATTEMPT);
        if (courseAttempt != null) {
            attempt = courseAttempt.getRawAssessment();
            Content content = courseAttempt.getRawChapterContent();
            isQuiz = (content != null) && (content.getContentType().equals("Quiz"));
        }
        if (attempt.getReviewPdf() != null && !attempt.getReviewPdf().isEmpty()){
            setHasOptionsMenu(true);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.testpress_fragment_review_stats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);
        showOrHideShareButton();
        addClickListeners();
        if (isQuiz) {
            hideViewsForQuiz();
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.testpress_download_icon, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.download_icon) {
            showDownloadDialogBox();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void showDownloadDialogBox(){

        String filename = changeFilenameIfAlreadyExist(exam.getTitle()+attempt.getId());

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.TestpressAppCompatAlertDialogStyle);
        builder.setTitle("Review PDF Download");
        builder.setMessage(filename);
        builder.setPositiveButton("Download", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
//                Log.d("TAG", "onClick: "+attempt.getReviewPdf());
//                requireActivity().startActivity(new Intent(Intent.ACTION_VIEW,
//                        Uri.parse(attempt.getReviewPdf())));
                Log.d("TAG", "onClick: "+ Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
                downloadAndOpenPdfFile("https://www.sampledocs.in/DownloadFiles/SampleFile?filename=sampledocs-100mb-pdf-file&ext=pdf",filename);
            }
        });
        builder.setNegativeButton("cancel", null);
        builder.show();
    }

    private String changeFilenameIfAlreadyExist(String fileName) {
        int counter = 1;
        String newFileName = fileName + ".pdf";
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), newFileName);
        while (file.exists()) {
            // If the file already exists, append a counter to the filename and try again
            newFileName = fileName + " (" + counter + ")" + ".pdf";
            file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), newFileName);
            counter++;
        }
        return newFileName;
    }

    private void downloadAndOpenPdfFile(String pdfUrl, String pdfFilename) {

        // Create a DownloadManager.Request object to specify the PDF file to download
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(pdfUrl))
                .setTitle(pdfFilename)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,pdfFilename);

        // Get the DownloadManager service and enqueue the download request
        DownloadManager downloadManager = (DownloadManager) requireContext().getSystemService(DOWNLOAD_SERVICE);
        long downloadId = downloadManager.enqueue(request);

        // Create a BroadcastReceiver to listen for completion of the download
        BroadcastReceiver onComplete = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Get the ID of the completed download from the intent
                long completedDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

                // If the completed download matches the one we started earlier, show a notification and open the PDF file
                if (completedDownloadId == downloadId) {
                    Intent intent1 = new Intent(Intent.ACTION_VIEW);
                    File pdfFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), pdfFilename);
                    Uri uri = FileProvider.getUriForFile(requireContext(), context.getPackageName()+ ".provider", pdfFile);
                    intent1.setDataAndType(uri, "application/pdf");
                    intent1.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    PendingIntent pendingIntent = PendingIntent.getActivity(requireContext(), 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
                    showNotification(pdfFilename, pendingIntent);
                }
            }
        };

        // Register the BroadcastReceiver to listen for completion of the download
        requireContext().registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    private void showNotification(String pdfFilename, PendingIntent pendingIntent) {
        // Create a notification channel if necessary (for Android 8.0 and higher)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("pdf_download_channel", "PDF Download", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = requireContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        // Create a notification to show that the download is complete
        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), "pdf_download_channel")
                .setSmallIcon(R.drawable.ic_file_download_18dp)
                .setContentTitle("PDF Download Complete")
                .setContentText(pdfFilename + " has been downloaded.")
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Show the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(requireContext());
        notificationManager.notify(1, builder.build());
    }

    private void bindViews(View view) {
        progressBar = (ProgressBar) view.findViewById(R.id.pb_loading);
        UIUtils.setIndeterminateDrawable(getActivity(), progressBar, 4);
        emptyView = view.findViewById(R.id.empty_container);
        emptyViewImage = (ImageView) view.findViewById(R.id.image_view);
        emptyTitleView = (TextView) view.findViewById(R.id.empty_title);
        emptyDescView = (TextView) view.findViewById(R.id.empty_description);
        retryButton = (Button) view.findViewById(R.id.retry_button);
        examTitle = (TextView) view.findViewById(R.id.exam_title);
        attemptDate = (TextView) view.findViewById(R.id.attempt_date);
        timeTaken = (TextView) view.findViewById(R.id.time_taken);
        score = (TextView) view.findViewById(R.id.score);
        rank = (TextView) view.findViewById(R.id.rank);
        maxRank = (TextView) view.findViewById(R.id.max_rank);
        correct = (TextView) view.findViewById(R.id.correct_count);
        incorrect = (TextView) view.findViewById(R.id.incorrect_count);
        accuracy = (TextView) view.findViewById(R.id.accuracy);
        percentile = (TextView) view.findViewById(R.id.percentile);
        percentage = (TextView) view.findViewById(R.id.percentage);
        totalQuestions = (TextView) view.findViewById(R.id.total_questions);
        totalMarks = (TextView) view.findViewById(R.id.total_marks);
        totalTime = (TextView) view.findViewById(R.id.total_time);
        cutoff = (TextView) view.findViewById(R.id.cutoff);
        scoreLayout = (LinearLayout) view.findViewById(R.id.score_layout);
        percentageLayout = (LinearLayout) view.findViewById(R.id.percentage_layout);
        percentileLayout = (LinearLayout) view.findViewById(R.id.percentile_layout);
        rankLayout = (LinearLayout) view.findViewById(R.id.rank_layout);
        reviewStatLayout = (LinearLayout) view.findViewById(R.id.review_statistics_layout);
        reviewStatLayout.setVisibility(View.GONE);
        analyticsButton = (TextView) view.findViewById(R.id.analytics);
        advancedAnalyticsButton = (TextView) view.findViewById(R.id.advanced_analytics);
        advancedAnalyticsLayout = (LinearLayout) view.findViewById(R.id.advanced_analytics_layout);
        retakeButton = (TextView) view.findViewById(R.id.retake);
        emailPdfButton = (TextView) view.findViewById(R.id.email_mcqs);
        retakeButtonLayout = (LinearLayout) view.findViewById(R.id.retake_button_layout);
        timeAnalyticsButtonLayout = (LinearLayout) view.findViewById(R.id.time_analytics_layout);
        emailPdfButtonLayout = (LinearLayout) view.findViewById(R.id.email_mcqs_layout);
        reviewQuestionsButton = (TextView) view.findViewById(R.id.review);
        timeAnalyticsButton = (Button) view.findViewById(R.id.time_analytics);
        shareButton = view.findViewById(R.id.share_to_unlock);
        shareButtonLayout = view.findViewById(R.id.share_button_layout);
        statsButtonLayout = view.findViewById(R.id.button_layout);
        totalMarksLayout = view.findViewById(R.id.total_marks_layout);
        totalTimeLayout = view.findViewById(R.id.total_time_layout);
        cutoffLayout = view.findViewById(R.id.cutoff_layout);
        ViewUtils.setTypeface(
                new TextView[] {
                        score, rank, correct, incorrect, timeTaken, accuracy, reviewQuestionsButton,
                        analyticsButton, emailPdfButton, retakeButton, emptyTitleView, retryButton,
                        timeAnalyticsButton, percentage, totalQuestions, totalMarks, totalTime,
                        cutoff, percentile
                },
                TestpressSdk.getRubikMediumFont(getContext())
        );
        TextView timeTakenLabel = (TextView) view.findViewById(R.id.time_taken_label);
        TextView scoreLabel = (TextView) view.findViewById(R.id.score_label);
        TextView rankLabel = (TextView) view.findViewById(R.id.rank_label);
        TextView correctLabel = (TextView) view.findViewById(R.id.correct_label);
        TextView incorrectLabel = (TextView) view.findViewById(R.id.incorrect_label);
        TextView accuracyLabel = (TextView) view.findViewById(R.id.accuracy_label);
        ViewUtils.setTypeface(
                new TextView[] {
                        scoreLabel, rankLabel, correctLabel, incorrectLabel, timeTakenLabel,
                        accuracyLabel, examTitle, attemptDate, emptyDescView, maxRank
                },
                TestpressSdk.getRubikRegularFont(getContext()));

    }

    private void addClickListeners() {
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openShareFragment();
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity().getClass() == ReviewStatsActivity.class) {
            ((ReviewStatsActivity) getActivity()).setActionBarTitle(R.string.testpress_test_report);
        }
        if (attempt == null) {
            loadAttempt();
        } else {
            displayTestReport();
        }
    }

    @SuppressLint("SetTextI18n")
    private void displayTestReport() {
        String previousActivity = getArguments().getString(PARAM_PREVIOUS_ACTIVITY);
        if((previousActivity != null) && previousActivity.equals(TestActivity.class.getName())) {
            attemptDate.setVisibility(View.GONE);
        } else {
            attemptDate.setText(attempt.getShortDate());
        }
        examTitle.setText(exam.getTitle());
        timeTaken.setText(attempt.getTimeTaken());
        correct.setText(attempt.getCorrectCount().toString());
        incorrect.setText(attempt.getIncorrectCount().toString());
        if (attempt.getRank().equals("NA") && !exam.getEnableRanks()) {
            rankLayout.setVisibility(View.GONE);
        } else {
            rank.setText(attempt.getRank());
            maxRank.setText(attempt.getMaxRank());
        }
        if (attempt.getPercentage() == null || attempt.getPercentage().equals("NA")) {
            percentageLayout.setVisibility(View.GONE);
        } else {
            percentage.setText(attempt.getPercentage());
        }
        if ((exam.getShowScore()) && (attempt.getScore() == null || attempt.getScore().equals("NA"))) {
            scoreLayout.setVisibility(View.GONE);
        } else {
            score.setText(attempt.getScore());
        }
        if ((exam.getShowPercentile()) &&
                (attempt.getPercentile() == null || attempt.getPercentile().equals("NA"))) {
            percentileLayout.setVisibility(View.GONE);
        } else {
            percentile.setText(attempt.getPercentile());
        }
        totalQuestions.setText(attempt.getTotalQuestions().toString());
        if (exam.getTotalMarks() != null) {
            totalMarks.setText(exam.getTotalMarks());
        }
        totalTime.setText(exam.getDuration());
        if (exam.getPassPercentage() != null) {
            cutoff.setText(exam.getPassPercentage().toString());
        }
        accuracy.setText(attempt.getAccuracy().toString());
        if (exam.getShowAnswers()) {
            reviewQuestionsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    requireActivity().startActivity(
                            ReviewQuestionsActivity.createIntent(getActivity(), exam, attempt)
                    );
                }
            });
            reviewQuestionsButton.setVisibility(View.VISIBLE);
            analyticsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().startActivity(
                            AnalyticsActivity.createIntent(getActivity(), attempt.getUrlFrag() +
                                    TestpressExamApiClient.ATTEMPT_SUBJECT_ANALYTICS_PATH, null, exam.getTitle())
                    );
                }
            });
            analyticsButton.setVisibility(View.VISIBLE);
            timeAnalyticsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().startActivity(
                            TimeAnalyticsActivity.createIntent(getActivity(), exam, attempt)
                    );
                }
            });
            // TODO: Clean up TimeAnalytics & enable it
            timeAnalyticsButtonLayout.setVisibility(View.GONE);
        } else {
            reviewQuestionsButton.setVisibility(View.GONE);
            timeAnalyticsButtonLayout.setVisibility(View.GONE);
        }

        if (exam.showAnalytics()) {
            analyticsButton.setVisibility(View.VISIBLE);
        } else {
            analyticsButton.setVisibility(View.GONE);
        }

        if (attempt.isExternalReviewUrlAvailable()){
            advancedAnalyticsLayout.setVisibility(View.VISIBLE);
            advancedAnalyticsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(requireContext(), WebViewActivity.class);
                    intent.putExtra("URL", attempt.getExternalReviewUrl());
                    intent.putExtra("TITLE", getText(R.string.testpress_advanced_analytics));
                    requireActivity().startActivity(intent);
                }
            });
        }

        if (Boolean.TRUE.equals(exam.getAllowPdf())) {
            emailPdfButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new EmailPdfDialog(getActivity(), R.style.TestpressAppCompatAlertDialogStyle,
                            true, attempt.getUrlFrag()).show();
                }
            });
        } else {
            emailPdfButtonLayout.setVisibility(View.GONE);
        }

        if (canAttemptExam() && getArguments().getBoolean(PARAM_SHOW_RETAKE_BUTTON, true)) {
            retakeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    RetakeExamUtil.showRetakeOptions(getContext(),
                            new RetakeExamUtil.SelectionListener() {
                                @Override
                                public void onOptionSelected(boolean isPartial) {
                                    startExam(isPartial);
                                }
                            });
                }
            });
        } else {
            retakeButtonLayout.setVisibility(View.GONE);
        }
        reviewStatLayout.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    private void hideViewsForQuiz() {
        totalMarksLayout.setVisibility(View.GONE);
        totalTimeLayout.setVisibility(View.GONE);
        cutoffLayout.setVisibility(View.GONE);
        scoreLayout.setVisibility(View.GONE);
        percentageLayout.setVisibility(View.GONE);
    }

    private boolean isAppNotSharedAlready() {
        String sharedPreferenceKey = exam.getShareToUnlockSharedPreferenceKey();
        SharedPreferences preferences = requireContext().getSharedPreferences(sharedPreferenceKey, Context.MODE_PRIVATE);
        return preferences.getInt(NO_OF_TIMES_SHARED, 0) < 2;
    }

    private void openShareFragment() {
        Intent intent = new Intent(getActivity(), ShareToUnLockActivity.class);
        intent.putExtra(SHARE_TO_UNLOCK_SHARED_PREFERENCE_KEY, exam.getShareToUnlockSharedPreferenceKey());
        String messageToShare = StringUtils.isNullOrEmpty(exam.getShareTextForSolutionUnlock()) ?
                instituteSettings.getAppShareText(): exam.getShareTextForSolutionUnlock();
        intent.putExtra(MESSAGE_TO_SHARE, messageToShare);
        requireActivity().startActivityForResult(intent, SHARE_APP);
    }

    private boolean shouldShowShareButton() {
        return (exam.isGrowthHackEnabled() && isAppNotSharedAlready()) ||
                (instituteSettings.isGrowthHackEnabled() && instituteSettings.isAppNotSharedAlready(requireContext()));
    }

    private void showOrHideShareButton() {
        if (shouldShowShareButton()) {
            shareButtonLayout.setVisibility(View.VISIBLE);
            statsButtonLayout.setVisibility(View.GONE);
        } else {
            statsButtonLayout.setVisibility(View.VISIBLE);
            shareButtonLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        showOrHideShareButton();
    }

    private void loadAttempt() {
        attemptsApiRequest = new TestpressExamApiClient(getActivity())
                .getAttempts(exam.getAttemptsUrl(), new HashMap<String, Object>())
                .enqueue(new TestpressCallback<TestpressApiResponse<Attempt>>() {
                    @Override
                    public void onSuccess(TestpressApiResponse<Attempt> response) {
                        if (getActivity() == null) {
                            return;
                        }
                        attempt = response.getResults().get(0);
                        if (exam.getAllowPdf()) {
                            getActivity().invalidateOptionsMenu();
                        }
                        displayTestReport();
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        progressBar.setVisibility(View.GONE);
                        retryButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                progressBar.setVisibility(View.VISIBLE);
                                emptyView.setVisibility(View.GONE);
                                loadAttempt();
                            }
                        });
                        if(exception.isUnauthenticated()) {
                            setEmptyText(R.string.testpress_authentication_failed,
                                    R.string.testpress_please_login,
                                    R.drawable.testpress_alert_warning);
                        } else if (exception.isNetworkError()) {
                            setEmptyText(R.string.testpress_network_error,
                                    R.string.testpress_no_internet_try_again,
                                    R.drawable.testpress_no_wifi);
                        } else {
                            setEmptyText(R.string.testpress_error_loading_questions,
                                    R.string.testpress_some_thing_went_wrong_try_again,
                                    R.drawable.testpress_alert_warning);
                        }
                    }
                });
    }

    private boolean canAttemptExam() {
        return (exam.getPausedAttemptsCount() == 0) && exam.canRetake() && exam.hasStarted() &&
                !exam.getDeviceAccessControl().equals("web");
    }

    private void startExam(boolean isPartial) {
        Intent intent = new Intent(getActivity(), TestActivity.class);
        intent.putExtra(TestActivity.PARAM_EXAM, exam);
        startActivityForResult(intent, CarouselFragment.TEST_TAKEN_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == TEST_TAKEN_REQUEST_CODE) && (Activity.RESULT_OK == resultCode)) {
            getActivity().setResult(resultCode);
            getActivity().finish();
        }
    }

    @Override
    public RetrofitCall[] getRetrofitCalls() {
        return new RetrofitCall[] { attemptsApiRequest };
    }

    protected void setEmptyText(final int title, final int description, int imageRes) {
        emptyView.setVisibility(View.VISIBLE);
        emptyTitleView.setText(title);
        emptyDescView.setText(description);
        emptyViewImage.setImageResource(imageRes);
    }

    public InstituteSettings getInstituteSettings() {
        //noinspection ConstantConditions
        InstituteSettings settings =
                TestpressSdk.getTestpressSession(getContext()).getInstituteSettings();
        return settings;
    }
}
