package in.testpress.store.network;

import android.content.Context;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import in.testpress.core.TestpressSdk;
import in.testpress.models.TestpressApiResponse;
import in.testpress.network.RetrofitCall;
import in.testpress.network.TestpressApiClient;
import in.testpress.store.data.model.NetworkProduct;
import in.testpress.store.data.model.NetworkProductCategory;
import in.testpress.store.data.model.NetworkProductListResponse;
import in.testpress.store.models.NetworkHash;
import in.testpress.store.models.NetworkOrderStatus;
import in.testpress.store.models.Order;
import in.testpress.store.models.OrderItem;
import in.testpress.store.models.Product;
import in.testpress.v2_4.models.ApiResponse;
import in.testpress.v2_4.models.ProductsListResponse;

public class StoreApiClient extends TestpressApiClient {

    public static final String PRODUCTS_LIST_PATH =  "/api/v2.2/products/";

    public static final String V4_PRODUCTS_LIST_PATH =  "/api/v2.4/products/";

    public static final String ORDERS_PATH = "/api/v2.2/orders/";

    public static final String ORDER_API_PATH = "/api/v2.5/orders/";

    public static final String ORDER_STATE_REFRESH_PATH = "/refresh/";

    public static final String ORDER_CONFIRM_PATH = "/confirm/";

    public static final String URL_PAYMENT_RESPONSE_HANDLER = "/payments/response/payu/";
    public static final String RAZORPAY_PAYMENT_RESPONSE_PATH = "/payments/response/razorpay/";

    public static final String PAYU_HASH_GENERATOR_PATH = "/api/v2.5/payu/dynamic_hash/";

    public static final String v3_ORDERS_PATH = "/api/v3/orders/";

    public static final String v2_4_ORDERS_PATH = "/api/v2.4/orders/";

    public static final String APPLY_COUPON_PATH = "/apply-coupon/";

    public static final String APPLY_DISCOUNT_PATH = "/apply-discount/";

    public static final String V2_5_PRODUCTS_CATEGORIES_PATH = "/api/v2.5/products/categories/";

    public static final String V3_PRODUCT_PATH = "/api/v3/products/";

    public StoreApiClient(final Context context) {
        super(context, checkTestpressSessionIsNull(TestpressSdk.getTestpressSession(context)));
    }
    
    public ProductService getProductService() {
        return retrofit.create(ProductService.class);
    }

    public RetrofitCall<TestpressApiResponse<Product>> getProducts(Map<String, Object> queryParams) {
        return getProductService().getProducts(queryParams);
    }

    public RetrofitCall<ApiResponse<ProductsListResponse>> getv4Products(Map<String, Object> queryParams) {
        return getProductService().getv4Products(queryParams);
    }

    public RetrofitCall<Product> getProductDetail(String productSlug) {
        return getProductService().getProductDetails(productSlug);
    }

    public RetrofitCall<Order> order(List<OrderItem> orderItems) {
        HashMap<String, Object> orderParameters = new HashMap<String, Object>();
        orderParameters.put("order_items", orderItems);
        return getProductService().order(orderParameters);
    }

    public RetrofitCall<Order> applyCoupon(Long orderId,String couponCode, Boolean useNewDiscountFeat) {
        HashMap<String, String> orderParameters = new HashMap<String, String>();
        orderParameters.put("code", couponCode);
        if (Boolean.TRUE.equals(useNewDiscountFeat)){
            return getProductService().applyDiscount(orderId, orderParameters);
        } else {
            return getProductService().applyCoupon(orderId, orderParameters);
        }
    }

    public RetrofitCall<Order> orderConfirm(Order order) {
        HashMap<String, Object> orderParameters = new HashMap<String, Object>();
        orderParameters.put("user", order.getUser());
        orderParameters.put("order_items", order.getOrderItems());
        orderParameters.put("shipping_address", order.getShippingAddress());
        orderParameters.put("zip", order.getZip());
        orderParameters.put("phone", order.getPhone());
        orderParameters.put("land_mark", order.getLandMark());
        return getProductService().orderConfirm(order.getId(), orderParameters);
    }

    public RetrofitCall<NetworkOrderStatus> refreshOrderStatus(String orderId, Boolean reconciliation) {
        HashMap<String, Boolean> parameters = new HashMap<>();
        parameters.put("trigger_payment_reconciliation", reconciliation);
        return getProductService().refreshOrderStatus(orderId, parameters);
    }

    public RetrofitCall<NetworkHash> generateHash(String key) {
        HashMap<String, Object> postData = new HashMap<String, Object>();
        postData.put("data", key);
        return getProductService().generateHash(postData);
    }

    public RetrofitCall<NetworkProductListResponse> getProductsV3(Map<String, Object> queryParams) {
        return getProductService().getProductsV3(queryParams);
    }

    public RetrofitCall<ApiResponse<List<NetworkProductCategory>>> getProductsCategories(Map<String, Object> queryParams) {
        return getProductService().getProductsCategories(queryParams);
    }

    public RetrofitCall<NetworkProduct> getProductDetailV3(int productId) {
        return getProductService().getProductDetailV3(productId);
    }
}
