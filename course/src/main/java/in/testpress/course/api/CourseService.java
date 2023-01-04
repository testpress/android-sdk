package in.testpress.course.api;

import java.util.Map;

import in.testpress.models.greendao.CourseAttempt;
import in.testpress.models.greendao.HtmlContent;
import in.testpress.course.models.Reputation;
import in.testpress.models.greendao.Chapter;
import in.testpress.models.greendao.Content;
import in.testpress.models.greendao.Course;
import in.testpress.models.TestpressApiResponse;
import in.testpress.models.greendao.VideoAttempt;
import in.testpress.network.RetrofitCall;

import in.testpress.v2_4.models.ApiResponse;
import in.testpress.v2_4.models.ContentsListResponse;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

import static in.testpress.course.api.TestpressCourseApiClient.ATTEMPTS_PATH;
import static in.testpress.course.api.TestpressCourseApiClient.CHAPTERS_PATH;
import static in.testpress.course.api.TestpressCourseApiClient.CONTENTS_PATH;
import static in.testpress.course.api.TestpressCourseApiClient.COURSE_LIST_PATH;
import static in.testpress.course.api.TestpressCourseApiClient.COURSE_PATH_v2_5;
import static in.testpress.course.api.TestpressCourseApiClient.LEADERBOARD_PATH;
import static in.testpress.course.api.TestpressCourseApiClient.RANK_PATH;
import static in.testpress.course.api.TestpressCourseApiClient.RUNNING_CONTENTS_PATH;
import static in.testpress.course.api.TestpressCourseApiClient.TARGET_PATH;
import static in.testpress.course.api.TestpressCourseApiClient.THREAD_PATH;
import static in.testpress.course.api.TestpressCourseApiClient.USER_VIDEOS_PATH;

public interface CourseService {

    @GET(COURSE_LIST_PATH)
    RetrofitCall<TestpressApiResponse<Course>> getCourses(
            @QueryMap Map<String, Object> queryParams,
            @Header("If-Modified-Since") String latestModifiedDate);

    @GET(COURSE_LIST_PATH + "{course_id}")
    RetrofitCall<Course> getCourse(@Path(value = "course_id", encoded = true) String courseId);


    @GET(COURSE_LIST_PATH + "{course_id}"+ CHAPTERS_PATH)
    RetrofitCall<TestpressApiResponse<Chapter>> getChapters(
            @Path(value = "course_id", encoded = true) String courseId,
            @QueryMap Map<String, Object> queryParams,
            @Header("If-Modified-Since") String latestModifiedDate);

    @GET("{chapter_url}")
    RetrofitCall<Chapter> getChapter(@Path(value = "chapter_url", encoded = true) String chapterUrl);

    @GET("{contents_url}")
    RetrofitCall<ApiResponse<ContentsListResponse>> getContents(
            @Path(value = "contents_url", encoded = true) String contentsUrlFrag,
            @QueryMap Map<String, Object> queryParams);

    @GET("{html_content_url}")
    RetrofitCall<HtmlContent> getHtmlContent(
            @Path(value = "html_content_url", encoded = true) String htmlContentUrlFrag);

    @GET("{content_url}")
    RetrofitCall<Content> getContent(
            @Path(value = "content_url", encoded = true) String contentUrl);

    @POST(CONTENTS_PATH + "{content_id}" + ATTEMPTS_PATH)
    RetrofitCall<CourseAttempt> createContentAttempt(
            @Path(value = "content_id", encoded = true) long contentId);

    @PUT(USER_VIDEOS_PATH + "{video_attempt_id}/")
    RetrofitCall<VideoAttempt> updateVideoAttempt(
            @Path(value = "video_attempt_id", encoded = true) long videoAttemptId,
            @Body Map<String, Object> parameters);

    @GET(LEADERBOARD_PATH)
    RetrofitCall<TestpressApiResponse<Reputation>> getLeaderboard(
            @QueryMap Map<String, Object> queryParams);

    @GET(TARGET_PATH)
    RetrofitCall<TestpressApiResponse<Reputation>> getTargets();

    @GET(THREAD_PATH)
    RetrofitCall<TestpressApiResponse<Reputation>> getThreads();

    @GET(RANK_PATH)
    RetrofitCall<Reputation> getMyRank();

    @GET(COURSE_PATH_v2_5+"{course_id}"+RUNNING_CONTENTS_PATH)
    RetrofitCall<ApiResponse<ContentsListResponse>> getRunningContents(
            @Path(value = "course_id", encoded = true) String courseId
    );

}