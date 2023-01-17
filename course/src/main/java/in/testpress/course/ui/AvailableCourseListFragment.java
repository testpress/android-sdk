package in.testpress.course.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSDKDatabase;
import in.testpress.course.AvailableCourseListAdapter;
import in.testpress.course.R;
import in.testpress.course.adapter.CategoriesListener;
import in.testpress.course.adapter.ProductCategoriesAdapter;
import in.testpress.course.enums.CourseType;
import in.testpress.course.pagers.CourseProductPager;
import in.testpress.course.repository.ProductCategoriesRepository;
import in.testpress.course.util.ManageCourseStates;
import in.testpress.database.entities.ProductCategoryEntity;
import in.testpress.models.greendao.Course;
import in.testpress.models.greendao.CourseDao;
import in.testpress.models.greendao.Product;
import in.testpress.models.greendao.ProductDao;
import in.testpress.course.viewmodels.ProductCategoriesViewModel;
import in.testpress.store.network.StoreApiClient;
import in.testpress.ui.BaseListViewFragment;
import in.testpress.util.SingleTypeAdapter;
import in.testpress.util.ThrowableLoader;

public class AvailableCourseListFragment extends BaseListViewFragment<Product> implements CategoriesListener {

    private CourseDao courseDao;
    private CourseProductPager pager;
    private ProductDao productDao;
    protected StoreApiClient apiClient;
    private ProductCategoriesViewModel viewModel;
    private ProductCategoriesAdapter productCategoriesAdapter;
    private List<Product> productList = new ArrayList<>();

    public static void show(FragmentActivity activity, int containerViewId) {
        activity.getSupportFragmentManager().beginTransaction()
                .replace(containerViewId, new AvailableCourseListFragment())
                .commitAllowingStateLoss();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiClient = new StoreApiClient(getActivity());
        courseDao = TestpressSDKDatabase.getCourseDao(getActivity());
        productDao = TestpressSDKDatabase.getProductDao(getContext());
        pager = new CourseProductPager(apiClient);
        viewModel = new ProductCategoriesViewModel(new ProductCategoriesRepository(requireContext()));
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        productCategoriesAdapter = new ProductCategoriesAdapter(requireContext(),this);
        productCategoriesListView.setLayoutManager(new LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false));
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
                    if (resource.getData() != null){
                        productCategoriesAdapter.setProductCategories(resource.getData());
                        productCategoriesAdapter.notifyDataSetChanged();
                        productCategoriesLayout.setVisibility(View.VISIBLE);
                    }
                    break;
                case ERROR:
                    productCategoriesLayout.setVisibility(View.GONE);
                    break;
            }
        });
    }

    @Override
    public void refreshWithProgress() {
        pager.reset();
        viewModel.loadContents();
        productCategoriesAdapter.setSelectedButton(0);
        super.refreshWithProgress();
    }

    @Override
    public Loader<List<Product>> onCreateLoader(int id, Bundle args) {
        return new ProductsLoader(getContext(), pager, courseDao);
    }


    private static class ProductsLoader extends ThrowableLoader<List<Product>> {

        private CourseProductPager pager;
        private CourseDao courseDao;

        ProductsLoader(Context context, CourseProductPager pager, CourseDao courseDao) {
            super(context, null);
            this.pager = pager;
            this.courseDao = courseDao;
        }

        @Override
        public List<Product> loadData() throws TestpressException {
            do {
                pager.next();
                List<Course> courses = pager.getListResponse().getCourses();

                ManageCourseStates manageCourseStates = new ManageCourseStates(CourseType.PRODUCT_COURSE, courseDao);
                manageCourseStates.updateCoursesWithLocalState(courses);
                courseDao.insertOrReplaceInTx(courses);
            } while (pager.hasNext());
            return pager.getResources();
        }
    }

    @Override
    protected SingleTypeAdapter<Product> createAdapter(List<Product> items) {
        return new AvailableCourseListAdapter(getActivity(), items);
    }


    private void deleteAndInsertProductsInDB(List<Product> products) {
        productDao.deleteAll();

        if (!products.isEmpty()) {
            productDao.insertOrReplaceInTx(products);
        }
    }

    @Override
    public void onLoadFinished(Loader<List<Product>> loader, List<Product> products) {
        final TestpressException exception = getException(loader);
        this.exception = exception;
        swipeRefreshLayout.setRefreshing(false);

        if (exception != null) {
            handleException(exception);
            getLoaderManager().destroyLoader(loader.getId());

        } else {
            this.items = products;
            productList = products;
            getListAdapter().getWrappedAdapter().setItems(products);
            deleteAndInsertProductsInDB(products);
            showList();
            getListAdapter().notifyDataSetChanged();
        }

        if(isItemsEmpty()) {
            setEmptyText();
        }
    }


    private void handleException(TestpressException exception) {
        int errorMessage = getErrorMessage(exception);

        if (!isItemsEmpty()) {
            showError(errorMessage);
        }
    }

    @Override
    protected boolean isItemsEmpty() {
        return productDao.queryBuilder().listLazy().isEmpty();
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
            setEmptyText(R.string.testpress_error_loading_courses,
                    R.string.testpress_some_thing_went_wrong_try_again,
                    R.drawable.ic_error_outline_black_18dp);
        }
        return R.string.testpress_some_thing_went_wrong_try_again;
    }

    @Override
    protected void setEmptyText() {
        setEmptyText(R.string.no_products_available, R.string.check_again_later,
                R.drawable.ic_error_outline_black_18dp);
    }

    @Override
    public void invoke(@NonNull ProductCategoryEntity productCategories) {
        List<Product> filteredProduct = new ArrayList<>();
        for (Product product : productList){
            if (product.getId() == 118 && productCategories.getId() == 0){
                filteredProduct.add(product);
            }
        }
        updateItems(filteredProduct);
    }
}
