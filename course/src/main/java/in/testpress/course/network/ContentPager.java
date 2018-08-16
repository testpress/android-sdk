package in.testpress.course.network;

import in.testpress.models.TestpressApiResponse;
import in.testpress.models.greendao.Content;
import in.testpress.network.BaseResourcePager;
import in.testpress.network.RetrofitCall;

public class ContentPager extends BaseResourcePager<Content> {

    private TestpressCourseApiClient apiClient;
    private long courseId;

    public ContentPager(long courseId, TestpressCourseApiClient apiClient) {
        this.courseId = courseId;
        this.apiClient = apiClient;
    }

    @Override
    protected Object getId(Content resource) {
        return resource.getId();
    }

    @Override
    public RetrofitCall<TestpressApiResponse<Content>> getItems(int page, int size) {
        queryParams.put(TestpressCourseApiClient.PAGE, page);
        return apiClient.getContents(courseId, queryParams);
    }

}
