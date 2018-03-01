package in.testpress.store.network;

import java.util.Map;

import in.testpress.model.TestpressApiResponse;
import in.testpress.network.RetrofitCall;
import in.testpress.store.models.Product;

import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

public interface ProductService {

    @GET(TestpressStoreApiClient.PRODUCTS_LIST_PATH)
    RetrofitCall<TestpressApiResponse<Product>> getProducts(@QueryMap Map<String, Object> options);

    @GET(TestpressStoreApiClient.PRODUCTS_LIST_PATH + "{product_slug}")
    RetrofitCall<Product> getProductDetails(
            @Path(value = "product_slug", encoded = true) String productUrlFrag);

}


