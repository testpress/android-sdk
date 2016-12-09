package in.testpress.course.network;

import java.io.IOException;

import in.testpress.course.models.Course;
import in.testpress.model.TestpressApiResponse;
import in.testpress.network.BaseResourcePager;
import retrofit2.Response;

public class CoursePager extends BaseResourcePager<Course> {

    private TestpressCourseApiClient apiClient;

    public CoursePager(TestpressCourseApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    protected Object getId(Course resource) {
        return resource.getId();
    }

    @Override
    public Response<TestpressApiResponse<Course>> getItems(int page, int size) throws IOException {
        queryParams.put(TestpressCourseApiClient.PAGE, page);
        return apiClient.getCourses(queryParams).execute();
    }

}
