package in.testpress.course.network;

import java.io.IOException;

import in.testpress.course.models.Chapter;
import in.testpress.model.TestpressApiResponse;
import in.testpress.network.BaseResourcePager;
import in.testpress.network.TestpressApiClient;
import retrofit2.Response;

public class ChapterPager extends BaseResourcePager<Chapter> {

    private TestpressCourseApiClient apiClient;
    private String chaptersUrlFrag;
    private String parentId;

    public ChapterPager(String chaptersUrlFrag, String parentId, TestpressCourseApiClient apiClient) {
        this.chaptersUrlFrag = chaptersUrlFrag;
        this.parentId = parentId;
        this.apiClient = apiClient;
    }

    @Override
    protected Object getId(Chapter resource) {
        return resource.getId();
    }

    @Override
    public Response<TestpressApiResponse<Chapter>> getItems(int page, int size) throws IOException {
        queryParams.put(TestpressApiClient.PAGE, page);
        queryParams.put(TestpressApiClient.PARENT, parentId);
        return apiClient.getChapters(chaptersUrlFrag, queryParams).execute();
    }

}
