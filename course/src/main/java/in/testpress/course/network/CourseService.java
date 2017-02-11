package in.testpress.course.network;

import java.util.Map;

import in.testpress.course.models.Content;
import in.testpress.course.models.HtmlContent;
import in.testpress.course.models.greendao.Chapter;
import in.testpress.course.models.greendao.Course;
import in.testpress.model.TestpressApiResponse;
import in.testpress.network.RetrofitCall;

import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

import static in.testpress.course.network.TestpressCourseApiClient.CHAPTERS_PATH;
import static in.testpress.course.network.TestpressCourseApiClient.COURSE_LIST_PATH;

public interface CourseService {

    @GET(COURSE_LIST_PATH)
    RetrofitCall<TestpressApiResponse<Course>> getCourses(
            @QueryMap Map<String, Object> queryParams,
            @Header("If-Modified-Since") String latestModifiedDate);

    @GET(COURSE_LIST_PATH + "{course_id}"+ CHAPTERS_PATH)
    RetrofitCall<TestpressApiResponse<Chapter>> getChapters(
            @Path(value = "course_id", encoded = true) String courseId,
            @QueryMap Map<String, Object> queryParams,
            @Header("If-Modified-Since") String latestModifiedDate);

    @GET("{contents_url}")
    RetrofitCall<TestpressApiResponse<Content>> getContents(
            @Path(value = "contents_url", encoded = true) String contentsUrlFrag,
            @QueryMap Map<String, Object> queryParams);

    @GET("{html_content_url}")
    RetrofitCall<HtmlContent> getHtmlContent(
            @Path(value = "html_content_url", encoded = true) String htmlContentUrlFrag);

    @GET("{content_url}")
    RetrofitCall<Content> getContent(
            @Path(value = "content_url", encoded = true) String contentUrl);

}