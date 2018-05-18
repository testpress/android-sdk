package in.testpress.exam.network;

import android.util.Log;

import java.io.IOException;

import in.testpress.exam.models.Comment;
import in.testpress.models.TestpressApiResponse;
import in.testpress.network.BaseResourcePager;
import retrofit2.Response;

import static in.testpress.exam.network.TestpressExamApiClient.COMMENTS_PATH;
import static in.testpress.exam.network.TestpressExamApiClient.QUESTIONS_PATH;

public class CommentsPager extends BaseResourcePager<Comment> {

    private TestpressExamApiClient apiClient;
    private final String commentsUrlFrag;

    public CommentsPager(String commentsUrlFrag, TestpressExamApiClient apiClient) {
        this.apiClient = apiClient;
        this.commentsUrlFrag = commentsUrlFrag;
    }

    public CommentsPager(long questionId, TestpressExamApiClient apiClient) {
        this(apiClient.getBaseUrl() + QUESTIONS_PATH + questionId + COMMENTS_PATH, apiClient);
    }

    @Override
    protected Object getId(Comment resource) {
        return resource.getId();
    }

    @Override
    public Response<TestpressApiResponse<Comment>> getItems(int page, int size) throws IOException {
        queryParams.put(TestpressExamApiClient.PAGE, page);
        return apiClient.getComments(commentsUrlFrag, queryParams).execute();
    }

    public Integer getCommentsCount() {
        return response != null ? response.getCount() : 0;
    }

}
