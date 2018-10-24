package in.testpress.course.network;

import java.util.Map;

import in.testpress.course.models.Reputation;
import in.testpress.models.TestpressApiResponse;
import in.testpress.models.greendao.Chapter;
import in.testpress.models.greendao.Content;
import in.testpress.models.greendao.Course;
import in.testpress.models.greendao.CourseAttempt;
import in.testpress.models.greendao.VideoAttempt;
import in.testpress.network.RetrofitCall;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

import static in.testpress.course.network.TestpressCourseApiClient.ATTEMPTS_PATH;
import static in.testpress.course.network.TestpressCourseApiClient.CHAPTERS_PATH;
import static in.testpress.course.network.TestpressCourseApiClient.CHAPTERS_PATH_V2_4;
import static in.testpress.course.network.TestpressCourseApiClient.CONTENTS;
import static in.testpress.course.network.TestpressCourseApiClient.CONTENTS_PATH;
import static in.testpress.course.network.TestpressCourseApiClient.COURSE_LIST_PATH;
import static in.testpress.course.network.TestpressCourseApiClient.LEADERBOARD_PATH;
import static in.testpress.course.network.TestpressCourseApiClient.RANK_PATH;
import static in.testpress.course.network.TestpressCourseApiClient.TARGET_PATH;
import static in.testpress.course.network.TestpressCourseApiClient.THREAD_PATH;
import static in.testpress.course.network.TestpressCourseApiClient.USER_VIDEOS_PATH;

public interface CourseService {

    @GET(COURSE_LIST_PATH)
    RetrofitCall<TestpressApiResponse<Course>> getCourses(
            @QueryMap Map<String, Object> queryParams,
            @Header("If-Modified-Since") String latestModifiedDate);

    @GET(COURSE_LIST_PATH + "{course_id}")
    RetrofitCall<Course> getCourse(@Path(value = "course_id", encoded = true) long courseId);

    @GET(COURSE_LIST_PATH + "{course_id}"+ CHAPTERS_PATH)
    RetrofitCall<TestpressApiResponse<Chapter>> getChapters(
            @Path(value = "course_id", encoded = true) long courseId,
            @QueryMap Map<String, Object> queryParams,
            @Header("If-Modified-Since") String latestModifiedDate);

    @GET(CHAPTERS_PATH_V2_4 + "{chapter_slug}")
    RetrofitCall<Chapter> getChapter(
            @Path(value = "chapter_slug", encoded = true) String chapterSlug);

    @GET(COURSE_LIST_PATH + "{course_id}"+ CONTENTS)
    RetrofitCall<TestpressApiResponse<Content>> getContents(
            @Path(value = "course_id", encoded = true) long courseId,
            @QueryMap Map<String, Object> queryParams);

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

}