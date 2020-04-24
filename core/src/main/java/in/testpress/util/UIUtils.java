package in.testpress.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;

import androidx.annotation.StringRes;
import com.google.android.material.snackbar.Snackbar;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import in.testpress.R;
import in.testpress.core.TestpressSdk;
import in.testpress.models.InstituteSettings;

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

    public static void loadLogoInView(ImageView logoView, Context context) {
        InstituteSettings instituteSettings = TestpressSdk.getTestpressSession(context).getInstituteSettings();
        String url = instituteSettings.getAppToolbarLogo();
        ImageLoader imageLoader = ImageUtils.initImageLoader(context);
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
        imageLoader.displayImage(url, logoView, options);
    }
}
