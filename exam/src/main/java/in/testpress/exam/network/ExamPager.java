package in.testpress.exam.network;

import java.util.List;

import in.testpress.exam.models.Exam;
import in.testpress.exam.models.TestpressApiResponse;
import retrofit.RetrofitError;

public class ExamPager extends BaseResourcePager<Exam> {

    private final String subclass;
    private TestpressApiResponse<Exam> response;

    public ExamPager(String subclass, TestpressExamApiClient apiClient) {
        super(apiClient);
        this.subclass = subclass;
    }

    @Override
    public BaseResourcePager<Exam> clear() {
        response = null;
        return super.clear();
    }

    @Override
    protected Object getId(Exam resource) {
        return resource.getId();
    }

    @Override
    public List<Exam> getItems(int page, int size) throws RetrofitError {
        queryParams.put(TestpressExamApiClient.STATE, subclass);
        queryParams.put(TestpressExamApiClient.PAGE, page);
        response = apiClient.getExams(queryParams);
        return response.getResults();
    }

    @Override
    public boolean hasNext() {
        return response == null || response.getNext() != null;
    }

}
