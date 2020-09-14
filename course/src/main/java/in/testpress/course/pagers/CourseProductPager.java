package in.testpress.course.pagers;

import java.io.IOException;
import java.util.List;

import in.testpress.models.greendao.Product;
import in.testpress.store.network.StoreApiClient;
import in.testpress.v2_4.BaseResourcePager;
import in.testpress.v2_4.models.ApiResponse;
import in.testpress.v2_4.models.ProductsListResponse;
import retrofit2.Response;

import static in.testpress.network.TestpressApiClient.PAGE;

public class CourseProductPager extends BaseResourcePager<ProductsListResponse, Product> {

    private StoreApiClient apiClient;
    public CourseProductPager(StoreApiClient apiClient) {
        this.apiClient = apiClient;
    }


    @Override
    protected Object getId(Product resource) {
        return resource.getId();
    }

    @Override
    public Response<ApiResponse<ProductsListResponse>> getResponse(
            int page, int size) throws IOException {
        queryParams.put(PAGE, page);
        return apiClient.getv4Products(queryParams).execute();
    }

    @Override
    public List<Product> getItems(ProductsListResponse resultResponse) {
        return resultResponse.getProducts();
    }

}
