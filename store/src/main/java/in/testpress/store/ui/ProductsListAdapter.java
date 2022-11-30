package in.testpress.store.ui;

import android.app.Activity;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.Arrays;
import java.util.List;

import in.testpress.store.R;
import in.testpress.store.models.Product;
import in.testpress.util.ImageUtils;
import in.testpress.util.SingleTypeAdapter;

public class ProductsListAdapter extends SingleTypeAdapter<Product> {

    private Activity mActivity;
    private ImageLoader mImageLoader;
    private DisplayImageOptions mOptions;

    ProductsListAdapter(Activity activity, final List<Product> items) {
        super(activity, R.layout.testpress_products_list_item);
        mActivity = activity;
        mImageLoader = ImageUtils.initImageLoader(activity);
        mOptions = ImageUtils.getPlaceholdersOption();
        setItems(items);
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[] { R.id.title, R.id.total_exams, R.id.total_notes, R.id.price,
                R.id.categories, R.id.thumbnail_image };
    }

    @Override
    protected void update(final int position, final Product item) {
        setText(0, item.getTitle());
        if(item.getExamsCount() == 0 ){
            setGone(1, true);
        } else {
            setText(1, mActivity.getResources().getQuantityString(R.plurals.exams_count,
                    item.getExamsCount(), item.getExamsCount()));

            setGone(1, false);
        }
        if(item.getNotesCount() == 0 ){
            setGone(2, true);
        } else {
            setText(2,  mActivity.getResources().getQuantityString(R.plurals.documents_count,
                    item.getNotesCount(), item.getNotesCount()));

            setGone(2, false);
        }
        setText(3, "₹ " + item.getPrice());
        String categories = Arrays.toString(item.getCategories().toArray());
        setText(4, categories.substring(1, categories.length() - 1));
        if (item.getImages().size() > 0) {
            ImageView imageView = view(5);
            mImageLoader.displayImage(item.getImages().get(0).getMedium(), imageView, mOptions);
        }
    }
}