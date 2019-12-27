package in.testpress.course.network;

import java.text.ParseException;

import in.testpress.models.greendao.Course;
import in.testpress.models.TestpressApiResponse;
import in.testpress.network.BaseDatabaseModelPager;
import in.testpress.network.RetrofitCall;

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
    public RetrofitCall<TestpressApiResponse<Course>> getItems(int page, int size) {
        queryParams.put(TestpressCourseApiClient.PAGE, page);
        return apiClient.getCourses(queryParams, latestModifiedDate);
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
