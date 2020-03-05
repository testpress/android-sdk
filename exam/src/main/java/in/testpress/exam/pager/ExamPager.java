package in.testpress.exam.pager;

import java.io.IOException;

import in.testpress.exam.network.TestpressExamApiClient;
import in.testpress.models.TestpressApiResponse;
import in.testpress.models.greendao.Exam;
import in.testpress.network.BaseResourcePager;
import retrofit2.Response;

public class ExamPager extends BaseResourcePager<Exam> {

    private TestpressExamApiClient apiClient;
    private String subclass;
    private String category;
    private String accessCode;

    public ExamPager(String accessCode, TestpressExamApiClient apiClient) {
        this.apiClient = apiClient;
        this.accessCode = accessCode;
    }

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
        queryParams.put(TestpressExamApiClient.PAGE, page);
        if (accessCode != null) {
            return apiClient.getExams(accessCode, queryParams).execute();
        }
        if (subclass != null) {
            queryParams.put(TestpressExamApiClient.STATE, subclass);
        }
        if (category != null) {
            queryParams.put(TestpressExamApiClient.CATEGORY, category);
        }
        return apiClient.getExams(queryParams).execute();
    }

}
