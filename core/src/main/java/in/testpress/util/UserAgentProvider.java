package in.testpress.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import java.util.Locale;

import in.testpress.BuildConfig;

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
 * {"app_name":"Testpress Sdk Sample App", "app_version":"1.0", "sdk_version":"1.0", "os":"6.0",
 * "brand":"motorola", "model":"XT1068", "locale":"en_US"}
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
                    userAgent = String.format("%s/%s Dalvik/%s (Linux; U; Android %s; %s %s Build/)",
                            context.getApplicationInfo().loadLabel(context.getPackageManager()),
                            appVersion,
                            System.getProperty("java.vm.version"),
                            Float.parseFloat(Build.VERSION.RELEASE),
                            Build.BRAND,
                            Build.MODEL,
                            Build.BOARD
                    );
                }
            }
        }
        return userAgent;
    }
}
