package in.testpress.exam.network;

import java.util.HashMap;
import java.util.Map;

import in.testpress.exam.models.Attempt;
import in.testpress.exam.models.AttemptItem;
import in.testpress.exam.models.Exam;
import in.testpress.exam.models.ExamCourse;
import in.testpress.exam.models.ReviewItem;
import in.testpress.exam.models.TestpressApiResponse;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.QueryMap;

public interface ExamService {

    @GET(TestpressExamApiClient.EXAMS_LIST_PATH)
    TestpressApiResponse<Exam> getExams(@QueryMap Map<String, Object> options,
                                        @Header("Authorization") String authorization);

    @GET(TestpressExamApiClient.EXAM_COURSES_PATH)
    TestpressApiResponse<ExamCourse> getExamCourses(@Header("Authorization") String authorization);

    @GET("/{mail_pdf_url}")
    Response mailQuestionsPdf(@Path(value = "mail_pdf_url", encode = false) String mailPdfUrlFrag,
                              @Header("Authorization") String authorization);

    @PUT("/{mail_pdf_url}")
    Void mailExplanationsPdf(@Path(value = "mail_pdf_url", encode = false) String mailPdfUrlFrag,
                 @Header("Authorization") String authorization);

    @GET("/{questions_url}")
    TestpressApiResponse<AttemptItem> getQuestions(
            @Path(value = "questions_url", encode = false) String questionsUrlFrag,
            @Header("Authorization") String authorization);


    @POST("/{attempts_url}")
    Attempt createAttempt(@Path(value = "attempts_url", encode = false) String attemptsUrlFrag,
                          @Header("Authorization") String authorization);

    @PUT("/{start_attempt_url}")
    Attempt startAttempt(@Path(value = "start_attempt_url", encode = false) String startAttemptUrlFrag,
                         @Header("Authorization") String authorization);

    @PUT("/{answer_url}")
    AttemptItem postAnswer(@Path(value = "answer_url", encode = false) String answerUrlFrag,
                           @Header("Authorization") String authorization,
                           @Body HashMap<String, Object> arguments);

    @PUT("/{heartbeat_url}")
    Attempt heartbeat(@Path(value = "heartbeat_url", encode = false) String heartbeatUrlFrag,
                      @Header("Authorization") String authorization);

    @PUT("/{end_exam_url}")
    Attempt endExam(@Path(value = "end_exam_url", encode = false) String endExamUrlFrag,
                    @Header("Authorization") String authorization);

    @GET("/{attempts_url}")
    TestpressApiResponse<Attempt> getAttempts(
            @Path(value = "attempts_url", encode = false) String attemptsUrlFrag,
            @QueryMap Map<String, Object> options, @Header("Authorization") String authorization);

    @GET("/{review_url}")
    TestpressApiResponse<ReviewItem> getReviewItems(
            @Path(value = "review_url", encode = false) String reviewUrlFrag,
            @QueryMap Map<String, Object> options, @Header("Authorization") String authorization);

}


