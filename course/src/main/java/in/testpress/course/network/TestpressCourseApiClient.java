package in.testpress.course.network;

import android.content.Context;

import java.util.Map;

import in.testpress.core.TestpressSdk;
import in.testpress.course.models.Reputation;
import in.testpress.models.TestpressApiResponse;
import in.testpress.models.greendao.Chapter;
import in.testpress.models.greendao.Content;
import in.testpress.models.greendao.Course;
import in.testpress.models.greendao.CourseAttempt;
import in.testpress.models.greendao.VideoAttempt;
import in.testpress.network.RetrofitCall;
import in.testpress.network.TestpressApiClient;

public class TestpressCourseApiClient extends TestpressApiClient {

    /**
     * Query Params
     */
    public static final String COURSE_ID = "course_id";
    public static final String LAST_POSITION =  "last_position";
    public static final String TIME_RANGES =  "time_ranges";

    public static final String EMBED_CODE =  "embed_code";

    /**
     * Course List Url
     */
    public static final String COURSE_LIST_PATH =  "/api/v2.4/courses/";

    public static final String CHAPTERS_PATH =  "/chapters/";

    public static final String CONTENTS_PATH =  "/api/v2.3/contents/";

    public static final String CONTENTS =  "/contents/";

    public static final String CONTENTS_PATH_V2_4 =  "/api/v2.4/contents/";
    public static final String CHAPTERS_PATH_V2_4 =  "/api/v2.4/chapters/";

    public static final String ATTEMPTS_PATH =  "/attempts/";

    public static final String USER_VIDEOS_PATH =  "/api/v2.3/user_videos/";

    public static final String LEADERBOARD_PATH =  "/api/v2.2/leaderboard/";

    public static final String RANK_PATH =  "/api/v2.2/me/rank/";

    public static final String THREAD_PATH =  "/api/v2.2/me/threats/";

    public static final String TARGET_PATH =  "/api/v2.2/me/targets/";

    public static final String EMBED_DOMAIN_RESTRICTED_VIDEO_PATH =  "embed/domain-restricted-video/";

    public TestpressCourseApiClient(final Context context) {
        super(context, checkTestpressSessionIsNull(TestpressSdk.getTestpressSession(context)));
    }
    
    public CourseService getCourseService() {
        return retrofit.create(CourseService.class);
    }

    public RetrofitCall<TestpressApiResponse<Course>> getCourses(Map<String, Object> queryParams,
                                                                 String latestModifiedDate) {
        return getCourseService().getCourses(queryParams, latestModifiedDate);
    }

    public RetrofitCall<Course> getCourse(long courseId) {
        return getCourseService().getCourse(courseId);
    }

    public RetrofitCall<TestpressApiResponse<Chapter>> getChapters(long courseId,
                                                                   Map<String, Object> queryParams,
                                                                   String latestModifiedDate) {
        return getCourseService().getChapters(courseId, queryParams, latestModifiedDate);
    }

    public RetrofitCall<Chapter> getChapter(String chapterSlug) {
        return getCourseService().getChapter(chapterSlug);
    }

    public RetrofitCall<TestpressApiResponse<Content>> getContents(long courseId,
                                                                   Map<String, Object> queryParams) {
        return getCourseService().getContents(courseId, queryParams);
    }

    public RetrofitCall<Content> getContent(String contentUrl) {
        return getCourseService().getContent(contentUrl);
    }

    public RetrofitCall<CourseAttempt> createContentAttempt(long contentId) {
        return getCourseService().createContentAttempt(contentId);
    }

    public RetrofitCall<VideoAttempt> updateVideoAttempt(long videoAttemptId,
                                                         Map<String, Object> parameters) {

        return getCourseService().updateVideoAttempt(videoAttemptId, parameters);
    }

    public RetrofitCall<TestpressApiResponse<Reputation>> getLeaderboard(
            Map<String, Object> queryParams) {

        return getCourseService().getLeaderboard(queryParams);
    }

    public RetrofitCall<TestpressApiResponse<Reputation>> getTargets() {
        return getCourseService().getTargets();
    }

    public RetrofitCall<TestpressApiResponse<Reputation>> getThreads() {
        return getCourseService().getThreads();
    }

    public RetrofitCall<Reputation> getMyRank() {
        return getCourseService().getMyRank();
    }
}
