package in.testpress.course.ui;

import android.app.Activity;
import android.view.View;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import in.testpress.core.TestpressSdk;
import in.testpress.course.R;
import in.testpress.models.greendao.Course;
import in.testpress.models.greendao.CourseDao;
import in.testpress.util.ImageUtils;
import in.testpress.util.SingleTypeAdapter;

class CourseListAdapter extends SingleTypeAdapter<Course> {

    private final Activity mActivity;
    private ImageLoader mImageLoader;
    private DisplayImageOptions mOptions;
    private CourseDao mCourseDao;

    CourseListAdapter(Activity activity, CourseDao courseDao) {
        super(activity.getLayoutInflater(), R.layout.testpress_course_list_item);
        mActivity = activity;
        mImageLoader = ImageUtils.initImageLoader(activity);
        mOptions = ImageUtils.getPlaceholdersOption();
        mCourseDao = courseDao;
    }

    @Override
    public int getCount() {
        return (int) mCourseDao.queryBuilder().count();
    }

    @Override
    public Course getItem(int position) {
        return mCourseDao.queryBuilder().orderAsc(CourseDao.Properties.Order).listLazy().get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[] { R.id.course_title, R.id.thumbnail_image, R.id.percentage,
                R.id.course_item_layout,  R.id.progress_bar_layout};
    }

    @Override
    protected void update(final int position, final Course course) {
        setFont(new int[]{0, 2}, TestpressSdk.getRubikMediumFont(mActivity));
        setText(0, course.getTitle());
        if (course.getImage() == null || course.getImage().isEmpty()) {
            setGone(1, true);
        } else {
            setGone(1, false);
            mImageLoader.displayImage(course.getImage(), imageView(1), mOptions);
        }
        view(3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (course.getChaptersCount() > 0) {
                    mActivity.startActivity(ChaptersGridActivity.createIntent(course.getTitle(),
                            course.getId().toString(), null, mActivity));
                } else {
                    mActivity.startActivity(ContentsListActivity.createIntent(course.getTitle(),
                            course.getContentsUrl(), mActivity));
                }
            }
        });
        // ToDo: Set completed percentage in the progress bar
        setGone(4, true);
    }

}
