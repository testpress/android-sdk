package in.testpress.course.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.loader.content.Loader;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.greenrobot.greendao.AbstractDao;

import java.util.List;

import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSDKDatabase;
import in.testpress.course.R;
import in.testpress.course.pagers.ChapterPager;
import in.testpress.course.api.TestpressCourseApiClient;
import in.testpress.models.greendao.Chapter;
import in.testpress.models.greendao.ChapterDao;
import in.testpress.models.greendao.Course;
import in.testpress.models.greendao.CourseDao;
import in.testpress.models.greendao.Product;
import in.testpress.models.greendao.ProductDao;
import in.testpress.network.BaseResourcePager;
import in.testpress.store.ui.ProductDetailsActivity;
import in.testpress.ui.BaseDataBaseFragment;
import in.testpress.util.SingleTypeAdapter;

import static in.testpress.course.TestpressCourse.COURSE_ID;
import static in.testpress.course.TestpressCourse.PARENT_ID;
import static in.testpress.course.TestpressCourse.PRODUCT_SLUG;
import static in.testpress.store.TestpressStore.STORE_REQUEST_CODE;

public class ChaptersListFragment extends BaseDataBaseFragment<Chapter, Long> {

    private TestpressCourseApiClient apiClient;
    private String courseId;
    private String parentId;
    private ChapterDao chapterDao;
    private CourseDao courseDao;
    private String productSlug;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        storeArgs();
        apiClient = new TestpressCourseApiClient(getActivity());
        chapterDao = TestpressSDKDatabase.getChapterDao(getActivity());
        courseDao = TestpressSDKDatabase.getCourseDao(getActivity());
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.course_preview_layout, container, false);
    }

    private void storeArgs() {
        courseId = getArguments().getString(COURSE_ID);
        productSlug = getArguments().getString(PRODUCT_SLUG);

        if (getArguments().getString(PARENT_ID) != null) {
            parentId = getArguments().getString(PARENT_ID);
        }

        if (getArguments() == null || courseId == null || courseId.isEmpty()) {
            throw new IllegalArgumentException("COURSE_ID must not be null or empty");
        }
    }
    
    @Override
    public void onViewCreated(View view,
                              Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        swipeRefreshLayout.setEnabled(false);

        if (productSlug != null) {
            displayBuyNowButton();
        }

        if (isItemsEmpty()) {
            showLoadingPlaceholder();
        }
    }

    private void displayBuyNowButton() {
        ProductDao productDao = TestpressSDKDatabase.getProductDao(getContext());
        Button buyButton = requireView().findViewById(R.id.buy_button);
        buyButton.setVisibility(View.VISIBLE);
        List<Product> products = productDao.queryBuilder().where(ProductDao.Properties.Slug.eq(productSlug)).list();
        if (!products.isEmpty()) {
            Product product = products.get(0);
            float price = Float.parseFloat(product.getCurrentPrice());

            if (price > 0.0) {
                buyButton.setText(R.string.buy_now);
            } else {
                buyButton.setText(R.string.get_it_for_free);
            }
        }

        buyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(requireContext(), ProductDetailsActivity.class);
                intent.putExtra(ProductDetailsActivity.PRODUCT_SLUG, productSlug);
                requireActivity().startActivity(intent);
            }
        });
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
            setEmptyText(R.string.testpress_error_loading_contents,
                    R.string.testpress_some_thing_went_wrong_try_again,
                    R.drawable.ic_error_outline_black_18dp);
        }
        return R.string.testpress_some_thing_went_wrong_try_again;
    }

    @Override
    protected void setEmptyText() {
        setEmptyText(R.string.testpress_no_content, R.string.testpress_no_content_description,
                R.drawable.ic_error_outline_black_18dp);
    }

    @Override
    protected SingleTypeAdapter<Chapter> createAdapter(List<Chapter> items) {
        return new ChaptersListAdapter(getActivity(), getCourse(), parentId, productSlug);
    }

    private Course getCourse() {
        return courseDao.queryBuilder().where(CourseDao.Properties.Id.eq(courseId)).list().get(0);
    }


    @Override
    protected BaseResourcePager<Chapter> getPager() {
        if (pager == null) {
            pager = new ChapterPager(courseId, apiClient);
        }
        return pager;
    }

    @Override
    protected boolean isItemsEmpty() {
        return getCourse().getRootChapters().isEmpty();
    }

    private void deleteExistingChapters() {
        getDao().queryBuilder()
                .where(ChapterDao.Properties.CourseId.eq(courseId))
                .buildDelete()
                .executeDeleteWithoutDetachingEntities();
        getDao().detachAll();
    }

    @Override
    public void onLoadFinished(Loader<List<Chapter>> loader,
                               List<Chapter> items) {
        super.onLoadFinished(loader, items);

        if (!items.isEmpty()) {
            deleteExistingChapters();
            getDao().insertOrReplaceInTx(items);
        }
        hideLoadingPlaceholder();
        swipeRefreshLayout.setEnabled(true);
    }

    @Override
    protected AbstractDao<Chapter, Long> getDao() {
        return chapterDao;
    }
}
