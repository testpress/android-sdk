package in.testpress.exam.api;

import java.util.HashMap;
import java.util.Map;

import in.testpress.exam.models.AttemptItem;
import in.testpress.exam.models.AudiencePollResponse;
import in.testpress.exam.models.Category;
import in.testpress.exam.models.Comment;
import in.testpress.exam.models.Permission;
import in.testpress.exam.models.ReportQuestionResponse;
import in.testpress.exam.models.Subject;
import in.testpress.exam.models.Vote;
import in.testpress.exam.network.NetworkAttemptSection;
import in.testpress.models.TestpressApiResponse;
import in.testpress.models.greendao.Attempt;
import in.testpress.models.greendao.AttemptSection;
import in.testpress.models.greendao.CourseAttempt;
import in.testpress.models.greendao.Exam;
import in.testpress.models.greendao.Language;
import in.testpress.models.greendao.ReviewItem;
import in.testpress.network.RetrofitCall;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;

import static in.testpress.exam.api.TestpressExamApiClient.ACCESS_CODES_PATH;
import static in.testpress.exam.api.TestpressExamApiClient.CONTENTS_PATH;
import static in.testpress.exam.api.TestpressExamApiClient.EXAMS_LIST_v2_3_PATH;
import static in.testpress.exam.api.TestpressExamApiClient.EXAMS_PATH;
import static in.testpress.exam.api.TestpressExamApiClient.LANGUAGES_PATH;
import static in.testpress.exam.api.TestpressExamApiClient.PERMISSIONS_PATH;
import static in.testpress.exam.api.TestpressExamApiClient.REPORTEES;
import static in.testpress.exam.api.TestpressExamApiClient.REPORT_QUESTION;

public interface ExamService {

    @GET(TestpressExamApiClient.EXAMS_LIST_PATH)
    RetrofitCall<TestpressApiResponse<Exam>> getExams(@QueryMap Map<String, Object> options);

    @GET(TestpressExamApiClient.EXAMS_LIST_PATH + "{exam_slug}")
    RetrofitCall<Exam> getExam(@Path(value = "exam_slug", encoded = true) String examSlug);

    @GET(ACCESS_CODES_PATH + "{access_code}" + EXAMS_PATH)
    RetrofitCall<TestpressApiResponse<Exam>> getExams(
            @Path(value = "access_code", encoded = true) String accessCode,
            @QueryMap Map<String, Object> options);

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
            @Path(value = "attempts_url", encoded = true) String attemptsUrlFrag,
            @Body Map<String, Object> options);

    @POST("{attempt_url}")
    RetrofitCall<CourseAttempt> createContentAttempt(
            @Path(value = "attempt_url", encoded = true) String attemptUrl,
            @Body Map<String, Object> options);

    @PUT("/{start_attempt_url}")
    RetrofitCall<Attempt> startAttempt(
            @Path(value = "start_attempt_url", encoded = true) String startAttemptUrlFrag);

    @GET("/{get_attempt_url}")
    RetrofitCall<Attempt> getAttempt(
            @Path(value = "get_attempt_url", encoded = true) String startAttemptUrlFrag);

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

    @PATCH("/{url_frag}")
    RetrofitCall<NetworkAttemptSection> updateSection(
            @Path(value = "url_frag", encoded = true) String urlFrag);

    @PUT("{end_exam_url}")
    RetrofitCall<CourseAttempt> endContentAttempt(
            @Path(value = "end_exam_url", encoded = true) String endExamUrlFrag);

    @GET("{attempts_url}")
    RetrofitCall<TestpressApiResponse<Attempt>> getAttempts(
            @Path(value = "attempts_url", encoded = true) String attemptsUrlFrag,
            @QueryMap Map<String, Object> options);

    @GET
    RetrofitCall<TestpressApiResponse<CourseAttempt>> getContentAttempts(@Url String attemptsUrl);

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

    @GET
    RetrofitCall<TestpressApiResponse<Comment>> getComments(@Url String commentsUrl,
                                                            @QueryMap Map<String, Object> options);

    @POST
    RetrofitCall<Comment> postComment(@Url String commentsUrl,
                                      @Body HashMap<String, String> arguments);

    @POST(TestpressExamApiClient.VOTES_PATH)
    RetrofitCall<Vote<Comment>> voteComment(@Body HashMap<String, Object> params);

    @DELETE(TestpressExamApiClient.VOTES_PATH + "{vote_id}/")
    RetrofitCall<String> deleteCommentVote(@Path(value = "vote_id") int id);

    @PUT(TestpressExamApiClient.VOTES_PATH + "{vote_id}/")
    RetrofitCall<Vote<Comment>> updateCommentVote(
            @Path(value = "vote_id") int id,
            @Body HashMap<String, Object> params);

    @GET(CONTENTS_PATH + "{content_id}" + PERMISSIONS_PATH)
    RetrofitCall<Permission> checkPermission(
            @Path(value = "content_id", encoded = true) long contentId);

    @GET(EXAMS_LIST_v2_3_PATH + "{exam_slug}" + LANGUAGES_PATH)
    RetrofitCall<TestpressApiResponse<Language>> getLanguages(
            @Path(value = "exam_slug", encoded = true) String examSlug);

    @GET(REPORT_QUESTION+"{question_id}"+REPORTEES)
    RetrofitCall<ReportQuestionResponse> getQuestionReports(
            @Path(value = "question_id",encoded = true) String questionId);

    @POST(REPORT_QUESTION+"{question_id}"+REPORTEES)
    RetrofitCall<ReportQuestionResponse.ReportQuestion> reportQuestion(
            @Path(value = "question_id",encoded = true) String questionId,
            @Body HashMap<String, Object> params);

    @GET("/{audience_poll_url}")
    RetrofitCall<AudiencePollResponse> getAudiencePoll(
            @Path(value = "audience_poll_url", encoded = true) String audiencePollUrl);

}


