package in.testpress.store;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import in.testpress.core.TestpressSdk;
import in.testpress.core.TestpressSession;
import in.testpress.store.ui.ProductDetailsActivity;
import in.testpress.store.ui.ProductListFragment;
import in.testpress.store.ui.ProductsListActivity;
import in.testpress.util.Assert;
import in.testpress.util.ImageUtils;

public class TestpressStore {

    public static final int STORE_REQUEST_CODE = 1000;
    public static final String CONTINUE_PURCHASE = "continue_purchase";
    public static final String PAYMENT_SUCCESS = "payment_success";

    /**
     * Use when testpress store need to be open in a container as a fragment.
     *
     * <p> Usage example:
     *
     * <p> TestpressSdk.initialize(getActivity(), instituteSettings, "userId", "accessToken", provider,
     * <p>             new TestpressCallback/<TestpressSession>() {
     * <p>             @Override
     * <p>             public void onSuccess(TestpressSession testpressSession) {
     * <p>                 <b>TestpressStore.show(this, R,id.fragment_container, testpressSession);</b>
     * <p>             }
     * <p> });
     *
     * @param activity Activity that has the container
     * @param containerViewId Container view id in which fragment needs to be replace
     * @param testpressSession TestpressSession got from the core module
     */
    public static void show(@NonNull FragmentActivity activity,
                            @NonNull @IdRes Integer containerViewId,
                            @NonNull TestpressSession testpressSession) {

        Assert.assertNotNull("Activity must not be null.", activity);
        Assert.assertNotNull("containerViewId must not be null.", containerViewId);

        init(activity.getApplicationContext(), testpressSession);
        ProductListFragment.show(activity, containerViewId);
    }

    /**
     * Use when testpress exam need to be open as a new Activity.
     *
     * <p> Usage example:
     *
     * <p> TestpressSdk.initialize(this, instituteSettings, "userId", "accessToken", provider,
     * <p>             new TestpressCallback/<TestpressSession>() {
     * <p>             @Override
     * <p>             public void onSuccess(TestpressSession testpressSession) {
     * <p>                 <b>TestpressStore.show(this, testpressSession);</b>
     * <p>             }
     * <p> });
     *
     * @param activity Activity from which the product list needs to display.
     * @param testpressSession TestpressSession got from the core module.
     */
    public static void show(@NonNull Activity activity, @NonNull TestpressSession testpressSession) {
        Assert.assertNotNull("Activity must not be null.", activity);

        init(activity.getApplicationContext(), testpressSession);
        Intent intent = new Intent(activity, ProductsListActivity.class);
        intent = populateIntent(intent, activity);
        activity.startActivityForResult(intent, STORE_REQUEST_CODE);
    }

    /**
     * Use to show a particular product.
     *
     * @param activity activity from which product detail needs to show.
     * @param productSlug Slug of the product which need to be show.
     * @param testpressSession TestpressSession got from the core module.
     */
    @SuppressWarnings("ConstantConditions")
    public static void showProduct(@NonNull Activity activity, @NonNull String productSlug,
                                   @NonNull TestpressSession testpressSession) {

        Assert.assertNotNull("Activity must not be null.", activity);
        Assert.assertNotNullAndNotEmpty("productSlug must not be null or empty.", productSlug);

        init(activity.getApplicationContext(), testpressSession);
        Intent intent = new Intent(activity, ProductDetailsActivity.class);
        intent.putExtra(ProductDetailsActivity.PRODUCT_SLUG, productSlug);
        activity.startActivityForResult(intent, STORE_REQUEST_CODE);
    }

    private static void init(Context applicationContext, TestpressSession testpressSession) {
        Assert.assertNotNull("TestpressSession must not be null.", testpressSession);

        TestpressSdk.setTestpressSession(applicationContext, testpressSession);
        ImageUtils.initImageLoader(applicationContext);
    }

    private static Intent populateIntent(Intent intent, Activity activity){

        if (activity.getIntent().getStringExtra("title") != "" &&
                activity.getIntent().getStringExtra("title") != null) {
            intent.putExtra("title", activity.getIntent().getStringExtra("title"));
        }

        return intent;
    }
}
