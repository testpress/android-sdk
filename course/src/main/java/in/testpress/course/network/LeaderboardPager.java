package in.testpress.course.network;

import java.io.IOException;

import in.testpress.course.models.Reputation;
import in.testpress.models.TestpressApiResponse;
import in.testpress.network.BaseResourcePager;
import retrofit2.Response;

public class LeaderboardPager extends BaseResourcePager<Reputation> {

    private TestpressCourseApiClient apiClient;

    public LeaderboardPager(TestpressCourseApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    protected Object getId(Reputation resource) {
        return resource.getId();
    }

    @Override
    public Response<TestpressApiResponse<Reputation>> getItems(int page, int size) throws IOException {
        queryParams.put(TestpressCourseApiClient.PAGE, page);
        queryParams.put(TestpressCourseApiClient.PAGE_SIZE, 20);
        return apiClient.getLeaderboard(queryParams).execute();
    }

}
