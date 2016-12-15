package in.testpress.course.network;

import android.content.Context;

import java.util.Map;

import in.testpress.core.TestpressSdk;
import in.testpress.course.models.Chapter;
import in.testpress.course.models.Content;
import in.testpress.course.models.Course;
import in.testpress.course.models.HtmlContent;
import in.testpress.model.TestpressApiResponse;
import in.testpress.network.RetrofitCall;
import in.testpress.network.TestpressApiClient;

public class TestpressCourseApiClient extends TestpressApiClient {

    /**
     * Course List Url
     */
    public static final String COURSE_LIST_PATH =  "/api/v2.2/courses/";

    public TestpressCourseApiClient(final Context context) {
        super(context, checkTestpressSessionIsNull(TestpressSdk.getTestpressSession(context)));
    }
    
    public CourseService getExamService() {
        return retrofit.create(CourseService.class);
    }

    public RetrofitCall<TestpressApiResponse<Course>> getCourses(Map<String, Object> queryParams) {
        return getExamService().getCourses(queryParams);
    }

    public RetrofitCall<TestpressApiResponse<Chapter>> getChapters(String chaptersUrlFrag,
                                                                   Map<String, Object> queryParams) {
        return getExamService().getChapters(chaptersUrlFrag, queryParams);
    }

    public RetrofitCall<TestpressApiResponse<Content>> getContents(String chaptersUrlFrag,
                                                                   Map<String, Object> queryParams) {
        return getExamService().getContents(chaptersUrlFrag, queryParams);
    }

    public RetrofitCall<HtmlContent> getHtmlContent(String htmlContentUrlFrag) {
        return getExamService().getHtmlContent(htmlContentUrlFrag);
    }

}
