package in.testpress.course.ui;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import java.util.List;
import in.testpress.core.TestpressSdk;
import in.testpress.course.R;
import in.testpress.ui.WebViewActivity;
import in.testpress.models.greendao.Course;
import in.testpress.util.ImageUtils;
import in.testpress.util.SingleTypeAdapter;

class CourseListAdapter extends SingleTypeAdapter<Course> {

    private final Activity mActivity;
    private ImageLoader mImageLoader;
    private DisplayImageOptions mOptions;
    private String productSlug;

    CourseListAdapter(Activity activity, List<Course> courses, String productSlug) {
        super(activity.getLayoutInflater(), R.layout.testpress_course_list_item);
        mActivity = activity;
        mImageLoader = ImageUtils.initImageLoader(activity);
        mOptions = ImageUtils.getPlaceholdersOption();
        this.productSlug = productSlug;
        setItems(courses);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[] { R.id.course_title, R.id.thumbnail_image, R.id.percentage,
                R.id.course_item_layout,  R.id.progress_bar_layout, R.id.external_link_title,
                R.id.total_chapters, R.id.total_contents, R.id.validity
        };
    }

    @Override
    protected void update(final int position, final Course course) {
        setFont(new int[]{0, 2, 6, 7, 8}, TestpressSdk.getRubikMediumFont(mActivity));
        setText(0, course.getTitle());
        showOrHideCourseImage(course);
        showChapterAndContentCounts(mActivity, course);
        showOrHideExternalLinkLabel(course);
        initializeItemClickListener(course);
        // ToDo: Set completed percentage in the progress bar
        setGone(4, true);
    }

    private void showOrHideCourseImage(Course course) {
        if (course.getImage() == null || course.getImage().isEmpty()) {
            setGone(1, true);
        } else {
            setGone(1, false);
            mImageLoader.displayImage(course.getImage(), imageView(1), mOptions);
        }
    }
    private void showChapterAndContentCounts(Activity activity, Course course) {
        setText(6,  activity.getResources().getQuantityString(R.plurals.chapters_count,
                course.getChaptersCount(), course.getChaptersCount()));
        setText(7,  activity.getResources().getQuantityString(R.plurals.contents_count,
                course.getContentsCount(), course.getContentsCount()));
    }

    private void showOrHideExternalLinkLabel(Course course) {
        if (!course.getExternal_link_label().equals("")) {
            setText(5, course.getExternal_link_label());
        }
        setGone(5, !course.isCourseForRegistration());
    }

    private void initializeItemClickListener(Course course) {
        view(3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCourseContentsOrExternalLink(mActivity, course, !course.isCourseForRegistration());
            }
        });
    }

    public void openCourseContentsOrExternalLink(Activity activity, Course course, boolean openCourseContent) {

        if (openCourseContent) {
            activity.startActivity(ChapterDetailActivity.createIntent(
                    course.getTitle(),
                    course.getId().toString(),
                    activity, this.productSlug));
        } else {
            Intent intent = new Intent(activity, WebViewActivity.class);
            intent.putExtra("URL", course.getExternal_content_link());
            intent.putExtra("TITLE", course.getExternal_link_label());
            activity.startActivity(intent);
        }
    }

}
