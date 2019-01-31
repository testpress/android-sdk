package in.testpress.exam.network;

import in.testpress.exam.models.AttemptItem;
import in.testpress.models.TestpressApiResponse;
import in.testpress.network.BaseResourcePager;
import in.testpress.network.RetrofitCall;

public class TestQuestionsPager extends BaseResourcePager<AttemptItem> {

    private TestpressExamApiClient apiClient;
    private final String questionsUrlFrag;

    public TestQuestionsPager(String questionsUrlFrag, TestpressExamApiClient apiClient) {
        this.apiClient = apiClient;
        this.questionsUrlFrag = questionsUrlFrag;
    }

    public void setApiClient(TestpressExamApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    protected Object getId(AttemptItem resource) {
        return resource.getUrl();
    }

    @Override
    public RetrofitCall<TestpressApiResponse<AttemptItem>> getItems(int page, int size) {
        queryParams.put(TestpressExamApiClient.PAGE, page);
        return apiClient.getQuestions(questionsUrlFrag, queryParams);
    }

}
