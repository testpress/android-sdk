package in.testpress.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

/**
 * Class that builds a User-Agent that is set on all HTTP calls.
 *
 * The user agent will change depending on the version of Android that
 * the user is running, the device their running and the version of the
 * app that they're running. This will allow your remote API to perform
 * User-Agent inspection to provide different logic routes or analytics
 * based upon the User-Agent.
 *
 * Example
 *
 * in.testpress.samples/1.1.2 (Dalvik; Android 9; Xiaomi POCO F1 Build/PKQ1.180729.001) okhttp
 *
 */
public class UserAgentProvider {

    private static String userAgent;

    public static String get(Context context) {
        if (userAgent == null) {
            synchronized (UserAgentProvider.class) {
                if (userAgent == null) {
                    String appVersion = "";
                    try {
                        appVersion = context.getPackageManager().getPackageInfo(
                                context.getPackageName(), 0).versionName;
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                    userAgent = String.format("%s/%s (Dalvik; Android %s; %s %s Build/%s) okhttp",
                            context.getApplicationInfo().packageName,
                            appVersion,
                            Build.VERSION.RELEASE,
                            Build.MANUFACTURER,
                            Build.MODEL,
                            Build.ID
                    );
                }
            }
        }
        return userAgent;
    }
}
