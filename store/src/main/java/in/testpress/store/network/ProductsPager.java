package in.testpress.store.network;

import in.testpress.models.TestpressApiResponse;
import in.testpress.network.BaseResourcePager;
import in.testpress.network.RetrofitCall;
import in.testpress.store.models.Product;

import static in.testpress.network.TestpressApiClient.PAGE;

public class ProductsPager extends BaseResourcePager<Product> {

    private TestpressStoreApiClient apiClient;;

    public ProductsPager(TestpressStoreApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ProductsPager() {}

    public void setApiClient(TestpressStoreApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    protected Object getId(Product resource) {
        return resource.getId();
    }

    @Override
    public RetrofitCall<TestpressApiResponse<Product>> getItems(int page, int size) {
        queryParams.put(PAGE, page);
        return apiClient.getProducts(queryParams);
    }

}
