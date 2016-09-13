package in.testpress.exam.network;

import java.io.IOException;

import in.testpress.exam.models.ReviewItem;
import in.testpress.exam.models.TestpressApiResponse;
import retrofit2.Response;

public class ReviewQuestionsPager extends BaseResourcePager<ReviewItem> {

    private String reviewUrlFrag;
    private String filter;

    public ReviewQuestionsPager(String reviewUrlFrag, String filter, TestpressExamApiClient service) {
        super(service);
        this.reviewUrlFrag = reviewUrlFrag;
        this.filter = filter;
    }

    @Override
    protected Object getId(ReviewItem resource) {
        return resource.getId();
    }

    @Override
    public Response<TestpressApiResponse<ReviewItem>> getItems(int page, int size) throws IOException {
        if (!filter.equals("all")) {
            queryParams.put(TestpressExamApiClient.STATE, filter);
        } else {
            queryParams.remove(TestpressExamApiClient.STATE);
        }
        queryParams.put(TestpressExamApiClient.PAGE, page);
        return apiClient.getReviewItems(reviewUrlFrag, queryParams).execute();
    }

}
