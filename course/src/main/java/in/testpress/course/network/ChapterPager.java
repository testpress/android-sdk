package in.testpress.course.network;

import java.io.IOException;
import java.text.ParseException;

import in.testpress.core.TestpressRetrofitRequest;
import in.testpress.models.greendao.Chapter;
import in.testpress.models.TestpressApiResponse;
import in.testpress.network.BaseDatabaseModelPager;
import in.testpress.network.RetrofitCall;
import in.testpress.network.TestpressApiClient;
import retrofit2.Response;

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

    @Override
    protected Object getId(Chapter resource) {
        return resource.getId();
    }

    @Override
    public Response<TestpressApiResponse<Chapter>> getItems(int page, int size) throws IOException {
        queryParams.put(TestpressApiClient.PAGE, page);
        if (parentId != null) {
            queryParams.put(PARENT, parentId);
        }
        return apiClient.getChapters(courseId, queryParams, latestModifiedDate).execute();
    }

    public TestpressRetrofitRequest<Chapter> getRetrofitCall() {
        return new TestpressRetrofitRequest<Chapter>() {
            @Override
            public RetrofitCall<TestpressApiResponse<Chapter>> getRetrofitCall(
                    int page, int size) {
                queryParams.put(TestpressApiClient.PAGE, page);
                if (parentId != null) {
                    queryParams.put(PARENT, parentId);
                }
                return apiClient.getChapters(courseId, queryParams, latestModifiedDate);
            }
        };
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
