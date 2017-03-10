package in.testpress.exam.network;

import java.util.HashMap;
import java.util.Map;

import in.testpress.exam.models.Attempt;
import in.testpress.exam.models.AttemptItem;
import in.testpress.exam.models.Category;
import in.testpress.exam.models.CourseAttempt;
import in.testpress.exam.models.Exam;
import in.testpress.exam.models.Subject;
import in.testpress.exam.models.greendao.ReviewItem;
import in.testpress.model.TestpressApiResponse;
import in.testpress.network.RetrofitCall;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;

public interface ExamService {

    @GET(TestpressExamApiClient.EXAMS_LIST_PATH)
    RetrofitCall<TestpressApiResponse<Exam>> getExams(@QueryMap Map<String, Object> options);

    @GET(TestpressExamApiClient.EXAMS_LIST_PATH + "{exam_slug}")
    RetrofitCall<Exam> getExam(@Path(value = "exam_slug", encoded = true) String examSlug);

    @GET("/{mail_pdf_url}")
    RetrofitCall<Void> mailQuestionsPdf(
            @Path(value = "mail_pdf_url", encoded = true) String mailPdfUrlFrag);

    @PUT("/{mail_pdf_url}")
    RetrofitCall<Void> mailExplanationsPdf(
            @Path(value = "mail_pdf_url", encoded = true) String mailPdfUrlFrag);

    @GET("/{questions_url}")
    RetrofitCall<TestpressApiResponse<AttemptItem>> getQuestions(
            @Path(value = "questions_url", encoded = true) String questionsUrlFrag,
            @QueryMap Map<String, Object> options);

    @POST("/{attempts_url}")
    RetrofitCall<Attempt> createAttempt(
            @Path(value = "attempts_url", encoded = true) String attemptsUrlFrag);

    @POST("{attempt_url}")
    RetrofitCall<CourseAttempt> createContentAttempt(
            @Path(value = "attempt_url", encoded = true) String attemptUrl);

    @PUT("/{start_attempt_url}")
    RetrofitCall<Attempt> startAttempt(
            @Path(value = "start_attempt_url", encoded = true) String startAttemptUrlFrag);

    @PUT("/{answer_url}")
    RetrofitCall<AttemptItem> postAnswer(
            @Path(value = "answer_url", encoded = true) String answerUrlFrag,
            @Body HashMap<String, Object> arguments);

    @PUT("/{heartbeat_url}")
    RetrofitCall<Attempt> heartbeat(
            @Path(value = "heartbeat_url", encoded = true) String heartbeatUrlFrag);

    @PUT("/{end_exam_url}")
    RetrofitCall<Attempt> endExam(
            @Path(value = "end_exam_url", encoded = true) String endExamUrlFrag);

    @PUT("{end_exam_url}")
    RetrofitCall<CourseAttempt> endContentAttempt(
            @Path(value = "end_exam_url", encoded = true) String endExamUrlFrag);

    @GET("{attempts_url}")
    RetrofitCall<TestpressApiResponse<Attempt>> getAttempts(
            @Path(value = "attempts_url", encoded = true) String attemptsUrlFrag,
            @QueryMap Map<String, Object> options);

    @GET("{attempts_url}")
    RetrofitCall<TestpressApiResponse<CourseAttempt>> getContentAttempts(
            @Path(value = "attempts_url", encoded = true) String attemptsUrlFrag,
            @QueryMap Map<String, Object> options);

    @GET
    RetrofitCall<TestpressApiResponse<ReviewItem>> getReviewItems(
            @Url String reviewUrl,
            @QueryMap Map<String, Object> options);

    @GET(TestpressExamApiClient.CATEGORIES_PATH)
    RetrofitCall<TestpressApiResponse<Category>> getCategories(@QueryMap Map<String, Object> options);

    @GET("/{questions_count_url}")
    RetrofitCall<TestpressApiResponse<Subject>> getSubjects(
            @Path(value = "questions_count_url", encoded = true) String urlFrag,
            @QueryMap Map<String, Object> options);

}


