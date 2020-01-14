package in.testpress.course.network;

import java.io.IOException;
import java.util.List;

import in.testpress.models.TestpressApiResponse;
import in.testpress.models.greendao.Content;
import in.testpress.v2_4.BaseResourcePager;
import in.testpress.v2_4.models.ApiResponse;
import in.testpress.v2_4.models.ContentsListResponse;
import retrofit2.Response;

public class ContentPager extends BaseResourcePager<ContentsListResponse, Content> {

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
    public Response<ApiResponse<ContentsListResponse>> getResponse(
            int page, int size) throws IOException {
        queryParams.put(TestpressCourseApiClient.PAGE, page);
        return apiClient.getContents(contentsUrlFrag, queryParams).execute();
    }

    @Override
    public List<Content> getItems(ContentsListResponse resultResponse) {
        return resultResponse.getContents();
    }
}
