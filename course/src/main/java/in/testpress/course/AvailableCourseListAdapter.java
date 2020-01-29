package in.testpress.course;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
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
import in.testpress.store.ui.ProductDetailsActivity;
import in.testpress.util.ImageUtils;
import in.testpress.util.IntegerList;
import in.testpress.util.SingleTypeAdapter;

import static in.testpress.store.TestpressStore.STORE_REQUEST_CODE;


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
    public Product getItem(int position) {
        return productDao.queryBuilder().listLazy().get(position);
    }

    @Override
    public int getCount() {
        return (int) productDao.queryBuilder().count();
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
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

        if (item.getCourseIds().size() == 0) {
            setGone(6, true);
        }

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
                openCoursesOrChapters(product);
            }
        });
    }

    private void openCoursesOrChapters(Product product) {
        if (product.getCourseIds().size() > 1) {
            openCoursesList(product.getCourseIds(), product.getSlug());
        } else if (product.getCourseIds().size() == 1) {
            openChapters(product.getCourseIds().get(0), product.getSlug());
        } else {
            Intent intent = new Intent(activity, ProductDetailsActivity.class);
            intent.putExtra(ProductDetailsActivity.PRODUCT_SLUG, product.getSlug());
            activity.startActivityForResult(intent, STORE_REQUEST_CODE);
        }
    }

    private void openCoursesList(IntegerList courseIds, String productSlug) {
        activity.startActivity(CoursePreviewActivity.createIntent(courseIds, activity, productSlug));
    }

    private void openChapters(Integer courseId, String productSlug) {
        List<Course> courses = courseDao.queryBuilder().where(CourseDao.Properties.Id.in(courseId)).list();
        Course course = courses.get(0);
        activity.startActivity(ChapterDetailActivity.createIntent(
                course.getTitle(),
                course.getId().toString(),
                activity, productSlug));
    }

}