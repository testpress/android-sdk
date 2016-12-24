package in.testpress.exam;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import in.testpress.core.TestpressSdk;
import in.testpress.core.TestpressSession;
import in.testpress.exam.ui.CarouselFragment;
import in.testpress.exam.ui.CategoriesGridFragment;
import in.testpress.exam.ui.CategoryGridActivity;
import in.testpress.exam.ui.ExamsListActivity;
import in.testpress.exam.ui.TestActivity;
import in.testpress.util.ImageUtils;

public class TestpressExam {

    public static final String ACTION_PRESSED_HOME = "pressedHome";

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
        //noinspection ConstantConditions
        if (activity == null) {
            throw new IllegalArgumentException("Activity must not be null.");
        }
        init(activity, testpressSession);
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
        //noinspection ConstantConditions
        if (context == null) {
            throw new IllegalArgumentException("Context must not be null.");
        }
        init(context, testpressSession);
        Intent intent = new Intent(context, ExamsListActivity.class);
        context.startActivity(intent);
    }

    /**
     * Use when testpress exam categories need to be open in a container as a fragment.
     *
     * <p> Usage example:
     *
     * <p> TestpressSdk.initialize(getActivity(), "baseUrl", "userId", "accessToken", provider,
     * <p>             new TestpressCallback/<TestpressSession>() {
     * <p>             @Override
     * <p>             public void onSuccess(TestpressSession testpressSession) {
     * <p>                 <b>TestpressExam.showCategories(this, R,id.fragment_container, testpressSession);</b>
     * <p>             }
     * <p> });
     *
     * @param activity Activity that has the container
     * @param containerViewId Container view id in which fragment needs to be replace
     * @param testpressSession TestpressSession got from the core module
     */
    public static void showCategories(@NonNull FragmentActivity activity,
                            @NonNull @IdRes Integer containerViewId,
                            @NonNull TestpressSession testpressSession) {
        //noinspection ConstantConditions
        if (activity == null) {
            throw new IllegalArgumentException("Activity must not be null.");
        }
        init(activity, testpressSession);
        CategoriesGridFragment.show(activity, containerViewId);
    }

    /**
     * Use when testpress exam categories need to be open as a new Activity.
     *
     * <p> Usage example:
     *
     * <p> TestpressSdk.initialize(this, "baseUrl", "userId", "accessToken", provider,
     * <p>             new TestpressCallback/<TestpressSession>() {
     * <p>             @Override
     * <p>             public void onSuccess(TestpressSession testpressSession) {
     * <p>                 <b>TestpressExam.showCategories(this, testpressSession);</b>
     * <p>             }
     * <p> });
     *
     * @param context Context to start the new activity.
     * @param testpressSession TestpressSession got from the core module.
     */
    public static void showCategories(@NonNull Context context,
                                      @NonNull TestpressSession testpressSession) {
        //noinspection ConstantConditions
        if (context == null) {
            throw new IllegalArgumentException("Context must not be null.");
        }
        init(context, testpressSession);
        Intent intent = new Intent(context, CategoryGridActivity.class);
        context.startActivity(intent);
    }

    /**
     * Use to start a particular exam.
     *
     * <p> Usage example:
     *
     * <p> TestpressSdk.initialize(this, "baseUrl", "userId", "accessToken", provider,
     * <p>             new TestpressCallback/<TestpressSession>() {
     * <p>             @Override
     * <p>             public void onSuccess(TestpressSession testpressSession) {
     * <p>                 <b>TestpressExam.startExam(this, testpressSession);</b>
     * <p>             }
     * <p> });
     *
     * @param activity Context to start the new activity.
     * @param examSlug Slug of the exam which need to be start.
     * @param testpressSession TestpressSession got from the core module.
     */
    @SuppressWarnings("ConstantConditions")
    public static void startExam(@NonNull Activity activity, @NonNull String examSlug,
                                 @NonNull TestpressSession testpressSession) {
        if (activity == null) {
            throw new IllegalArgumentException("Activity must not be null.");
        }
        if (examSlug == null || examSlug.isEmpty()) {
            throw new IllegalArgumentException("PARAM_EXAM_SLUG must not be null or empty.");
        }
        init(activity, testpressSession);
        Intent intent = new Intent(activity, TestActivity.class);
        intent.putExtra(TestActivity.PARAM_EXAM_SLUG, examSlug);
        activity.startActivityForResult(intent, CarouselFragment.TEST_TAKEN_REQUEST_CODE);
    }

    private static void init(Context context, TestpressSession testpressSession) {
        if (testpressSession == null) {
            throw new IllegalArgumentException("TestpressSession must not be null.");
        }
        TestpressSdk.setTestpressSession(context, testpressSession);
        ImageUtils.initImageLoader(context);
    }

}
