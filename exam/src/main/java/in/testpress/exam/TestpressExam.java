package in.testpress.exam;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import in.testpress.exam.ui.AuthenticateFragment;
import in.testpress.exam.ui.CarouselFragment;
import in.testpress.exam.ui.ExamsListActivity;

public class TestpressExam {

    /**
     * Use when testpress exam need to be open in a container as a fragment
     * & already initialized TestpressSdk.
     *
     * <p> Usage example:
     *
     * <p> TestpressSdk.initialize(getActivity(), "baseUrl", "username", "password",
     * <p>             new TestpressCallback/<TestpressAuthToken>() {
     * <p>             @Override
     * <p>             public void onSuccess(TestpressAuthToken response) {
     * <p>                 <b>TestpressExam.show(this, R,id.fragment_container);</b>
     * <p>             }
     * <p> });
     *
     * @param activity Activity that has the container
     * @param containerViewId Container view id in which fragment needs to be replace
     */
    public static void show(@NonNull FragmentActivity activity, @NonNull @IdRes Integer containerViewId) {
        initImageLoader(activity);
        CarouselFragment.show(activity, containerViewId);
    }

    /**
     * Use when testpress exam need to be open in a container as a fragment
     * & not yet initialized TestpressSdk.
     *
     * <p> Usage example:
     *
     * <p> TestpressExam.show(this, R,id.fragment_container, "http://demo.testpress.in", "username", "password");
     *
     * @param activity Activity that has the container
     * @param containerViewId Container view id in which fragment needs to be replace
     * @param baseUrl Base url of institute
     * @param username Username
     * @param password Password
     */
    public static void show(@NonNull FragmentActivity activity, @NonNull @IdRes Integer containerViewId,
                            @NonNull String baseUrl, @NonNull String username, @NonNull String password) {
        initImageLoader(activity);
        AuthenticateFragment.show(activity, containerViewId, baseUrl, username, password);
    }

    /**
     * Use when testpress exam need to be open as a new Activity
     * & already initialized TestpressSdk.
     *
     * <p> Usage example:
     *
     * <p> TestpressSdk.initialize(getActivity(), "baseUrl", "username", "password",
     * <p>             new TestpressCallback/<TestpressAuthToken>() {
     * <p>             @Override
     * <p>             public void onSuccess(TestpressAuthToken response) {
     * <p>                 <b>TestpressExam.show(this);</b>
     * <p>             }
     * <p> });
     *
     * @param context Context to start the new activity.
     */
    public static void show(@NonNull Context context) {
        initImageLoader(context);
        Intent intent = new Intent(context, ExamsListActivity.class);
        context.startActivity(intent);
    }

    /**
     * Use when testpress exam need to be open as a new Activity
     * & not yet initialized TestpressSdk.
     *
     * <p> Usage example:
     *
     * <p> TestpressExam.show(this, "http://demo.testpress.in", "username", "password");
     *
     * @param context Context to start the new activity.
     * @param baseUrl Base url of institute
     * @param username Username
     * @param password Password
     */
    public static void show(@NonNull Context context, @NonNull  String baseUrl,
                            @NonNull  String username, @NonNull  String password) {
        initImageLoader(context);
        context.startActivity(ExamsListActivity.getNewIntent(context, baseUrl, username, password));
    }

    public static void initImageLoader(Context context) {
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisk(true).cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new FadeInBitmapDisplayer(300)).build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new WeakMemoryCache())
                .diskCacheSize(500 * 1024 * 1024).build();

        ImageLoader.getInstance().init(config);
    }

}
