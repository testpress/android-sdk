package in.testpress.store.network;

import java.io.IOException;

import in.testpress.models.TestpressApiResponse;
import in.testpress.network.BaseResourcePager;
import in.testpress.store.models.Product;
import retrofit2.Response;

import static in.testpress.network.TestpressApiClient.PAGE;

public class ProductsPager extends BaseResourcePager<Product> {

    private TestpressStoreApiClient apiClient;;

    public ProductsPager(TestpressStoreApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    protected Object getId(Product resource) {
        return resource.getId();
    }

    @Override
    public Response<TestpressApiResponse<Product>> getItems(int page, int size) throws IOException {
        queryParams.put(PAGE, page);
        return apiClient.getProducts(queryParams).execute();
    }

}
