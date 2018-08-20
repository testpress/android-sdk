package in.testpress.course.network;

import java.text.ParseException;

import in.testpress.models.TestpressApiResponse;
import in.testpress.models.greendao.Content;
import in.testpress.network.BaseDatabaseModelPager;
import in.testpress.network.RetrofitCall;

import static in.testpress.network.TestpressApiClient.UNFILTERED;

public class ContentPager extends BaseDatabaseModelPager<Content> {

    private TestpressCourseApiClient apiClient;
    private long courseId;

    public ContentPager(long courseId, TestpressCourseApiClient apiClient) {
        this.courseId = courseId;
        this.apiClient = apiClient;
    }

    public ContentPager() {}

    public void setApiClient(TestpressCourseApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public void setContentsUrlFrag(long courseId) {
        this.courseId = courseId;
    }

    @Override
    protected Object getId(Content resource) {
        return resource.getId();
    }

    @Override
    public RetrofitCall<TestpressApiResponse<Content>> getItems(int page, int size) {
        queryParams.put(TestpressCourseApiClient.PAGE, page);
        queryParams.put(UNFILTERED, true);
        return apiClient.getContents(courseId, queryParams);
    }

    @Override
    protected Content register(Content content) {
        if (content != null && content.getModified() != null && !content.getModified().isEmpty()) {
            try {
                content.setModifiedDate(simpleDateFormat.parse(content.getModified()).getTime());
            } catch (ParseException e) {
                e.printStackTrace();
                return null;
            }
        }
        return content;
    }

}
