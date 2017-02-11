package in.testpress.exam.network;

import java.io.IOException;

import in.testpress.exam.models.CourseAttempt;
import in.testpress.model.TestpressApiResponse;
import in.testpress.network.BaseResourcePager;
import retrofit2.Response;

import static in.testpress.exam.network.TestpressExamApiClient.STATE_PAUSED;

public class ContentAttemptsPager extends BaseResourcePager<CourseAttempt> {

    private TestpressExamApiClient apiClient;
    private final String attemptsUrl;
    private final String filter;

    public ContentAttemptsPager(String attemptsUrl, String filter,
                                TestpressExamApiClient apiClient) {

        this.apiClient = apiClient;
        this.filter = filter;
        this.attemptsUrl = attemptsUrl;
    }

    @Override
    protected Object getId(CourseAttempt resource) {
        return resource.getId();
    }

    @Override
    public Response<TestpressApiResponse<CourseAttempt>> getItems(int page, int size)
            throws IOException {

        queryParams.put(TestpressExamApiClient.PAGE, page);
        return apiClient.getContentAttempts(attemptsUrl, queryParams).execute();
    }

    @Override
    protected CourseAttempt register(CourseAttempt courseAttempt){
        if (filter != null && filter.equals(STATE_PAUSED) && courseAttempt != null &&
                !courseAttempt.getAssessment().getState().equals(STATE_PAUSED)) {
            // To filter paused attempts, discard completed attempts.
            return null;
        }
        return courseAttempt;
    }

}
