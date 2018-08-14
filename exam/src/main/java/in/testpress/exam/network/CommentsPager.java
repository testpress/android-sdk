package in.testpress.exam.network;

import in.testpress.exam.models.Comment;
import in.testpress.models.TestpressApiResponse;
import in.testpress.network.BaseResourcePager;
import in.testpress.network.RetrofitCall;

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
    public RetrofitCall<TestpressApiResponse<Comment>> getItems(int page, int size) {
        queryParams.put(TestpressExamApiClient.PAGE, page);
        return apiClient.getComments(commentsUrl, queryParams);
    }

    public Integer getCommentsCount() {
        return response != null ? response.getCount() : 0;
    }

}
