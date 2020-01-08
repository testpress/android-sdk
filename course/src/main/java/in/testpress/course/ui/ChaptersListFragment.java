package in.testpress.course.ui;

import android.os.Bundle;
import android.support.v4.content.Loader;
import android.util.Log;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSDKDatabase;
import in.testpress.course.R;
import in.testpress.course.network.ChapterPager;
import in.testpress.course.network.TestpressCourseApiClient;
import in.testpress.models.greendao.Chapter;
import in.testpress.models.greendao.ChapterDao;
import in.testpress.models.greendao.Course;
import in.testpress.models.greendao.CourseDao;
import in.testpress.network.BaseResourcePager;
import in.testpress.ui.BaseDataBaseFragment;
import in.testpress.util.SingleTypeAdapter;

import static in.testpress.course.TestpressCourse.COURSE_ID;
import static in.testpress.course.TestpressCourse.PARENT_ID;

public class ChaptersListFragment extends BaseDataBaseFragment<Chapter, Long> {

    private TestpressCourseApiClient apiClient;
    private String courseId;
    private String parentId = "null";
    private ChapterDao chapterDao;
    private CourseDao courseDao;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        storeArgs();
        apiClient = new TestpressCourseApiClient(getActivity());
        chapterDao = TestpressSDKDatabase.getChapterDao(getActivity());
        courseDao = TestpressSDKDatabase.getCourseDao(getActivity());
    }

    private void storeArgs() {
        courseId = getArguments().getString(COURSE_ID);

        if (getArguments().getString(PARENT_ID) != null) {
            parentId = getArguments().getString(PARENT_ID);
        }

        if (getArguments() == null || courseId == null || courseId.isEmpty()) {
            throw new IllegalArgumentException("COURSE_ID must not be null or empty");
        }
    }

    @Override
    protected int getErrorMessage(TestpressException exception) {
        if (exception.isUnauthenticated()) {
            setEmptyText(R.string.testpress_authentication_failed, R.string.testpress_please_login,
                    R.drawable.ic_error_outline_black_18dp);
            return R.string.testpress_authentication_failed;
        } else if (exception.isNetworkError()) {
            setEmptyText(R.string.testpress_network_error, R.string.testpress_no_internet_try_again,
                    R.drawable.ic_error_outline_black_18dp);
            return R.string.testpress_no_internet_try_again;
        } else {
            setEmptyText(R.string.testpress_error_loading_contents,
                    R.string.testpress_some_thing_went_wrong_try_again,
                    R.drawable.ic_error_outline_black_18dp);
        }
        return R.string.testpress_some_thing_went_wrong_try_again;
    }

    @Override
    protected void setEmptyText() {
        setEmptyText(R.string.testpress_no_content, R.string.testpress_no_content_description,
                R.drawable.ic_error_outline_black_18dp);
    }

    @Override
    protected SingleTypeAdapter<Chapter> createAdapter(List<Chapter> items) {
        return new ChaptersListAdapter(getActivity(), courseId, parentId);
    }

    private void setLastModifiedForPager() {
        QueryBuilder<Chapter> parentChaptersQueryBuilder = Chapter.getParentChaptersQueryBuilder(getContext(), courseId, parentId);

        if (parentChaptersQueryBuilder.count() > 0) {
            Chapter latestChapter = parentChaptersQueryBuilder
                    .orderDesc(ChapterDao.Properties.ModifiedDate)
                    .listLazy().get(0);

            ((ChapterPager) pager).setLatestModifiedDate(latestChapter.getModified());
        }
    }

    private Course getCourse() {
        return courseDao.queryBuilder().where(CourseDao.Properties.Id.eq(courseId)).list().get(0);
    }


    @Override
    protected BaseResourcePager<Chapter> getPager() {
        if (pager == null) {
            Course course = getCourse();

            if (parentId.equals("null") && course.hasChapters()) {
                pager = new ChapterPager(courseId, apiClient);
            } else {
                pager = new ChapterPager(courseId, parentId, apiClient);
                setLastModifiedForPager();
            }
        }
        return pager;
    }

    @Override
    protected AbstractDao<Chapter, Long> getDao() {
        return chapterDao;
    }
}
