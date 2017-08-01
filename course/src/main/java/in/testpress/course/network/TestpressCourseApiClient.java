package in.testpress.course.network;

import android.content.Context;

import java.util.Map;

import in.testpress.core.TestpressSdk;
import in.testpress.course.models.Content;
import in.testpress.course.models.HtmlContent;
import in.testpress.course.models.Reputation;
import in.testpress.course.models.greendao.Chapter;
import in.testpress.course.models.greendao.Course;
import in.testpress.model.TestpressApiResponse;
import in.testpress.network.RetrofitCall;
import in.testpress.network.TestpressApiClient;

public class TestpressCourseApiClient extends TestpressApiClient {

    /**
     * Course List Url
     */
    public static final String COURSE_LIST_PATH =  "/api/v2.2/courses/";

    public static final String CHAPTERS_PATH =  "/chapters/";

    public static final String LEADERBOARD_PATH =  "/api/v2.2/leaderboard/";

    public static final String RANK_PATH =  "/api/v2.2/me/rank/";

    public static final String THREAD_PATH =  "/api/v2.2/me/threats/";

    public static final String TARGET_PATH =  "/api/v2.2/me/targets/";

    public TestpressCourseApiClient(final Context context) {
        super(context, checkTestpressSessionIsNull(TestpressSdk.getTestpressSession(context)));
    }
    
    public CourseService getExamService() {
        return retrofit.create(CourseService.class);
    }

    public RetrofitCall<TestpressApiResponse<Course>> getCourses(Map<String, Object> queryParams,
                                                                 String latestModifiedDate) {
        return getExamService().getCourses(queryParams, latestModifiedDate);
    }

    public RetrofitCall<TestpressApiResponse<Chapter>> getChapters(String courseId,
                                                                   Map<String, Object> queryParams,
                                                                   String latestModifiedDate) {
        return getExamService().getChapters(courseId, queryParams, latestModifiedDate);
    }

    public RetrofitCall<TestpressApiResponse<Content>> getContents(String chaptersUrlFrag,
                                                                   Map<String, Object> queryParams) {
        return getExamService().getContents(chaptersUrlFrag, queryParams);
    }

    public RetrofitCall<HtmlContent> getHtmlContent(String htmlContentUrlFrag) {
        return getExamService().getHtmlContent(htmlContentUrlFrag);
    }

    public RetrofitCall<Content> getContent(String contentUrl) {
        return getExamService().getContent(contentUrl);
    }

    public RetrofitCall<TestpressApiResponse<Reputation>> getLeaderboard(
            Map<String, Object> queryParams) {

        return getExamService().getLeaderboard(queryParams);
    }

    public RetrofitCall<TestpressApiResponse<Reputation>> getTargets() {
        return getExamService().getTargets();
    }

    public RetrofitCall<TestpressApiResponse<Reputation>> getThreads() {
        return getExamService().getThreads();
    }

    public RetrofitCall<Reputation> getMyRank() {
        return getExamService().getMyRank();
    }
}
