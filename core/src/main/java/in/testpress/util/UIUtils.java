package in.testpress.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;

import in.testpress.R;

public class UIUtils {

    public static void hideSoftKeyboard(Activity activity) {
        if (activity != null && activity.getCurrentFocus() != null) {
            InputMethodManager inputMethodManager =
                    (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

    public static void setIndeterminateDrawable(Context context, Object view, int width) {
        float pixelWidth = getPixelFromDp(context, width);
        if (view instanceof ProgressDialog) {
            ((ProgressDialog) view).setIndeterminateDrawable(new CircularProgressDrawable(
                    ContextCompat.getColor(context, R.color.testpress_color_primary), pixelWidth));
        } else if (view instanceof ProgressBar) {
            ((ProgressBar) view).setIndeterminateDrawable(new CircularProgressDrawable(
                    ContextCompat.getColor(context, R.color.testpress_color_primary), pixelWidth));
        }
    }

    public static float getDpFromPixel(Context context, float px) {
        return px / context.getResources().getDisplayMetrics().density;
    }

    public static float getPixelFromDp(Context context, float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    public static void setGone(View[] views) {
        for (View view : views) {
            view.setVisibility(View.GONE);
        }
    }

    public static void setVisible(View[] views) {
        for (View view : views) {
            view.setVisibility(View.VISIBLE);
        }
    }

}
