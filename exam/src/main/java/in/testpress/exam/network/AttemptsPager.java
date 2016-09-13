package in.testpress.exam.network;

import java.io.IOException;

import in.testpress.exam.models.Attempt;
import in.testpress.exam.models.TestpressApiResponse;
import retrofit2.Response;

public class AttemptsPager extends BaseResourcePager<Attempt> {

    private final String attemptsUrlFrag;

    public AttemptsPager(String attemptsUrlFrag, TestpressExamApiClient service) {
        super(service);
        this.attemptsUrlFrag = attemptsUrlFrag;
    }

    @Override
    protected Object getId(Attempt resource) {
        return resource.getId();
    }

    @Override
    public Response<TestpressApiResponse<Attempt>> getItems(int page, int size) throws IOException {
        queryParams.put(TestpressExamApiClient.PAGE, page);
        return apiClient.getAttempts(attemptsUrlFrag, queryParams).execute();
    }

}
