package in.testpress.course.ui;

import android.app.Activity;
import android.view.View;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import in.testpress.core.TestpressSdk;
import in.testpress.course.R;
import in.testpress.course.models.Course;
import in.testpress.util.ImageUtils;
import in.testpress.util.SingleTypeAdapter;

class CourseListAdapter extends SingleTypeAdapter<Course> {

    private final Activity mActivity;
    private ImageLoader mImageLoader;
    private DisplayImageOptions mOptions;

    CourseListAdapter(Activity activity, final List<Course> items, int layout) {
        super(activity.getLayoutInflater(), layout);
        mActivity = activity;
        mImageLoader = ImageUtils.initImageLoader(activity);
        mOptions = ImageUtils.getPlaceholdersOption();
        setItems(items);
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[] { R.id.course_title, R.id.thumbnail_image, R.id.percentage,
                R.id.course_item_layout };
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
                mActivity.startActivity(ChaptersGridActivity.createIntent(course.getTitle(),
                        course.getChaptersUrlFrag(), null, mActivity));
            }
        });
        // ToDo: Set completed percentage in the progress bar
    }

}
