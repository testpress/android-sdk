package in.testpress.course.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.loader.content.Loader;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import org.greenrobot.greendao.AbstractDao;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToDoubleBiFunction;

import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSDKDatabase;
import in.testpress.core.TestpressSdk;
import in.testpress.course.R;
import in.testpress.course.pagers.ChapterPager;
import in.testpress.course.api.TestpressCourseApiClient;
import in.testpress.course.util.UIUtils;
import in.testpress.models.InstituteSettings;
import in.testpress.models.greendao.Chapter;
import in.testpress.models.greendao.ChapterDao;
import in.testpress.models.greendao.Course;
import in.testpress.models.greendao.CourseDao;
import in.testpress.network.BaseResourcePager;
import in.testpress.network.RetrofitCall;
import in.testpress.ui.BaseDataBaseFragment;
import in.testpress.util.Permission;
import in.testpress.util.SingleTypeAdapter;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;

import static in.testpress.course.TestpressCourse.COURSE_ID;
import static in.testpress.course.TestpressCourse.PARENT_ID;
import static in.testpress.course.TestpressCourse.PRODUCT_SLUG;

public class ChaptersListFragment extends BaseDataBaseFragment<Chapter, Long> {

    private TestpressCourseApiClient apiClient;
    private String courseId;
    private String parentId;
    private ChapterDao chapterDao;
    private CourseDao courseDao;
    private String productSlug;
    private Course course;
    private RetrofitCall<Course> courseApiRequest;
    private InstituteSettings instituteSettings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        storeArgs();
        apiClient = new TestpressCourseApiClient(getActivity());
        chapterDao = TestpressSDKDatabase.getChapterDao(getActivity());
        courseDao = TestpressSDKDatabase.getCourseDao(getActivity());
        instituteSettings = TestpressSdk.getTestpressSession(requireContext()).getInstituteSettings();
        List<Course> courses = courseDao.queryBuilder().where(CourseDao.Properties.Id.eq(courseId)).list();
        if (!courses.isEmpty()) {
            course = courses.get(0);
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.course_preview_layout, container, false);
    }

    private void storeArgs() {
        courseId = getArguments().getString(COURSE_ID);
        productSlug = getArguments().getString(PRODUCT_SLUG);

        if (getArguments().getString(PARENT_ID) != null) {
            parentId = getArguments().getString(PARENT_ID);
        }

        if (getArguments() == null || courseId == null || courseId.isEmpty()) {
            throw new IllegalArgumentException("COURSE_ID must not be null or empty");
        }
    }
    
    @Override
    public void onViewCreated(View view,
                              Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        swipeRefreshLayout.setEnabled(false);

        if (productSlug != null) {
            displayBuyNowButton();
        }

        if(getCourse() == null) {
            getLoaderManager().destroyLoader(0);
            showLoadingPlaceholder();
            fetchCourseAndShowChapters(courseId);
        }

        if (getCourse() != null && isItemsEmpty()) {
            showLoadingPlaceholder();
        }
        setHasOptionsMenu(productSlug == null && isCustomTestGenerationEnabled());
    }

    private void fetchCourseAndShowChapters(String courseId) {
        courseApiRequest = new TestpressCourseApiClient(getContext()).getCourse(courseId)
                .enqueue(new TestpressCallback<Course>() {
                    @Override
                    public void onSuccess(Course fetchedCourse) {
                        course = fetchedCourse;
                        course.setIsMyCourse(true);
                        courseDao.insertOrReplace(course);
                        configureList(getActivity(), getListView());
                        refreshWithProgress();
                    }

                    @Override
                    public void onException(TestpressException exception) {
                        getErrorMessage(exception);
                    }
                });
    }

    private void displayBuyNowButton() {
        Button buyButton = requireView().findViewById(R.id.buy_button);
        UIUtils.displayBuyNowButton(buyButton, productSlug, requireContext());
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
        return new ChaptersListAdapter(getActivity(), getCourse(), parentId, productSlug);
    }

    private Course getCourse() {
        return course;
    }


    @Override
    protected BaseResourcePager<Chapter> getPager() {
        if (pager == null) {
            pager = new ChapterPager(courseId, apiClient);
        }
        return pager;
    }

    @Override
    protected boolean isItemsEmpty() {
        if (getCourse() == null) {
            return true;
        }
        return getCourse().getRootChapters().isEmpty();
    }

    private void deleteExistingChapters() {
        getDao().queryBuilder()
                .where(ChapterDao.Properties.CourseId.eq(courseId))
                .buildDelete()
                .executeDeleteWithoutDetachingEntities();
        getDao().detachAll();
    }

    @Override
    public void onLoadFinished(Loader<List<Chapter>> loader,
                               List<Chapter> items) {
        super.onLoadFinished(loader, items);

        if (!items.isEmpty()) {
            deleteExistingChapters();
            getDao().insertOrReplaceInTx(items);
        }
        hideLoadingPlaceholder();
        swipeRefreshLayout.setEnabled(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.custom_test_generation, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private boolean isCustomTestGenerationEnabled() {
        return instituteSettings.getEnableCustomTest() && parentId == null
                && course.getAllowCustomTestGeneration() != null && course.getAllowCustomTestGeneration();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.custom_test_icon) {
            openCustomTestGenerationActivity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected AbstractDao<Chapter, Long> getDao() {
        return chapterDao;
    }

    private void openCustomTestGenerationActivity() {
        startActivity(
                CustomTestGenerationActivity.Companion.createIntent(
                        requireContext(),
                        "Custom Module",
                        "/courses/custom_test_generation/?course_id="+courseId+"%26testpress_app=android",
                        true,
                        CustomTestGenerationActivity.class
                )
        );
    }
}
