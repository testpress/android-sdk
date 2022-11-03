package `in`.testpress.course.pagers

import `in`.testpress.course.api.TestpressCourseApiClient
import `in`.testpress.course.models.Reputation
import `in`.testpress.models.TestpressApiResponse
import `in`.testpress.network.BaseResourcePager
import retrofit2.Response

class LeaderboardPager(val apiClient: TestpressCourseApiClient) :
    BaseResourcePager<Reputation>() {

    override fun getId(resource: Reputation): Any =resource.id

    override fun getItems(page: Int, size: Int): Response<TestpressApiResponse<Reputation>> {
        queryParams.put(TestpressCourseApiClient.PAGE,page)
        queryParams.put(TestpressCourseApiClient.PAGE_SIZE, 20)
        return apiClient.getLeaderboard(queryParams).execute()
    }
}