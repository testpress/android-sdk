package in.testpress.course.network;

import java.io.IOException;

import in.testpress.models.TestpressApiResponse;
import in.testpress.models.greendao.Content;
import in.testpress.network.BaseResourcePager;
import retrofit2.Response;

public class ContentPager extends BaseResourcePager<Content> {

    private TestpressCourseApiClient apiClient;
    private String contentsUrlFrag;

    public ContentPager(String contentsUrlFrag, TestpressCourseApiClient apiClient) {
        this.contentsUrlFrag = contentsUrlFrag;
        this.apiClient = apiClient;
    }

    @Override
    protected Object getId(Content resource) {
        return resource.getId();
    }

    @Override
    public Response<TestpressApiResponse<Content>> getItems(int page, int size) throws IOException {
        queryParams.put(TestpressCourseApiClient.PAGE, page);
        return apiClient.getContents(contentsUrlFrag, queryParams).execute();
    }

}
