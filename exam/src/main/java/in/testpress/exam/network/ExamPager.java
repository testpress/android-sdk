package in.testpress.exam.network;

import java.io.IOException;

import in.testpress.exam.models.Exam;
import in.testpress.model.TestpressApiResponse;
import in.testpress.network.BaseResourcePager;
import retrofit2.Response;

public class ExamPager extends BaseResourcePager<Exam> {

    private TestpressExamApiClient apiClient;
    private final String subclass;
    private String category;

    public ExamPager(String subclass, String category, TestpressExamApiClient apiClient) {
        this.apiClient = apiClient;
        this.subclass = subclass;
        this.category = category;
    }

    @Override
    protected Object getId(Exam resource) {
        return resource.getId();
    }

    @Override
    public Response<TestpressApiResponse<Exam>> getItems(int page, int size) throws IOException {
        queryParams.put(TestpressExamApiClient.STATE, subclass);
        queryParams.put(TestpressExamApiClient.PAGE, page);
        if (category != null) {
            queryParams.put(TestpressExamApiClient.CATEGORY, category);
        }
        return apiClient.getExams(queryParams).execute();
    }

}
