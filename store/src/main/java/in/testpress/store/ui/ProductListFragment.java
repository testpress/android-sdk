package in.testpress.store.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSDKDatabase;
import in.testpress.models.greendao.ProductDao;
import in.testpress.store.R;
import in.testpress.models.greendao.Product;
import in.testpress.store.network.ProductsPager;
import in.testpress.store.network.TestpressStoreApiClient;
import in.testpress.ui.BaseListViewFragment;
import in.testpress.util.SingleTypeAdapter;
import in.testpress.util.ThrowableLoader;
import in.testpress.util.ViewUtils;

import static in.testpress.store.TestpressStore.STORE_REQUEST_CODE;

public class ProductListFragment extends BaseListViewFragment<Product> {

    protected TestpressStoreApiClient apiClient;
    private ProductsPager refreshPager;
    private ProductDao productDao;
    private ProductsPager pager;

    public static void show(FragmentActivity activity, int containerViewId) {
        activity.getSupportFragmentManager().beginTransaction()
                .replace(containerViewId, new ProductListFragment())
                .commitAllowingStateLoss();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        apiClient = new TestpressStoreApiClient(getActivity());
        pager = new ProductsPager(apiClient);
        productDao = TestpressSDKDatabase.getProductDao(getContext());
        super.onCreate(savedInstanceState);
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


    private QueryBuilder<Product> getQueryBuilder() {
        return Product.getQueryBuilder(getContext());
    }

    private ProductsPager getRefreshPager() {
        if (refreshPager == null) {
            refreshPager = new ProductsPager(apiClient);
            QueryBuilder<Product> queryBuilder = getQueryBuilder();
            if (queryBuilder.count() > 0) {
            }
        }
        return refreshPager;
    }

    protected void displayDataFromDB() {
        getListAdapter().notifyDataSetChanged();

        if (isItemsEmpty()) {
            setEmptyText();
            retryButton.setVisibility(View.GONE);
        }
    }

    @Override
    public Loader<List<Product>> onCreateLoader(int id, Bundle args) {
        return new ProductsLoader(getContext(), getRefreshPager());
    }

    private static class ProductsLoader extends ThrowableLoader<List<Product>> {

        private ProductsPager pager;

        ProductsLoader(Context context, ProductsPager pager) {
            super(context, null);
            this.pager = pager;
        }

        @Override
        public List<Product> loadData() throws TestpressException {
            pager.next();
            Log.d("ProductListFragment", "onLoadFinished: Products" + pager.getListResponse().getCourses());
            Log.d("ProductListFragment", "onLoadFinished: Product 1" + pager.getListResponse().getPrices().get(0).getProductId());
            return pager.getListResponse().getProducts();
        }
    }


    @Override
    public void onLoadFinished(Loader<List<Product>> loader, List<Product> products) {
        final TestpressException exception = getException(loader);
        getActivity().getSupportLoaderManager().destroyLoader(loader.getId());
        if (exception != null) {
            this.exception = exception;
            int errorMessage = getErrorMessage(exception);
            if (!isItemsEmpty()) {
                showError(errorMessage);
            }
            showList();
            getLoaderManager().destroyLoader(loader.getId());
            return;
        }

        getListAdapter().getWrappedAdapter().setItems(products);
        this.exception = null;
        this.items = products;
        productDao.deleteAll();
        if (!products.isEmpty()) {
            productDao.insertOrReplaceInTx(products);
        }
        showList();
    }

    private void saveItems(ProductsPager pager) {
        List<Product> products = pager.getResources();
        ProductDao productDao = TestpressSDKDatabase.getProductDao(getContext());
        for (int i = 0; i < products.size(); i++) {
            Product product = products.get(i);
            List<Product> productFromDB = productDao.queryBuilder()
                    .where(ProductDao.Properties.Id.eq(product.getId())).list();
            Log.d("ProductListFragment", "saveItems: " + product.getCourseIds());
            productDao.insertOrReplace(product);
        }

    }

    @Override
    protected boolean isItemsEmpty() {
        return productDao.count() == 0;
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

    @Override
    public void refreshWithProgress() {
        items.clear();
        pager.reset();
        super.refreshWithProgress();
    }

    public void refresh() {
        getActivity().getSupportLoaderManager().restartLoader(0, null, this);
    }

}
