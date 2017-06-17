package in.testpress.course.network;

import java.util.Map;

import in.testpress.course.models.Chapter;
import in.testpress.course.models.Content;
import in.testpress.course.models.Course;
import in.testpress.model.TestpressApiResponse;
import in.testpress.network.RetrofitCall;

import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

public interface CourseService {

    @GET(TestpressCourseApiClient.COURSE_LIST_PATH)
    RetrofitCall<TestpressApiResponse<Course>> getCourses(@QueryMap Map<String, Object> queryParams);

    @GET("{chapters_url}")
    RetrofitCall<TestpressApiResponse<Chapter>> getChapters(
            @Path(value = "chapters_url", encoded = true) String chaptersUrlFrag,
            @QueryMap Map<String, Object> queryParams);

    @GET("{contents_url}")
    RetrofitCall<TestpressApiResponse<Content>> getContents(
            @Path(value = "contents_url", encoded = true) String contentsUrlFrag,
            @QueryMap Map<String, Object> queryParams);

}