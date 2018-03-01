package in.testpress.store.network;

import android.content.Context;

import java.util.Map;

import in.testpress.core.TestpressSdk;
import in.testpress.model.TestpressApiResponse;
import in.testpress.network.RetrofitCall;
import in.testpress.network.TestpressApiClient;
import in.testpress.store.models.Product;

public class TestpressStoreApiClient extends TestpressApiClient {

    public static final String PRODUCTS_LIST_PATH =  "/api/v2.2/products/";

    public TestpressStoreApiClient(final Context context) {
        super(context, checkTestpressSessionIsNull(TestpressSdk.getTestpressSession(context)));
    }
    
    public ProductService getProductService() {
        return retrofit.create(ProductService.class);
    }

    public RetrofitCall<TestpressApiResponse<Product>> getProducts(Map<String, Object> queryParams) {
        return getProductService().getProducts(queryParams);
    }

    public RetrofitCall<Product> getProductDetail(String productSlug) {
        return getProductService().getProductDetails(productSlug);
    }

}
