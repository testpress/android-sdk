package in.testpress.store.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.FragmentActivity;
import android.view.View;
import android.widget.ListView;

import java.util.List;

import in.testpress.core.TestpressException;
import in.testpress.network.BaseResourcePager;
import in.testpress.store.R;
import in.testpress.store.models.Product;
import in.testpress.store.network.ProductsPager;
import in.testpress.store.network.StoreApiClient;
import in.testpress.ui.PagedItemFragment;
import in.testpress.util.SingleTypeAdapter;

import static in.testpress.store.TestpressStore.STORE_REQUEST_CODE;

public class ProductListFragment extends PagedItemFragment<Product> {

    protected StoreApiClient apiClient;

    public static void show(FragmentActivity activity, int containerViewId) {
        activity.getSupportFragmentManager().beginTransaction()
                .replace(containerViewId, new ProductListFragment())
                .commitAllowingStateLoss();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        apiClient = new StoreApiClient(getActivity());
        pager = new ProductsPager(apiClient);
        super.onCreate(savedInstanceState);
    }

    protected BaseResourcePager<Product> getPager() {
        return pager;
    }

    @Override
    protected SingleTypeAdapter<Product> createAdapter(List<Product> items) {
        return new ProductsListAdapter(getActivity(), items);
    }

    public void onListItemClick(ListView l, View v, int position, long id) {
        Product product = ((Product) l.getItemAtPosition(position));
        Intent intent = new Intent(getActivity(), ProductDetailsActivity.class);
        intent.putExtra(ProductDetailsActivity.PRODUCT_SLUG, product.getSlug());
        getActivity().startActivityForResult(intent, STORE_REQUEST_CODE);
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
        }
        setEmptyText(R.string.testpress_error_loading_products,
                R.string.testpress_some_thing_went_wrong_try_again,
                R.drawable.ic_error_outline_black_18dp);
        return R.string.testpress_some_thing_went_wrong_try_again;
    }

    @Override
    protected void setEmptyText() {
        setEmptyText(R.string.testpress_no_products, R.string.testpress_no_products_description,
                    R.drawable.testpress_box);
    }
}
