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

import in.testpress.core.TestpressSession;
import in.testpress.exam.ui.CarouselFragment;
import in.testpress.exam.ui.ExamsListActivity;

public class TestpressExam {

    private static TestpressSession testpressSession;

    public static TestpressSession getTestpressSession() {
        if (testpressSession == null) {
            throw new IllegalStateException("Exam module is not initialized.");
        }
        return testpressSession;
    }

    /**
     * Use when testpress exam need to be open in a container as a fragment.
     *
     * <p> Usage example:
     *
     * <p> TestpressSdk.initialize(getActivity(), "baseUrl", "userId", "accessToken", provider,
     * <p>             new TestpressCallback/<TestpressSession>() {
     * <p>             @Override
     * <p>             public void onSuccess(TestpressSession testpressSession) {
     * <p>                 <b>TestpressExam.show(this, R,id.fragment_container, testpressSession);</b>
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
        if (activity == null) {
            throw new IllegalArgumentException("Activity must not be null.");
        } else if (testpressSession == null) {
            throw new IllegalArgumentException("TestpressSession must not be null.");
        }
        TestpressExam.testpressSession = testpressSession;
        initImageLoader(activity);
        CarouselFragment.show(activity, containerViewId);
    }

    /**
     * Use when testpress exam need to be open as a new Activity.
     *
     * <p> Usage example:
     *
     * <p> TestpressSdk.initialize(this, "baseUrl", "userId", "accessToken", provider,
     * <p>             new TestpressCallback/<TestpressSession>() {
     * <p>             @Override
     * <p>             public void onSuccess(TestpressSession testpressSession) {
     * <p>                 <b>TestpressExam.show(this, testpressSession);</b>
     * <p>             }
     * <p> });
     *
     * @param context Context to start the new activity.
     * @param testpressSession TestpressSession got from the core module.
     */
    public static void show(@NonNull Context context, @NonNull TestpressSession testpressSession) {
        if (context == null) {
            throw new IllegalArgumentException("Context must not be null.");
        } else if (testpressSession == null) {
            throw new IllegalArgumentException("TestpressSession must not be null.");
        }
        TestpressExam.testpressSession = testpressSession;
        initImageLoader(context);
        Intent intent = new Intent(context, ExamsListActivity.class);
        context.startActivity(intent);
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
