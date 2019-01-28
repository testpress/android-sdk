package in.testpress.course.util;

import android.app.Activity;

public class StringUtil {

    public static String getPluralString(Activity activity, int pluralStringResourceId, int quantity, String defaultString) {
        try {
            return activity.getResources().getQuantityString(pluralStringResourceId, quantity);
        } catch (Exception e) {
            return defaultString;
        }
    }
}
