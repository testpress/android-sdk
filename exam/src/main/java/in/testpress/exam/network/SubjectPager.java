package in.testpress.exam.network;

import in.testpress.exam.models.Subject;
import in.testpress.models.TestpressApiResponse;
import in.testpress.network.BaseResourcePager;
import in.testpress.network.RetrofitCall;

public class SubjectPager extends BaseResourcePager<Subject> {

    private TestpressExamApiClient apiClient;
    private final String parentSubjectId;
    private final String baseUrl;

    public SubjectPager(String parentSubjectId, String baseUrl, TestpressExamApiClient apiClient) {
        this.apiClient = apiClient;
        this.parentSubjectId = parentSubjectId;
        this.baseUrl = baseUrl;
    }

    public void setApiClient(TestpressExamApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    protected Object getId(Subject resource) {
        return resource.getId();
    }

    @Override
    public RetrofitCall<TestpressApiResponse<Subject>> getItems(int page, int size) {
        queryParams.put(TestpressExamApiClient.PAGE, page);
        if (parentSubjectId != null) {
            queryParams.put(TestpressExamApiClient.PARENT, parentSubjectId);
        }
        return apiClient.getSubjects(baseUrl, queryParams);
    }

    @Override
    protected Subject register(Subject subject){
        // Discard the Subject if its Total answered count is zero
        if ((subject != null) && (subject.getTotal() != 0)) {
            subject.setCorrectPercentage(getPercentage(subject.getCorrect(), subject.getTotal()));
            subject.setIncorrectPercentage(getPercentage(subject.getIncorrect(), subject.getTotal()));
            subject.setUnansweredPercentage(getPercentage(subject.getUnanswered(), subject.getTotal()));
            return subject;
        }
        return null;
    }

    private float getPercentage(int value, int total) {
        return ((float) value) / total * 100f;
    }

}
