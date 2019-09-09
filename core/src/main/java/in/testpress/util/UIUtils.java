package in.testpress.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Build;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;

import in.testpress.R;

public class UIUtils {

    public static void hideSoftKeyboard(Activity activity) {
        if (activity != null) {
            hideSoftKeyboard(activity, activity.getCurrentFocus());
        }
    }

    public static void hideSoftKeyboard(Activity activity, View view) {
        if (activity != null && view != null) {
            InputMethodManager inputMethodManager =
                    (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);

            assert inputMethodManager != null;
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static void showSoftKeyboard(Activity activity, EditText editText) {
        if (activity != null) {
            InputMethodManager inputMethodManager =
                    (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);

            boolean isShowing =
                    inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_FORCED);

            if (!isShowing) {
                activity.getWindow()
                        .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            }
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

    public static void showSnackBar(View view, @StringRes int message) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
    }

    public static void showAlert(Context context, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context,
                R.style.TestpressAppCompatAlertDialogStyle);

        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.testpress_ok, null);
        builder.show();
    }
}
