package in.testpress.exam.network;

import java.io.IOException;

import in.testpress.exam.models.Category;
import in.testpress.models.TestpressApiResponse;
import in.testpress.network.BaseResourcePager;
import in.testpress.network.RetrofitCall;
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
    public RetrofitCall<TestpressApiResponse<Category>> getItems(int page, int size) {
        queryParams.put(TestpressExamApiClient.PARENT, parentId);
        queryParams.put(TestpressExamApiClient.PAGE, page);
        return apiClient.getCategories(queryParams);
    }

}
