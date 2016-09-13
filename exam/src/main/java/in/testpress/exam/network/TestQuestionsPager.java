package in.testpress.exam.network;

import java.io.IOException;

import in.testpress.exam.models.AttemptItem;
import in.testpress.exam.models.TestpressApiResponse;
import retrofit2.Response;

public class TestQuestionsPager extends BaseResourcePager<AttemptItem> {

    private final String questionsUrlFrag;

    public TestQuestionsPager(String questionsUrlFrag, TestpressExamApiClient service) {
        super(service);
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

}
