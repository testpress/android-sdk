package in.testpress.course.network;

import in.testpress.models.TestpressApiResponse;
import in.testpress.models.greendao.CourseCredit;
import in.testpress.network.BaseDatabaseModelPager;
import in.testpress.network.RetrofitCall;
import in.testpress.network.TestpressApiClient;

public class CourseCreditPager extends BaseDatabaseModelPager<CourseCredit> {

    private TestpressCourseApiClient apiClient;

    public CourseCreditPager(TestpressCourseApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    protected Object getId(CourseCredit resource) {
        return resource.getId();
    }

    @Override
    public RetrofitCall<TestpressApiResponse<CourseCredit>> getItems(int page, int size) {
        queryParams.put(TestpressApiClient.PAGE, page);
        queryParams.put(TestpressCourseApiClient.PAGE_SIZE, size);
        return apiClient.getCoursesCredit(queryParams);
    }
}
