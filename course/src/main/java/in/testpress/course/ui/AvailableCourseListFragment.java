package in.testpress.course.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.Loader;
import android.view.View;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSDKDatabase;
import in.testpress.course.AvailableCourseListAdapter;
import in.testpress.course.R;
import in.testpress.course.network.TestpressCourseApiClient;
import in.testpress.models.greendao.Course;
import in.testpress.models.greendao.CourseDao;
import in.testpress.models.greendao.Product;
import in.testpress.models.greendao.ProductDao;
import in.testpress.store.network.ProductsPager;
import in.testpress.store.network.TestpressStoreApiClient;
import in.testpress.ui.BaseListViewFragment;
import in.testpress.util.SingleTypeAdapter;
import in.testpress.util.ThrowableLoader;

public class AvailableCourseListFragment extends BaseListViewFragment<Product> {

    private TestpressCourseApiClient mApiClient;
    private CourseDao courseDao;
    private ProductsPager refreshPager;
    private ProductDao productDao;
    protected TestpressStoreApiClient apiClient;
    private ProductsPager pager;

    public static void show(FragmentActivity activity, int containerViewId) {
        activity.getSupportFragmentManager().beginTransaction()
                .replace(containerViewId, new AvailableCourseListFragment())
                .commitAllowingStateLoss();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApiClient = new TestpressCourseApiClient(getActivity());
        apiClient = new TestpressStoreApiClient(getActivity());
        courseDao = TestpressSDKDatabase.getCourseDao(getActivity());
        productDao = TestpressSDKDatabase.getProductDao(getContext());
    }


    private ProductsPager getRefreshPager() {
        if (refreshPager == null) {
            refreshPager = new ProductsPager(apiClient);
            QueryBuilder<Product> queryBuilder = getQueryBuilder();
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
        return new ProductsLoader(getContext(), getRefreshPager(), courseDao);
    }


    private static class ProductsLoader extends ThrowableLoader<List<Product>> {

        private ProductsPager pager;
        private CourseDao courseDao;

        ProductsLoader(Context context, ProductsPager pager, CourseDao courseDao) {
            super(context, null);
            this.pager = pager;
            this.courseDao = courseDao;
        }

        @Override
        public List<Product> loadData() throws TestpressException {
            pager.next();
            List<Course> courses = pager.getListResponse().getCourses();
            for (Course course: courses) {
                course.setIsProduct(true);
                courseDao.insertOrReplace(course);
            }
            courseDao.insertOrReplaceInTx(courses);
            return pager.getListResponse().getProducts();
        }
    }

    @Override
    protected SingleTypeAdapter<Product> createAdapter(List<Product> items) {
        return new AvailableCourseListAdapter(getActivity(), items);
    }

    private QueryBuilder<Product> getQueryBuilder() {
        return Product.getQueryBuilder(getContext());
    }



    @Override
    public void onLoadFinished(Loader<List<Product>> loader, List<Product> products) {
        final TestpressException exception = getException(loader);

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
        getListAdapter().notifyDataSetChanged();
        getLoaderManager().destroyLoader(loader.getId());
        showList();
    }


    private void saveItems(ProductsPager pager) {
        List<Product> products = pager.getResources();
        ProductDao productDao = TestpressSDKDatabase.getProductDao(getContext());
        CourseDao courseDao = TestpressSDKDatabase.getCourseDao(getContext());
        for (int i = 0; i < products.size(); i++) {
            Product product = products.get(i);
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
        } else {
            setEmptyText(R.string.testpress_error_loading_courses,
                    R.string.testpress_some_thing_went_wrong_try_again,
                    R.drawable.ic_error_outline_black_18dp);
        }
        return R.string.testpress_some_thing_went_wrong_try_again;
    }

    @Override
    protected void setEmptyText() {
        setEmptyText(R.string.testpress_no_courses, R.string.testpress_no_courses_description,
                    R.drawable.ic_error_outline_black_18dp);
    }

    @Override
    public void refreshWithProgress() {
        items.clear();
        refreshPager.reset();
        super.refreshWithProgress();
    }

    public void refresh() {
        getLoaderManager().restartLoader(0, null, this);
    }


}
