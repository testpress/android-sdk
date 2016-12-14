package in.testpress.course.ui;

import android.app.Activity;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import in.testpress.core.TestpressSdk;
import in.testpress.course.R;
import in.testpress.course.models.Content;
import in.testpress.util.ImageUtils;
import in.testpress.util.SingleTypeAdapter;

class ContentsListAdapter extends SingleTypeAdapter<Content> {

    private final Activity mActivity;
    private ImageLoader mImageLoader;
    private DisplayImageOptions mOptions;

    ContentsListAdapter(Activity activity, final List<Content> items, int layout) {
        super(activity.getLayoutInflater(), layout);
        mActivity = activity;
        mImageLoader = ImageUtils.initImageLoader(activity);
        mOptions = ImageUtils.getPlaceholdersOption();
        setItems(items);
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[] { R.id.content_title, R.id.thumbnail_image, R.id.white_foreground,
                R.id.lock, R.id.content_item_layout };
    }

    @Override
    protected void update(final int position, final Content content) {
        textView(0).setTypeface(TestpressSdk.getRubikMediumFont(mActivity));
        setText(0, content.getName());
        if (content.getImage() == null || content.getImage().isEmpty()) {
            setGone(1, true);
        } else {
            setGone(1, false);
            mImageLoader.displayImage(content.getImage(), imageView(1), mOptions);
        }
        if (content.getIsLocked() || !content.getHasStarted()) {
            setGone(2, false);
            setGone(3, false);
        } else {
            setGone(2, true);
            setGone(3, true);
        }
    }

}
