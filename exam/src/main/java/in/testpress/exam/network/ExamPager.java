package in.testpress.exam.network;

import java.io.IOException;

import in.testpress.exam.models.Exam;
import in.testpress.exam.models.TestpressApiResponse;
import retrofit2.Response;

public class ExamPager extends BaseResourcePager<Exam> {

    private final String subclass;

    public ExamPager(String subclass, TestpressExamApiClient apiClient) {
        super(apiClient);
        this.subclass = subclass;
    }

    @Override
    protected Object getId(Exam resource) {
        return resource.getId();
    }

    @Override
    public Response<TestpressApiResponse<Exam>> getItems(int page, int size) throws IOException {
        queryParams.put(TestpressExamApiClient.STATE, subclass);
        queryParams.put(TestpressExamApiClient.PAGE, page);
        return apiClient.getExams(queryParams).execute();
    }

}
