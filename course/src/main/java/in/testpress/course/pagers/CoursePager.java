package in.testpress.course.pagers;

import java.io.IOException;
import java.text.ParseException;

import in.testpress.course.api.TestpressCourseApiClient;
import in.testpress.models.greendao.Course;
import in.testpress.models.TestpressApiResponse;
import in.testpress.network.BaseDatabaseModelPager;
import retrofit2.Response;

public class CoursePager extends BaseDatabaseModelPager<Course> {

    private TestpressCourseApiClient apiClient;

    public CoursePager(TestpressCourseApiClient apiClient) {
        super();
        this.apiClient = apiClient;
    }

    @Override
    protected Object getId(Course resource) {
        return resource.getId();
    }

    @Override
    public Response<TestpressApiResponse<Course>> getItems(int page, int size) throws IOException {
        queryParams.put(TestpressCourseApiClient.PAGE, page);
        return apiClient.getCourses(queryParams, latestModifiedDate).execute();
    }

    @Override
    protected Course register(Course course) {
        if (course != null && course.getModified() != null && !course.getModified().isEmpty()) {
            try {
                course.setModifiedDate(simpleDateFormat.parse(course.getModified()).getTime());
            } catch (ParseException e) {
                e.printStackTrace();
                return null;
            }
        }
        return course;
    }

}
