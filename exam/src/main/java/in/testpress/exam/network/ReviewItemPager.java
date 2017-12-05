package in.testpress.exam.network;

import java.io.IOException;

import in.testpress.models.TestpressApiResponse;
import in.testpress.models.greendao.ReviewItem;
import in.testpress.network.BaseDatabaseModelPager;
import retrofit2.Response;

public class ReviewItemPager extends BaseDatabaseModelPager<ReviewItem> {

    private String reviewUrl;
    private TestpressExamApiClient apiClient;

    public ReviewItemPager(String reviewUrl, TestpressExamApiClient apiClient) {
        this.reviewUrl = reviewUrl;
        this.apiClient = apiClient;
    }

    @Override
    protected Object getId(ReviewItem resource) {
        return resource.getId();
    }

    @Override
    public Response<TestpressApiResponse<ReviewItem>> getItems(int page, int size) throws IOException {
        queryParams.put(TestpressExamApiClient.PAGE, page);
        return apiClient.getReviewItems(reviewUrl, queryParams).execute();
    }

}
