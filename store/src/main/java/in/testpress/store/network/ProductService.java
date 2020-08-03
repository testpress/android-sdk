package in.testpress.store.network;

import java.util.HashMap;
import java.util.Map;
import in.testpress.models.TestpressApiResponse;
import in.testpress.network.RetrofitCall;
import in.testpress.store.models.CouponCodeResponse;
import in.testpress.store.models.Order;
import in.testpress.store.models.Product;
import in.testpress.store.models.Response;
import in.testpress.v2_4.models.ApiResponse;
import in.testpress.v2_4.models.ProductsListResponse;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;
import static in.testpress.store.network.TestpressStoreApiClient.APPLY_COUPON;
import static in.testpress.store.network.TestpressStoreApiClient.COUPON_PATH;
import static in.testpress.store.network.TestpressStoreApiClient.ORDERS_PATH;
import static in.testpress.store.network.TestpressStoreApiClient.ORDER_CONFIRM_PATH;

public interface ProductService {

    @GET(TestpressStoreApiClient.PRODUCTS_LIST_PATH)
    RetrofitCall<TestpressApiResponse<Product>> getProducts(@QueryMap Map<String, Object> options);

    @GET(TestpressStoreApiClient.V4_PRODUCTS_LIST_PATH)
    RetrofitCall<ApiResponse<ProductsListResponse>> getv4Products(@QueryMap Map<String, Object> options);

    @GET(TestpressStoreApiClient.V4_PRODUCTS_LIST_PATH + "{product_slug}")
    RetrofitCall<Response> getProductDetails(
            @Path(value = "product_slug", encoded = true) String productUrlFrag);

    @POST(ORDERS_PATH)
    RetrofitCall<Order> order(@Body HashMap<String, Object> arguments);

    @PUT(ORDERS_PATH + "{order_id}" + ORDER_CONFIRM_PATH)
    RetrofitCall<Order> orderConfirm(
            @Path(value = "order_id", encoded = true) int orderId,
            @Body HashMap<String, Object> arguments);

    @PATCH(COUPON_PATH + "{customer_id}" + APPLY_COUPON)
    RetrofitCall<CouponCodeResponse> applyCouponCode(
            @Path(value = "customer_id", encoded = true) int customerId
    );

}


