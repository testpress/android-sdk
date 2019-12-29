package in.testpress.store.network;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import in.testpress.models.TestpressApiResponse;
import in.testpress.models.greendao.BookmarkFolder;
import in.testpress.models.greendao.Course;
import in.testpress.models.greendao.Price;
import in.testpress.models.greendao.Product;
import in.testpress.models.greendao.Video;
import in.testpress.network.TestpressApiClient;
import in.testpress.v2_4.BaseResourcePager;
import in.testpress.v2_4.models.ApiResponse;
import in.testpress.v2_4.models.ProductsListResponse;
import retrofit2.Response;

import static in.testpress.network.TestpressApiClient.PAGE;

public class ProductsPager extends BaseResourcePager<ProductsListResponse, Product> {

    private TestpressStoreApiClient apiClient;
    private SimpleDateFormat simpleDateFormat;
    private final Map<Object, Price> prices = new LinkedHashMap<>();
    private final Map<Object, Course> courses = new LinkedHashMap<>();

    public ProductsPager(TestpressStoreApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public int getTotalCount() {
        return apiResponse.getCount();
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

        for (Price price : resultResponse.getPrices()) {
            prices.put(price.getId(), price);
        }

        for (Course course: resultResponse.getCourses()) {
            courses.put(course.getId(), course);
        }
        return resultResponse.getProducts();
    }

    public ArrayList<Price> getPrices() {
        return new ArrayList<>(prices.values());
    }

    public ArrayList<Course> getCourses() {
        return new ArrayList<>(courses.values());
    }
}
