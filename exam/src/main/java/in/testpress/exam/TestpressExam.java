package in.testpress.exam;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import in.testpress.exam.ui.AuthenticateFragment;
import in.testpress.exam.ui.ExamsListActivity;
import in.testpress.exam.ui.ExamsListFragment;

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
    public static void show(Activity activity, int containerViewId) {
        ExamsListFragment.show(activity, containerViewId);
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
    public static void show(Activity activity, int containerViewId, String baseUrl, String username,
                            String password) {
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
    public static void show(Context context) {
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
    public static void show(Context context, String baseUrl, String username, String password) {
        context.startActivity(ExamsListActivity.getNewIntent(context, baseUrl, username, password));
    }

}
