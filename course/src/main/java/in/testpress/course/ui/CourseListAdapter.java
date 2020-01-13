package in.testpress.course.ui;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

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
    private List<Course> courses;
    private String productSlug;

    CourseListAdapter(Activity activity, CourseDao courseDao, List<Course> courses, String productSlug) {
        super(activity.getLayoutInflater(), R.layout.testpress_course_list_item);
        mActivity = activity;
        mImageLoader = ImageUtils.initImageLoader(activity);
        mOptions = ImageUtils.getPlaceholdersOption();
        mCourseDao = courseDao;
        this.courses = courses;
        this.productSlug = productSlug;
    }

    @Override
    public int getCount() {
        if (!courses.isEmpty()) {
            return courses.size();
        }

        return (int) mCourseDao.queryBuilder().where(CourseDao.Properties.IsMyCourse.eq(true)).count();
    }

    @Override
    public Course getItem(int position) {
        if (!courses.isEmpty()) {
            return courses.get(position);
        }

        return mCourseDao.queryBuilder()
                .where(CourseDao.Properties.IsMyCourse.eq(true))
                .orderAsc(CourseDao.Properties.Order)
                .listLazy().get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[] { R.id.course_title, R.id.thumbnail_image, R.id.percentage,
                R.id.course_item_layout,  R.id.progress_bar_layout, R.id.external_link_title};
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

        setTextToTextView(course.getExternal_link_label(), (TextView) view(5));
        toggleTextViewVisibility(!course.isCourseForRegistration(), view(5));

        view(3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCourseContentsOrExternalLink(mActivity, course, !course.isCourseForRegistration());
            }
        });
        // ToDo: Set completed percentage in the progress bar
        setGone(4, true);
    }

    public void toggleTextViewVisibility(boolean toHide, View view) {
        if (toHide) {
            view.setVisibility(View.GONE);
        } else {
            view.setVisibility(View.VISIBLE);
        }
    }

    public void setTextToTextView(String textViewText, TextView textView) {
        if (!textViewText.equals("")) {
            textView.setText(textViewText);
        }
    }

    public void openCourseContentsOrExternalLink(Activity activity, Course course, boolean openCourseContent) {

        if (openCourseContent) {
            activity.startActivity(ChapterDetailActivity.createIntent(
                    course.getTitle(),
                    course.getId().toString(),
                    activity));
        } else {
            Intent intent = new Intent(activity, WebViewActivity.class);
            intent.putExtra("URL", course.getExternal_content_link());
            intent.putExtra("TITLE", course.getExternal_link_label());
            activity.startActivity(intent);
        }
    }

}
