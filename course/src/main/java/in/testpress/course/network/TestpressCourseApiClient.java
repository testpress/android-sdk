package in.testpress.course.network;

import android.content.Context;

import java.util.Map;

import in.testpress.core.TestpressSdk;
import in.testpress.course.models.Course;
import in.testpress.model.TestpressApiResponse;
import in.testpress.network.RetrofitCall;
import in.testpress.network.TestpressApiClient;

public class TestpressCourseApiClient extends TestpressApiClient {

    /**
     * Course List Url
     */
    public static final String COURSE_LIST_PATH =  "/api/v2.2/courses/";

    /**
     * Query Params
     */
    public static final String PAGE = "page";

    public TestpressCourseApiClient(final Context context) {
        super(context, checkTestpressSessionIsNull(TestpressSdk.getTestpressSession(context)));
    }
    
    public CourseService getExamService() {
        return retrofit.create(CourseService.class);
    }

    public RetrofitCall<TestpressApiResponse<Course>> getCourses(Map<String, Object> queryParams) {
        return getExamService().getCourses(queryParams);
    }

}
