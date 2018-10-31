package in.testpress.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;
import static junit.framework.Assert.assertNotNull;

public class PreferenceUtils {

    private static final String TESTPRESS_SHARED_PREFS = "TESTPRESS_SHARED_PREFS";

    private static SharedPreferences preferences;
    private static SharedPreferences.Editor editor;

    public static SharedPreferences getPreferences(Context context) {
        if (preferences == null) {
            assertNotNull("Context must not be null.", context);
            preferences = context.getSharedPreferences(TESTPRESS_SHARED_PREFS, MODE_PRIVATE);
        }
        return preferences;
    }

    @SuppressLint("CommitPrefEdits")
    public static SharedPreferences.Editor getPreferenceEditor(Context context) {
        if (editor == null) {
            assertNotNull("Context must not be null.", context);
            editor = getPreferences(context).edit();
        }
        return editor;
    }

}
