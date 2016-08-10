package in.testpress.exam.network;

import java.util.List;

import in.testpress.exam.models.Attempt;
import in.testpress.exam.models.ReviewItem;
import in.testpress.exam.models.TestpressApiResponse;

public class ReviewQuestionsPager extends BaseResourcePager<ReviewItem> {

    private Attempt attempt;
    private String filter;
    private TestpressApiResponse<ReviewItem> response;

    public ReviewQuestionsPager(Attempt attempt, String filter, TestpressExamApiClient service) {
        super(service);
        this.attempt = attempt;
        this.filter = filter;
    }

    @Override
    public BaseResourcePager<ReviewItem> clear() {
        response = null;
        return super.clear();
    }

    @Override
    protected Object getId(ReviewItem resource) {
        return resource.getId();
    }

    @Override
    public List<ReviewItem> getItems(int page, int size) {
        if (!filter.equals("all")) {
            queryParams.put(TestpressExamApiClient.STATE, filter);
        } else {
            queryParams.remove(TestpressExamApiClient.STATE);
        }
        queryParams.put(TestpressExamApiClient.PAGE, page);
        response = apiClient.getReviewItems(attempt.getReviewFrag(), queryParams);
        return response.getResults();
    }

    @Override
    public boolean hasNext() {
        return response == null || response.getNext() != null;
    }
}
