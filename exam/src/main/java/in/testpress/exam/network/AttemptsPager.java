package in.testpress.exam.network;

import java.util.List;

import in.testpress.exam.models.Attempt;
import in.testpress.exam.models.Exam;
import in.testpress.exam.models.TestpressApiResponse;

public class AttemptsPager extends BaseResourcePager<Attempt> {

    Exam exam;
    TestpressApiResponse<Attempt> response;

    public AttemptsPager(Exam exam, TestpressExamApiClient service) {
        super(service);
        this.exam = exam;
    }

    @Override
    public BaseResourcePager<Attempt> clear() {
        response = null;
        return super.clear();
    }

    @Override
    protected Object getId(Attempt resource) {
        return resource.getId();
    }

    @Override
    public List<Attempt> getItems(int page, int size) {
        queryParams.put(TestpressExamApiClient.PAGE, page);
        response = apiClient.getAttempts(exam.getAttemptsFrag(), queryParams);
        return response.getResults();
    }

    @Override
    public boolean hasNext() {
        return response == null || response.getNext() != null;
    }
}
