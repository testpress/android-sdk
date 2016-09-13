package in.testpress.exam.network;

import android.content.Context;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import in.testpress.core.TestpressSdk;
import in.testpress.core.TestpressSession;
import in.testpress.exam.models.Attempt;
import in.testpress.exam.models.AttemptItem;
import in.testpress.exam.models.Exam;
import in.testpress.exam.models.ReviewItem;
import in.testpress.exam.models.TestpressApiResponse;
import in.testpress.network.ErrorHandlingCallAdapterFactory;
import in.testpress.network.RetrofitCall;
import in.testpress.util.UserAgentProvider;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TestpressExamApiClient {

    private final Retrofit retrofit;
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

    public TestpressExamApiClient(final Context context) {
        testpressSession = TestpressSdk.getTestpressSession(context);
        if (testpressSession == null) {
            throw new IllegalStateException("TestpressSession must not be null.");
        }
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

        // Set headers for all network requests
        Interceptor interceptor = new Interceptor() {
            @Override
            public okhttp3.Response intercept(Interceptor.Chain chain) throws IOException {
                Request newRequest = chain.request().newBuilder()
                        .addHeader("User-Agent", UserAgentProvider.get(context))
                        .addHeader("Authorization", "JWT " + testpressSession.getToken())
                        .build();
                return chain.proceed(newRequest);
            }
        };
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.connectTimeout(2, TimeUnit.MINUTES).readTimeout(2, TimeUnit.MINUTES);
        httpClient.addInterceptor(interceptor);

        // Set log level
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        httpClient.addInterceptor(httpLoggingInterceptor);

        retrofit = new Retrofit.Builder()
                .baseUrl(testpressSession.getBaseUrl())
                .addCallAdapterFactory(new ErrorHandlingCallAdapterFactory())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(httpClient.build())
                .build();
    }
    
    public ExamService getExamService() {
        return retrofit.create(ExamService.class);
    }

    public RetrofitCall<TestpressApiResponse<Exam>> getExams(Map<String, Object> queryParams) {
        return getExamService().getExams(queryParams);
    }

    public RetrofitCall<Void> mailQuestionsPdf(String mailPdfUrlFrag) {
        return getExamService().mailQuestionsPdf(mailPdfUrlFrag);
    }

    public RetrofitCall<Void> mailExplanationsPdf(String mailPdfUrlFrag) {
        return getExamService().mailExplanationsPdf(mailPdfUrlFrag);
    }

    public RetrofitCall<Attempt> createAttempt(String attemptsUrlFrag) {
        return getExamService().createAttempt(attemptsUrlFrag);
    }

    public RetrofitCall<Attempt> startAttempt(String startAttemptUrlFrag) {
        return getExamService().startAttempt(startAttemptUrlFrag);
    }

    public RetrofitCall<Attempt> endAttempt(String endAttemptUrlFrag) {
        return getExamService().endExam(endAttemptUrlFrag);
    }

    public RetrofitCall<TestpressApiResponse<AttemptItem>> getQuestions(
            String questionsUrlFrag, Map<String, Object> queryParams) {
        return getExamService().getQuestions(questionsUrlFrag, queryParams);
    }

    public RetrofitCall<TestpressApiResponse<Attempt>> getAttempts(String urlFrag,
                                                                   Map<String, Object> queryParams) {
        return getExamService().getAttempts(urlFrag, queryParams);
    }

    public RetrofitCall<TestpressApiResponse<ReviewItem>> getReviewItems(
            String urlFrag, Map<String, Object> queryParams) {
        return getExamService().getReviewItems(urlFrag, queryParams);
    }

    public RetrofitCall<AttemptItem> postAnswer(String answerUrlFrag, List<Integer> savedAnswers,
                                                Boolean review) {
        HashMap<String, Object> answer = new HashMap<String, Object>();
        answer.put("selected_answers", savedAnswers);
        answer.put("review", review);
        return getExamService().postAnswer(answerUrlFrag, answer);
    }

    public RetrofitCall<Attempt> heartbeat(String heartbeatUrlFrag) {
        return getExamService().heartbeat(heartbeatUrlFrag);
    }

    public RetrofitCall<Attempt> endExam(String endExamUrlFrag) {
        return getExamService().endExam(endExamUrlFrag);
    }

}
