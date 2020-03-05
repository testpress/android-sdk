package in.testpress.exam.pager;

import java.io.IOException;

import in.testpress.exam.models.Comment;
import in.testpress.exam.network.TestpressExamApiClient;
import in.testpress.models.TestpressApiResponse;
import in.testpress.network.BaseResourcePager;
import retrofit2.Response;

public class CommentsPager extends BaseResourcePager<Comment> {

    private TestpressExamApiClient apiClient;
    private String commentsUrl;

    public CommentsPager(String commentsUrl, TestpressExamApiClient apiClient) {
        this.apiClient = apiClient;
        this.commentsUrl = commentsUrl;
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
