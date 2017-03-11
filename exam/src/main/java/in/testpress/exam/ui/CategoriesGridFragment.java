package in.testpress.exam.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSdk;
import in.testpress.exam.R;
import in.testpress.exam.models.Category;
import in.testpress.exam.network.CategoryPager;
import in.testpress.exam.network.TestpressExamApiClient;
import in.testpress.ui.BaseGridFragment;
import in.testpress.util.ImageUtils;
import in.testpress.util.UIUtils;

public class CategoriesGridFragment extends BaseGridFragment<Category> {

    public static final String PARENT_ID = "parentId";
    private TestpressExamApiClient mApiClient;
    private String parentId = "null";
    private DisplayImageOptions mOptions;
    private ImageLoader mImageLoader;

    public static void show(FragmentActivity activity, int containerViewId) {
        activity.getSupportFragmentManager().beginTransaction()
                .replace(containerViewId, new CategoriesGridFragment())
                .commitAllowingStateLoss();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().getString(PARENT_ID) != null) {
            parentId = getArguments().getString(PARENT_ID);
        }
        mApiClient = new TestpressExamApiClient(getActivity());
        mOptions = ImageUtils.getPlaceholdersOption();
        mImageLoader = ImageUtils.initImageLoader(getActivity());
    }

    @Override
    protected CategoryPager getPager() {
        if (pager == null) {
            pager = new CategoryPager(parentId, mApiClient);
        }
        return (CategoryPager)pager;
    }

    @Override
    protected View getChildView(final Category category, ViewGroup parent) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.testpress_category_grid_item,
                parent, false);
        TextView name = (TextView) view.findViewById(R.id.title);
        ImageView thumbnailImage = (ImageView) view.findViewById(R.id.thumbnail_image);
        name.setText(category.getName());
        mImageLoader.displayImage(category.getImage(), thumbnailImage, mOptions);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (category.getLeaf()) {
                    Intent intent = new Intent(getActivity(), ExamsListActivity.class);
                    intent.putExtra(ExamsListFragment.CATEGORY, category.getSlug());
                    getActivity().startActivity(intent);
                } else {
                    getActivity().startActivity(
                            CategoryGridActivity.createIntent(category.getName(),
                                    category.getId().toString(), category.getSlug(), getContext()));
                }
            }
        });
        name.setTypeface(TestpressSdk.getRubikMediumFont(getContext()));
        return view;
    }

    @Override
    protected TableRow.LayoutParams getLayoutParams() {
        return new TableRow.LayoutParams(getChildColumnWidth(), TableRow.LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected int getChildColumnWidth() {
        return (int)UIUtils.getPixelFromDp(getContext(), 118);
    }

    @Override
    protected int getErrorMessage(TestpressException exception) {
        if (exception.isUnauthenticated()) {
            setEmptyText(R.string.testpress_authentication_failed, R.string.testpress_please_login,
                    R.drawable.ic_error_outline_black_18dp);
            return R.string.testpress_authentication_failed;
        } else if (exception.isNetworkError()) {
            setEmptyText(R.string.testpress_network_error, R.string.testpress_no_internet_try_again,
                    R.drawable.ic_error_outline_black_18dp);
            return R.string.testpress_no_internet_try_again;
        } else {
            setEmptyText(R.string.testpress_error_loading_categories,
                    R.string.testpress_some_thing_went_wrong_try_again,
                    R.drawable.ic_error_outline_black_18dp);
        }
        return R.string.testpress_some_thing_went_wrong_try_again;
    }

    @Override
    protected void setEmptyText() {
        setEmptyText(R.string.testpress_no_categories, R.string.testpress_no_categories_description,
                    R.drawable.ic_error_outline_black_18dp);
    }

}
