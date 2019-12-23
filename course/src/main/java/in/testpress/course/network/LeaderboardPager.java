package in.testpress.course.network;

import in.testpress.course.models.Reputation;
import in.testpress.models.TestpressApiResponse;
import in.testpress.network.BaseResourcePager;
import in.testpress.network.RetrofitCall;

public class LeaderboardPager extends BaseResourcePager<Reputation> {

    private TestpressCourseApiClient apiClient;

    public LeaderboardPager(TestpressCourseApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public LeaderboardPager() {}

    public void setApiClient(TestpressCourseApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    protected Object getId(Reputation resource) {
        return resource.getId();
    }

    @Override
    public RetrofitCall<TestpressApiResponse<Reputation>> getItems(int page, int size) {
        queryParams.put(TestpressCourseApiClient.PAGE, page);
        queryParams.put(TestpressCourseApiClient.PAGE_SIZE, 20);
        return apiClient.getLeaderboard(queryParams);
    }

}
