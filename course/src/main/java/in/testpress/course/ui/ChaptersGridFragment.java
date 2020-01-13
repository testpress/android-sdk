package in.testpress.course.ui;

import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.greenrobot.greendao.query.QueryBuilder;
import org.greenrobot.greendao.query.WhereCondition;

import java.util.List;

import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSDKDatabase;
import in.testpress.core.TestpressSdk;
import in.testpress.course.R;
import in.testpress.models.greendao.Chapter;
import in.testpress.models.greendao.ChapterDao;
import in.testpress.course.network.ChapterPager;
import in.testpress.course.network.TestpressCourseApiClient;
import in.testpress.ui.BaseGridFragment;
import in.testpress.util.ImageUtils;
import in.testpress.util.UIUtils;

import static in.testpress.course.TestpressCourse.COURSE_ID;
import static in.testpress.course.TestpressCourse.PARENT_ID;

public class ChaptersGridFragment extends BaseGridFragment<Chapter> {

    private TestpressCourseApiClient mApiClient;
    private String courseId;
    private String parentId = "null";
    private DisplayImageOptions mOptions;
    private ImageLoader mImageLoader;
    private ChapterDao chapterDao;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        courseId =  getArguments().getString(COURSE_ID);
        if (getArguments() == null || courseId == null || courseId.isEmpty()) {
            throw new IllegalArgumentException("COURSE_ID must not be null or empty");
        }
        if (getArguments().getString(PARENT_ID) != null) {
            parentId = getArguments().getString(PARENT_ID);
        }
        mApiClient = new TestpressCourseApiClient(getActivity());
        mOptions = ImageUtils.getPlaceholdersOption();
        mImageLoader = ImageUtils.initImageLoader(getActivity());
        chapterDao = TestpressSDKDatabase.getChapterDao(getActivity());
    }

    @Override
    protected void initItems() {
        if (getParentChaptersQueryBuilder().count() != 0) {
            showGrid();
        }
        getLoaderManager().initLoader(0, null, this);
        displayItems();
        firstCallBack = false;
    }

    private QueryBuilder<Chapter> getCourseChaptersQueryBuilder() {
        return chapterDao.queryBuilder().where(ChapterDao.Properties.CourseId.eq(courseId),
                ChapterDao.Properties.Active.eq(true));
    }

    private QueryBuilder<Chapter> getParentChaptersQueryBuilder() {
        WhereCondition parentCondition;
        if (parentId.equals("null")) {
            parentCondition = ChapterDao.Properties.ParentId.isNull();
        } else {
            parentCondition = ChapterDao.Properties.ParentId.eq(parentId);
        }
        return getCourseChaptersQueryBuilder().where(parentCondition);
    }

    @Override
    protected List<Chapter> getItems() {
        return getParentChaptersQueryBuilder().orderAsc(ChapterDao.Properties.Order).list();
    }

    @Override
    protected ChapterPager getPager() {
        if (pager == null) {
            QueryBuilder<Chapter> courseChaptersQueryBuilder = getCourseChaptersQueryBuilder();
            if (parentId.equals("null") && courseChaptersQueryBuilder.count() == 0) {
                pager = new ChapterPager(courseId, mApiClient);
            } else {
                pager = new ChapterPager(courseId, parentId, mApiClient);
                QueryBuilder<Chapter> parentChaptersQueryBuilder = getParentChaptersQueryBuilder();
                if (parentChaptersQueryBuilder.count() > 0) {
                    Chapter latestChapter = parentChaptersQueryBuilder
                            .orderDesc(ChapterDao.Properties.ModifiedDate)
                            .list().get(0);

                    ((ChapterPager) pager).setLatestModifiedDate(latestChapter.getModified());
                }

            }
        }
        return (ChapterPager)pager;
    }

    @Override
    public void onLoadFinished(final Loader<List<Chapter>> loader, final List<Chapter> items) {
        final TestpressException exception = getException(loader);
        if (exception != null) {
            this.exception = exception;
            int errorMessage = getErrorMessage(exception);
            if (!isItemsEmpty()) {
                showError(errorMessage);
            }
            showGrid();
            getLoaderManager().destroyLoader(loader.getId());
            return;
        }

        this.exception = null;
        this.items = items;
        if (!items.isEmpty()) {
            chapterDao.insertOrReplaceInTx(items);
        }
        displayItems();
        showGrid();
    }
    @Override
    protected boolean isItemsEmpty() {
        return getParentChaptersQueryBuilder().count() == 0;
    }

    @Override
    protected View getChildView(final Chapter chapter, ViewGroup parent) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.testpress_chapter_grid_item,
                parent, false);
        TextView name = (TextView) view.findViewById(R.id.title);
        ImageView thumbnailImage = (ImageView) view.findViewById(R.id.thumbnail_image);
        name.setText(chapter.getName());
        if (chapter.getImage() == null || chapter.getImage().isEmpty()) {
            thumbnailImage.setVisibility(View.GONE);
        } else {
            thumbnailImage.setVisibility(View.VISIBLE);
            mImageLoader.displayImage(chapter.getImage(), thumbnailImage, mOptions);
        }
        View lock = view.findViewById(R.id.lock);
        View whiteForeground = view.findViewById(R.id.white_foreground);
        if (chapter.getIsLocked()) {
            lock.setVisibility(View.VISIBLE);
            whiteForeground.setVisibility(View.VISIBLE);
        } else {
            lock.setVisibility(View.GONE);
            whiteForeground.setVisibility(View.GONE);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().startActivity(ChapterDetailActivity.createIntent(
                            chapter.getUrl(),
                            getContext(), null)
                    );
                }
            });
        }
        name.setTypeface(TestpressSdk.getRubikMediumFont(getContext()));
        return view;
    }

    @Override
    protected TableRow.LayoutParams getLayoutParams() {
        return new TableRow.LayoutParams(getChildColumnWidth(), TableRow.LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected int getChildColumnWidth() {
        return (int)UIUtils.getPixelFromDp(getContext(), 118);
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
            setEmptyText(R.string.testpress_error_loading_chapters,
                    R.string.testpress_some_thing_went_wrong_try_again,
                    R.drawable.ic_error_outline_black_18dp);
        }
        return R.string.testpress_some_thing_went_wrong_try_again;
    }

    @Override
    protected void setEmptyText() {
        setEmptyText(R.string.testpress_no_chapter, R.string.testpress_no_chapter_description,
                    R.drawable.ic_error_outline_black_18dp);
    }

}
