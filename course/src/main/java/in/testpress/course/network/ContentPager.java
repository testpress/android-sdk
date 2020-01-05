package in.testpress.course.network;

import java.io.IOException;
import java.util.List;

import in.testpress.core.TestpressRetrofitRequest;
import in.testpress.models.TestpressApiResponse;
import in.testpress.models.greendao.Content;
import in.testpress.models.greendao.ContentDao;
import in.testpress.network.BaseResourcePager;
import in.testpress.network.RetrofitCall;
import retrofit2.Response;

import static in.testpress.network.TestpressApiClient.MODIFIED_SINCE;
import static in.testpress.network.TestpressApiClient.UNFILTERED;

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

    public TestpressRetrofitRequest<Content> getRetrofitCall() {
        return new TestpressRetrofitRequest<Content>() {
            @Override
            public RetrofitCall<TestpressApiResponse<Content>> getRetrofitCall(
                    int page, int size) {
                queryParams.put(TestpressCourseApiClient.PAGE, page);
                queryParams.put(UNFILTERED, true);
                return apiClient.getContents(courseId, queryParams);
            }
        };
    }

    public void setLastModifiedQueryParams(ContentDao dao) {
        List<Content> contents = dao.queryBuilder()
                .where(ContentDao.Properties.CourseId.eq(courseId))
                .orderDesc(ContentDao.Properties.ModifiedDate).list();

        if (!contents.isEmpty()) {
            Content content = contents.get(0);
            this.setQueryParams(MODIFIED_SINCE, content.getModified());
            this.setQueryParams(UNFILTERED, true);
        }
    }

    @Override
    public Response<TestpressApiResponse<Content>> getItems(int page, int size) throws IOException {
        queryParams.put(TestpressCourseApiClient.PAGE, page);
        queryParams.put(UNFILTERED, true);
        return apiClient.getContents(courseId, queryParams).execute();
    }

}
