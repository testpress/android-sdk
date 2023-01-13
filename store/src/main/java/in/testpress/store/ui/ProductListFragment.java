package in.testpress.store.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import in.testpress.core.TestpressException;
import in.testpress.network.BaseResourcePager;
import in.testpress.store.ProductCategoriesViewModel;
import in.testpress.store.R;
import in.testpress.store.adapter.ProductCategoriesAdapter;
import in.testpress.store.models.Product;
import in.testpress.store.network.ProductsPager;
import in.testpress.store.network.StoreApiClient;
import in.testpress.store.repository.ProductCategoriesRepository;
import in.testpress.ui.PagedItemFragment;
import in.testpress.util.SingleTypeAdapter;

import static in.testpress.store.TestpressStore.STORE_REQUEST_CODE;

public class ProductListFragment extends PagedItemFragment<Product> {

    protected StoreApiClient apiClient;
    private ProductCategoriesViewModel viewModel;
    private ProductCategoriesAdapter productCategoriesAdapter;

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
        viewModel = new ProductCategoriesViewModel(new ProductCategoriesRepository(requireContext()));
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        productCategoriesListView.setVisibility(View.VISIBLE);
        productCategoriesAdapter = new ProductCategoriesAdapter(requireContext());
        productCategoriesListView.setLayoutManager(new LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false));

productCategoriesListView.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        Toast.makeText(requireContext(), "Hi", Toast.LENGTH_SHORT).show();
    }
});
        productCategoriesListView.setAdapter(productCategoriesAdapter);
        initViewModel();
    }

    private void initViewModel(){
        initalizeObservers();
        viewModel.loadContents();
    }

    private void initalizeObservers(){
        viewModel.getItems().observe(getViewLifecycleOwner(),resource -> {
            switch (resource.getStatus()) {
                case LOADING:

                    break;
                case SUCCESS:
                    productCategoriesAdapter.addProductCategories(Objects.requireNonNull(resource.getData()));
                    break;
                case ERROR:

                    break;
            }
        });
    }

    protected BaseResourcePager<Product> getPager() {
        return pager;
    }

    @Override
    protected SingleTypeAdapter<Product> createAdapter(List<Product> items) {
        return new ProductsListAdapter(getActivity(), items);
    }

    @Override
    public void updateItems(List<Product> items) {

        super.updateItems(items);
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
