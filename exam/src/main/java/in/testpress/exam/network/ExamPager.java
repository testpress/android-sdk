package in.testpress.exam.network;


import in.testpress.models.TestpressApiResponse;
import in.testpress.models.greendao.Exam;
import in.testpress.network.BaseResourcePager;
import in.testpress.network.RetrofitCall;

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
    public RetrofitCall<TestpressApiResponse<Exam>> getItems(int page, int size) {
        queryParams.put(TestpressExamApiClient.PAGE, page);
        if (accessCode != null) {
            return apiClient.getExams(accessCode, queryParams);
        }
        if (subclass != null) {
            queryParams.put(TestpressExamApiClient.STATE, subclass);
        }
        if (category != null) {
            queryParams.put(TestpressExamApiClient.CATEGORY, category);
        }
        return apiClient.getExams(queryParams);
    }

}
