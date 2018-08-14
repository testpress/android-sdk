package in.testpress.exam.network;

import in.testpress.models.TestpressApiResponse;
import in.testpress.models.greendao.Attempt;
import in.testpress.network.BaseResourcePager;
import in.testpress.network.RetrofitCall;

public class AttemptsPager extends BaseResourcePager<Attempt> {

    private TestpressExamApiClient apiClient;
    private final String attemptsUrlFrag;

    public AttemptsPager(String attemptsUrlFrag, TestpressExamApiClient apiClient) {
        this.apiClient = apiClient;
        this.attemptsUrlFrag = attemptsUrlFrag;
    }

    @Override
    protected Object getId(Attempt resource) {
        return resource.getId();
    }

    @Override
    public RetrofitCall<TestpressApiResponse<Attempt>> getItems(int page, int size) {
        queryParams.put(TestpressExamApiClient.PAGE, page);
        return apiClient.getAttempts(attemptsUrlFrag, queryParams);
    }

}
