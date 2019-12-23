package in.testpress.course.network;

import java.text.ParseException;

import in.testpress.models.TestpressApiResponse;
import in.testpress.models.greendao.Chapter;
import in.testpress.network.BaseDatabaseModelPager;
import in.testpress.network.RetrofitCall;
import in.testpress.network.TestpressApiClient;

import static in.testpress.network.TestpressApiClient.PARENT;

public class ChapterPager extends BaseDatabaseModelPager<Chapter> {

    private TestpressCourseApiClient apiClient;
    private String courseId;
    private String parentId;

    public ChapterPager(String courseId, TestpressCourseApiClient apiClient) {
        this(courseId, null, apiClient);
    }

    public ChapterPager(String courseId, String parentId, TestpressCourseApiClient apiClient) {
        super();
        this.courseId = courseId;
        this.parentId = parentId;
        this.apiClient = apiClient;
    }

    public ChapterPager() {
        super();
    }

    public void setApiClient(TestpressCourseApiClient api) {
        this.apiClient = api;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    @Override
    protected Object getId(Chapter resource) {
        return resource.getId();
    }

    @Override
    public RetrofitCall<TestpressApiResponse<Chapter>> getItems(int page, int size) {
        queryParams.put(TestpressApiClient.PAGE, page);
        if (parentId != null) {
            queryParams.put(PARENT, parentId);
        }
        return apiClient.getChapters(courseId, queryParams, latestModifiedDate);
    }

    @Override
    protected Chapter register(Chapter chapter) {
        if (chapter != null && chapter.getModified() != null && !chapter.getModified().isEmpty()) {
            try {
                chapter.setModifiedDate(simpleDateFormat.parse(chapter.getModified()).getTime());
            } catch (ParseException e) {
                e.printStackTrace();
                return null;
            }
        }
        return chapter;
    }

}
