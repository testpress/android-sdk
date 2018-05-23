package in.testpress.exam.network;

import java.io.IOException;

import in.testpress.exam.models.Comment;
import in.testpress.models.TestpressApiResponse;
import in.testpress.network.BaseResourcePager;
import retrofit2.Response;

import static in.testpress.exam.network.TestpressExamApiClient.COMMENTS_PATH;
import static in.testpress.exam.network.TestpressExamApiClient.QUESTIONS_PATH;

public class CommentsPager extends BaseResourcePager<Comment> {

    private TestpressExamApiClient apiClient;
    private final String commentsUrl;

    public CommentsPager(long questionId, TestpressExamApiClient apiClient) {
        this.apiClient = apiClient;
        this.commentsUrl = apiClient.getBaseUrl() + QUESTIONS_PATH + questionId + COMMENTS_PATH;
    }

    @Override
    protected Object getId(Comment resource) {
        return resource.getId();
    }

    @Override
    public Response<TestpressApiResponse<Comment>> getItems(int page, int size) throws IOException {
        queryParams.put(TestpressExamApiClient.PAGE, page);
        return apiClient.getComments(commentsUrl, queryParams).execute();
    }

    public Integer getCommentsCount() {
        return response != null ? response.getCount() : 0;
    }

}
