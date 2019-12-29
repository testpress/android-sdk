package in.testpress.course;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import in.testpress.core.TestpressSDKDatabase;
import in.testpress.course.ui.ChapterDetailActivity;
import in.testpress.course.ui.CoursePreviewActivity;
import in.testpress.models.greendao.Course;
import in.testpress.models.greendao.CourseDao;
import in.testpress.models.greendao.Product;
import in.testpress.course.R;
import in.testpress.store.ui.ProductDetailsActivity;
import in.testpress.util.ImageUtils;
import in.testpress.util.SingleTypeAdapter;

import static in.testpress.store.TestpressStore.STORE_REQUEST_CODE;

public class AvailableCourseListAdapter extends SingleTypeAdapter<Product> {

    private Activity activity;
    private ImageLoader mImageLoader;
    private DisplayImageOptions mOptions;
    private CourseDao courseDao;

    public AvailableCourseListAdapter(Activity activity, final List<Product> items) {
        super(activity, R.layout.available_course_list_item);
        this.activity = activity;
        mImageLoader = ImageUtils.initImageLoader(activity);
        mOptions = ImageUtils.getPlaceholdersOption();
        courseDao = TestpressSDKDatabase.getCourseDao(activity);
        setItems(items);
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[] { R.id.title, R.id.total_chapters, R.id.total_contents, R.id.price,
                R.id.categories, R.id.thumbnail_image, R.id.product_item_layout, R.id.view_details };
    }

    @Override
    protected void update(final int position, final Product item) {
        List<Course> courses = courseDao.queryBuilder().where(CourseDao.Properties.Id.in(item.getCourseIds())).list();

        int chaptersCount = 0;
        int contentsCount = 0;
        for (Course course: courses) {
            chaptersCount += course.getChaptersCount();
            contentsCount += course.getContentsCount();
        }

        String price = String.format("₹%s", item.getCurrentPrice());

        setText(0, item.getTitle());
        setText(1,  activity.getResources().getQuantityString(R.plurals.chapters_count,
                chaptersCount, chaptersCount));
        setGone(1, false);
        setText(2,  activity.getResources().getQuantityString(R.plurals.contents_count,
                contentsCount, contentsCount));
        setGone(2, false);
        setText(3, price);
        view(7).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            Intent intent = new Intent(activity, ProductDetailsActivity.class);
            intent.putExtra(ProductDetailsActivity.PRODUCT_SLUG, item.getSlug());
            activity.startActivityForResult(intent, STORE_REQUEST_CODE);
            }
        });
        view(6).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.startActivity(CoursePreviewActivity.createIntent(
                        item.getCourseIds(),
                        activity, item.getSlug()));
            }
        });
        ImageView imageView = view(5);
        mImageLoader.displayImage(item.getImage(), imageView, mOptions);
    }
}