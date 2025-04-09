package in.testpress.store.network;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import in.testpress.models.TestpressApiResponse;
import in.testpress.network.RetrofitCall;
import in.testpress.store.data.model.NetworkProduct;
import in.testpress.store.data.model.NetworkProductCategory;
import in.testpress.store.data.model.NetworkProductListResponse;
import in.testpress.store.models.NetworkHash;
import in.testpress.store.models.NetworkOrderStatus;
import in.testpress.store.models.Order;
import in.testpress.store.models.Product;
import in.testpress.v2_4.models.ApiResponse;
import in.testpress.v2_4.models.ProductsListResponse;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

import static in.testpress.store.network.StoreApiClient.APPLY_COUPON_PATH;
import static in.testpress.store.network.StoreApiClient.ORDERS_PATH;
import static in.testpress.store.network.StoreApiClient.ORDER_API_PATH;
import static in.testpress.store.network.StoreApiClient.ORDER_CONFIRM_PATH;
import static in.testpress.store.network.StoreApiClient.ORDER_STATE_REFRESH_PATH;
import static in.testpress.store.network.StoreApiClient.PAYU_HASH_GENERATOR_PATH;
import static in.testpress.store.network.StoreApiClient.V3_PRODUCT_PATH;
import static in.testpress.store.network.StoreApiClient.v2_4_ORDERS_PATH;
import static in.testpress.store.network.StoreApiClient.v3_ORDERS_PATH;
import static in.testpress.store.network.StoreApiClient.V2_5_PRODUCTS_CATEGORIES_PATH;

public interface ProductService {

    @GET(StoreApiClient.PRODUCTS_LIST_PATH)
    RetrofitCall<TestpressApiResponse<Product>> getProducts(@QueryMap Map<String, Object> options);

    @GET(StoreApiClient.V4_PRODUCTS_LIST_PATH)
    RetrofitCall<ApiResponse<ProductsListResponse>> getv4Products(@QueryMap Map<String, Object> options);

    @GET(StoreApiClient.PRODUCTS_LIST_PATH + "{product_slug}")
    RetrofitCall<Product> getProductDetails(
            @Path(value = "product_slug", encoded = true) String productUrlFrag);

    @POST(v3_ORDERS_PATH)
    RetrofitCall<Order> order(@Body HashMap<String, Object> arguments);

    @POST(v2_4_ORDERS_PATH + "{order_id}" + APPLY_COUPON_PATH)
    RetrofitCall<Order> applyCoupon(
            @Path(value = "order_id", encoded = true) Long orderId,
            @Body HashMap<String, String> arguments);

    @PUT(ORDERS_PATH + "{order_id}" + ORDER_CONFIRM_PATH)
    RetrofitCall<Order> orderConfirm(
            @Path(value = "order_id", encoded = true) int orderId,
            @Body HashMap<String, Object> arguments);

    @POST(PAYU_HASH_GENERATOR_PATH)
    RetrofitCall<NetworkHash> generateHash(@Body HashMap<String, Object> arguments);

    @POST(ORDER_API_PATH + "{order_id}" + ORDER_STATE_REFRESH_PATH)
    RetrofitCall<NetworkOrderStatus> refreshOrderStatus(
            @Path(value = "order_id", encoded = true) String orderId,
            @Body HashMap<String, Boolean> arguments);

    @GET("/api/v3/products/")
    RetrofitCall<NetworkProductListResponse> getProductsV3(@QueryMap Map<String, Object> options);

    @GET(V2_5_PRODUCTS_CATEGORIES_PATH)
    RetrofitCall<ApiResponse<List<NetworkProductCategory>>> getProductsCategories(
            @QueryMap Map<String, Object> options
    );

    @GET(V3_PRODUCT_PATH + "{product_id}")
    RetrofitCall<NetworkProduct> getProductDetailV3(
            @Path(value = "product_id", encoded = true) Long productId
    );
}


