package in.testpress.course.network;

import java.util.Map;

import in.testpress.course.models.Course;
import in.testpress.model.TestpressApiResponse;
import in.testpress.network.RetrofitCall;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

public interface CourseService {

    @GET(TestpressCourseApiClient.COURSE_LIST_PATH)
    RetrofitCall<TestpressApiResponse<Course>> getCourses(@QueryMap Map<String, Object> queryParams);

}