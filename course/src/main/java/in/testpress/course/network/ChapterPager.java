package in.testpress.course.network;

import java.text.ParseException;

import in.testpress.models.TestpressApiResponse;
import in.testpress.models.greendao.Chapter;
import in.testpress.network.BaseDatabaseModelPager;
import in.testpress.network.RetrofitCall;
import in.testpress.network.TestpressApiClient;

import static in.testpress.network.TestpressApiClient.PARENT;
import static in.testpress.network.TestpressApiClient.UNFILTERED;

public class ChapterPager extends BaseDatabaseModelPager<Chapter> {

    private TestpressCourseApiClient apiClient;
    private long courseId;
    private String parentId;

    public ChapterPager(long courseId, TestpressCourseApiClient apiClient) {
        this(courseId, null, apiClient);
    }

    public ChapterPager(long courseId, String parentId, TestpressCourseApiClient apiClient) {
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
    public RetrofitCall<TestpressApiResponse<Chapter>> getItems(int page, int size) {
        queryParams.put(TestpressApiClient.PAGE, page);
        if (parentId != null) {
            queryParams.put(PARENT, parentId);
        }
        queryParams.put(UNFILTERED, true);
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
