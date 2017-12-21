package in.testpress.course.network;

import java.io.IOException;
import java.text.ParseException;

import in.testpress.models.greendao.Chapter;
import in.testpress.models.TestpressApiResponse;
import in.testpress.network.BaseDatabaseModelPager;
import in.testpress.network.TestpressApiClient;
import retrofit2.Response;

public class ChapterPager extends BaseDatabaseModelPager<Chapter> {

    private TestpressCourseApiClient apiClient;
    private String courseId;

    public ChapterPager(String courseId, TestpressCourseApiClient apiClient) {
        super();
        this.courseId = courseId;
        this.apiClient = apiClient;
    }

    @Override
    protected Object getId(Chapter resource) {
        return resource.getId();
    }

    @Override
    public Response<TestpressApiResponse<Chapter>> getItems(int page, int size) throws IOException {
        queryParams.put(TestpressApiClient.PAGE, page);
        return apiClient.getChapters(courseId, queryParams, latestModifiedDate).execute();
    }

    @Override
    protected Chapter register(Chapter chapter) {
        if (chapter != null) {
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
