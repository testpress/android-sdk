package in.testpress.exam.pager;

import java.io.IOException;

import in.testpress.exam.models.Category;
import in.testpress.exam.network.TestpressExamApiClient;
import in.testpress.models.TestpressApiResponse;
import in.testpress.network.BaseResourcePager;
import retrofit2.Response;

public class CategoryPager extends BaseResourcePager<Category> {

    private TestpressExamApiClient apiClient;
    private String parentId;

    public CategoryPager(String parentId, TestpressExamApiClient apiClient) {
        this.apiClient = apiClient;
        this.parentId = parentId;
    }

    @Override
    protected Object getId(Category resource) {
        return resource.getId();
    }

    @Override
    public Response<TestpressApiResponse<Category>> getItems(int page, int size) throws IOException {
        queryParams.put(TestpressExamApiClient.PARENT, parentId);
        queryParams.put(TestpressExamApiClient.PAGE, page);
        return apiClient.getCategories(queryParams).execute();
    }

}
