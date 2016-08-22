package in.testpress.exam.network;

import android.content.Context;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.testpress.core.TestpressSdk;
import in.testpress.core.TestpressSession;
import in.testpress.exam.models.Attempt;
import in.testpress.exam.models.AttemptItem;
import in.testpress.exam.models.Exam;
import in.testpress.exam.models.ReviewItem;
import in.testpress.exam.models.TestpressApiResponse;
import retrofit.RestAdapter;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;

public class TestpressExamApiClient {

    private final RestAdapter restAdapter;
    private final TestpressSession testpressSession;

    /**
     * Exams List URL
     */
    public static final String EXAMS_LIST_PATH =  "/api/v2.2/exams/";

    public static final String MAIL_PDF_PATH =  "pdf/";
    public static final String MAIL_PDF_QUESTIONS_PATH =  "pdf-questions/";

    /**
     * End Exam URL
     */
    public static final String END_EXAM_PATH =  "end/";

    /**
     * Query Params
     */
    public static final String SEARCH_QUERY = "q";
    public static final String STATE = "state";
    public static final String PAGE = "page";

    public TestpressExamApiClient(Context context) {
        testpressSession = TestpressSdk.getTestpressSession(context);
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

        restAdapter = new RestAdapter.Builder()
                .setEndpoint(testpressSession.getBaseUrl())
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setConverter(new GsonConverter(gson))
                .build();
    }

    private String getAuthToken() {
        return "JWT " + testpressSession.getToken();
    }

    public ExamService getExamService() {
        return restAdapter.create(ExamService.class);
    }

    public TestpressApiResponse<Exam> getExams(Map<String, Object> queryParams) {
        return getExamService().getExams(queryParams, getAuthToken());
    }

    public Response mailQuestionsPdf(String mailPdfUrlFrag) {
        return getExamService().mailQuestionsPdf(mailPdfUrlFrag, getAuthToken());
    }

    public Void mailExplanationsPdf(String mailPdfUrlFrag) {
        return getExamService().mailExplanationsPdf(mailPdfUrlFrag, getAuthToken());
    }

    public Attempt createAttempt(String attemptsUrlFrag) {
        return getExamService().createAttempt(attemptsUrlFrag, getAuthToken());
    }

    public Attempt startAttempt(String startAttemptUrlFrag) {
        return getExamService().startAttempt(startAttemptUrlFrag, getAuthToken());
    }

    public Attempt endAttempt(String endAttemptUrlFrag) {
        return getExamService().endExam(endAttemptUrlFrag, getAuthToken());
    }

    public TestpressApiResponse<AttemptItem> getQuestions(String questionsUrlFrag) {
        return getExamService().getQuestions(questionsUrlFrag, getAuthToken());
    }

    public AttemptItem postAnswer(String answerUrlFrag, List<Integer> savedAnswers, Boolean review) {
        HashMap<String, Object> answer = new HashMap<String, Object>();
        answer.put("selected_answers", savedAnswers);
        answer.put("review", review);
        return getExamService().postAnswer(answerUrlFrag, getAuthToken(), answer);
    }

    public Attempt heartbeat(String heartbeatUrlFrag) {
        return getExamService().heartbeat(heartbeatUrlFrag, getAuthToken());
    }

    public Attempt endExam(String endExamUrlFrag) {
        return getExamService().endExam(endExamUrlFrag, getAuthToken());
    }

    public TestpressApiResponse<Attempt> getAttempts(String urlFrag, Map<String, Object> queryParams) {
        return getExamService().getAttempts(urlFrag, queryParams, getAuthToken());
    }

    public TestpressApiResponse<ReviewItem> getReviewItems(String urlFrag, Map<String, Object> queryParams) {
        return getExamService().getReviewItems(urlFrag, queryParams, getAuthToken());
    }

}
