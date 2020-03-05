package in.testpress.exam.pager;

import java.io.IOException;

import in.testpress.exam.models.AttemptItem;
import in.testpress.exam.network.TestpressExamApiClient;
import in.testpress.models.TestpressApiResponse;
import in.testpress.network.BaseResourcePager;
import retrofit2.Response;

public class TestQuestionsPager extends BaseResourcePager<AttemptItem> {

    private TestpressExamApiClient apiClient;
    private final String questionsUrlFrag;

    public TestQuestionsPager(String questionsUrlFrag, TestpressExamApiClient apiClient) {
        this.apiClient = apiClient;
        this.questionsUrlFrag = questionsUrlFrag;
    }

    @Override
    protected Object getId(AttemptItem resource) {
        return resource.getUrl();
    }

    @Override
    public Response<TestpressApiResponse<AttemptItem>> getItems(int page, int size) throws IOException {
        queryParams.put(TestpressExamApiClient.PAGE, page);
        return apiClient.getQuestions(questionsUrlFrag, queryParams).execute();
    }

    public TestpressApiResponse<AttemptItem> getResponse() {
        return response;
    }

}
