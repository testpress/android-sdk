package in.testpress.course.ui;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;

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
import in.testpress.models.greendao.Course;
import in.testpress.models.greendao.CourseDao;
import in.testpress.network.BaseResourcePager;
import in.testpress.ui.BaseGridFragment;
import in.testpress.util.ImageUtils;
import in.testpress.util.UIUtils;

import static in.testpress.course.TestpressCourse.CHAPTER_ID;
import static in.testpress.course.TestpressCourse.COURSE_ID;
import static in.testpress.course.TestpressCourse.PARENT_CHAPTER_ID;

public class NewChaptersGridFragment extends BaseGridFragment<Chapter> {

    private long courseId;
    String parentChapterId = "null";
    private ImageLoader mImageLoader;

    public static NewChaptersGridFragment getInstance(long courseId, Long parentChapterId) {
        NewChaptersGridFragment fragment = new NewChaptersGridFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(COURSE_ID, courseId);
        if (parentChapterId != null && parentChapterId != 0) {
            bundle.putString(PARENT_CHAPTER_ID, parentChapterId.toString());
        }
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        assert getArguments() != null;
        courseId =  getArguments().getLong(COURSE_ID);
        if (getArguments().getString(PARENT_CHAPTER_ID) != null) {
            parentChapterId = getArguments().getString(PARENT_CHAPTER_ID);
        }
        mImageLoader = ImageUtils.initImageLoader(getActivity());
    }

    @Override
    protected void initItems() {
        if (parentChapterId.equals("null")) {
            List<Course> courses = TestpressSDKDatabase.getCourseDao(getContext()).queryBuilder()
                    .where(CourseDao.Properties.Id.eq(courseId))
                    .list();

            if (!courses.isEmpty()) {
                //noinspection ConstantConditions
                ((ExpandableContentsActivity) getActivity())
                        .setActionBarTitle(courses.get(0).getTitle());
            }
        } else {
            List<Chapter> chapters = TestpressSDKDatabase.getChapterDao(getContext()).queryBuilder()
                    .where(ChapterDao.Properties.Id.eq(parentChapterId))
                    .list();

            if (!chapters.isEmpty()) {
                //noinspection ConstantConditions
                ((ExpandableContentsActivity) getActivity())
                        .setActionBarTitle(chapters.get(0).getName());
            }
        }

        if (getParentChaptersQueryBuilder().count() != 0) {
            showGrid();
        }
        displayItems();
        firstCallBack = false;
        swipeRefreshLayout.setEnabled(false);
    }

    private QueryBuilder<Chapter> getParentChaptersQueryBuilder() {
        WhereCondition parentCondition;
        if (parentChapterId.equals("null")) {
            parentCondition = ChapterDao.Properties.ParentId.isNull();
        } else {
            parentCondition = ChapterDao.Properties.ParentId.eq(parentChapterId);
        }
        return Chapter.getAllActiveChaptersQueryBuilder(getContext(), courseId)
                .where(parentCondition);
    }

    @Override
    protected List<Chapter> getItems() {
        return getParentChaptersQueryBuilder().orderAsc(ChapterDao.Properties.Order).list();
    }

    @Override
    protected boolean isItemsEmpty() {
        return getParentChaptersQueryBuilder().count() == 0;
    }

    @Override
    protected View getChildView(final Chapter chapter, ViewGroup parent) {
        View view = getLayoutInflater().inflate(R.layout.testpress_chapter_grid_item, parent, false);
        TextView name = view.findViewById(R.id.title);
        ImageView thumbnailImage = view.findViewById(R.id.thumbnail_image);
        name.setText(chapter.getName());
        if (chapter.getImage() == null || chapter.getImage().isEmpty()) {
            thumbnailImage.setVisibility(View.GONE);
        } else {
            thumbnailImage.setVisibility(View.VISIBLE);
            mImageLoader.displayImage(chapter.getImage(), thumbnailImage);
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
                    if (chapter.getRawChildrenCount(getContext()) > 0) {
                        displayChildChapters(chapter.getId());
                    } else {
                        displayContents(chapter.getId());
                    }
                }
            });
        }
        assert getContext() != null;
        name.setTypeface(TestpressSdk.getRubikMediumFont(getContext()));
        return view;
    }

    void displayChildChapters(Long parentId) {
        this.parentChapterId = String.valueOf(parentId);
        initItems();
    }

    private void displayContents(long chapterId) {
        ContentsListFragment fragment = new ContentsListFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(CHAPTER_ID, chapterId);
        fragment.setArguments(bundle);
        assert getFragmentManager() != null;
        getFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment)
                .commitAllowingStateLoss();
    }

    @Override
    protected TableRow.LayoutParams getLayoutParams() {
        return new TableRow.LayoutParams(getChildColumnWidth(), TableRow.LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected int getChildColumnWidth() {
        assert getContext() != null;
        return (int) UIUtils.getPixelFromDp(getContext(), 118);
    }

    @Override
    protected int getErrorMessage(TestpressException exception) {
        return R.string.testpress_network_error;
    }

    @Override
    protected void setEmptyText() {
        setEmptyText(R.string.testpress_no_chapter, R.string.testpress_no_chapter_description,
                R.drawable.ic_error_outline_black_18dp);
    }

    @Override
    protected BaseResourcePager<Chapter> getPager() {
        return null;
    }
}
