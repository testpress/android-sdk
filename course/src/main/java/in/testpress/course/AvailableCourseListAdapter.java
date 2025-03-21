package in.testpress.course;

import static in.testpress.store.TestpressStore.STORE_REQUEST_CODE;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import in.testpress.core.TestpressSDKDatabase;
import in.testpress.core.TestpressSdk;
import in.testpress.course.ui.ChapterDetailActivity;
import in.testpress.course.ui.CoursePreviewActivity;
import in.testpress.models.greendao.Course;
import in.testpress.models.greendao.CourseDao;
import in.testpress.models.greendao.Product;
import in.testpress.models.greendao.ProductDao;
import in.testpress.store.TestpressStore;
import in.testpress.store.ui.ProductDetailsActivity;
import in.testpress.util.ImageUtils;
import in.testpress.util.SingleTypeAdapter;


public class AvailableCourseListAdapter extends SingleTypeAdapter<Product> {

    private Activity activity;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private CourseDao courseDao;
    private ProductDao productDao;
    private int chaptersCount;
    private int contentsCount;

    public AvailableCourseListAdapter(Activity activity, final List<Product> items) {
        super(activity, R.layout.available_course_list_item);
        this.activity = activity;
        this.imageLoader = ImageUtils.initImageLoader(activity);
        this.options = ImageUtils.getPlaceholdersOption();
        courseDao = TestpressSDKDatabase.getCourseDao(activity);
        productDao = TestpressSDKDatabase.getProductDao(activity);
        setItems(items);
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[] { R.id.title, R.id.total_chapters, R.id.total_contents, R.id.price,
                R.id.thumbnail_image, R.id.product_item_layout, R.id.counts };
    }

    private void calculateChaptersAndContentsCount(Product product) {
        List<Course> courses = courseDao.queryBuilder().where(CourseDao.Properties.Id.in(product.getCourseIds())).list();
        chaptersCount = 0;
        contentsCount = 0;

        for (Course course: courses) {
            chaptersCount += course.getChaptersCount();
            contentsCount += course.getContentsCount();
        }
    }

    @Override
    protected void update(final int position, final Product item) {
        setFont(new int[]{0, 1, 2, 3}, TestpressSdk.getRubikMediumFont(activity));
        setText(0, item.getTitle());

        calculateChaptersAndContentsCount(item);
        setText(1,  activity.getResources().getQuantityString(R.plurals.chapters_count,
                chaptersCount, chaptersCount));
        setText(2,  activity.getResources().getQuantityString(R.plurals.contents_count,
                contentsCount, contentsCount));

        setGone(6,item.getCourseIds().size() == 0);

        String price = String.format("₹%s", item.getCurrentPrice());
        setText(3, price);

        ImageView imageView = view(4);
        imageLoader.displayImage(item.getImage(), imageView, options);
        addOnClickListeners(item);
    }

    private void addOnClickListeners(final Product product) {
        view(5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProductDetail(product);
            }
        });
    }

    private void showProductDetail(Product product) {
        if (product.getCourseIds().size() > 1) {
            activity.startActivity(CoursePreviewActivity.createIntent(product.getCourseIds(), activity, product.getSlug()));
        } else if (product.getCourseIds().size() == 1 ) {
            openChapters(product, activity);
        } else {
            Intent intent = new Intent(activity, ProductDetailsActivity.class);
            intent.putExtra(ProductDetailsActivity.PRODUCT_SLUG, product.getSlug());
            activity.startActivityForResult(intent, STORE_REQUEST_CODE);
        }
//        Intent intent = new Intent(activity, ProductDetailsActivity.class);
//        intent.putExtra(ProductDetailsActivity.PRODUCT_SLUG, product.getSlug());
//        activity.startActivityForResult(intent, TestpressStore.STORE_REQUEST_CODE);
    }

    private void openChapters(Product product, Activity activity) {
        activity.startActivity(ChapterDetailActivity.createIntent(
                product.getTitle(),
                product.getCourseIds().get(0).toString(),
                activity, product.getSlug()));
    }

}